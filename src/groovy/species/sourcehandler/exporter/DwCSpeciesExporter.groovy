package species.sourcehandler.exporter
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.hibernate.SessionFactory;
import java.text.SimpleDateFormat;
import org.hibernate.exception.ConstraintViolationException;
import species.*
import species.Contributor
import species.ScientificName.TaxonomyRank
import java.util.HashSet
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry
import java.nio.channels.FileChannel
import au.com.bytecode.opencsv.CSVWriter
import org.apache.commons.logging.LogFactory
import species.utils.Utils;
import java.math.BigDecimal

/**
 * Exports data into Darwin core format
 */

class DwCSpeciesExporter{


	protected static DwCSpeciesExporter _instance
	private CSVWriter taxonWriter
	private CSVWriter mediaWriter
	private CSVWriter descriptionWriter
	private CSVWriter vernacularWriter
	/*private CSVWriter agentWriter*/

	public static  DwCSpeciesExporter getInstance() {
		if(!_instance) {
			_instance = new DwCSpeciesExporter();
		}
		return _instance;
	}
	
def exportSpecieData(String directory, list_of_species, reqUser , dl_id , params_filterUrl) {
		log.info "Darwin Core specie export started"
		/* if(!directory) {
			directory = config.speciesPortal.species.speciesDownloadDir
		} */


		String folderName = "dwc_specie"+  new Date().getTime()
		if(!directory)
			directory = File.createTempFile('','');
		String folderPath = directory + "/"+ folderName+ "/"+ folderName

		initWriters(folderPath)
		fillHeaders() 

		exportSpecie(list_of_species, reqUser , dl_id, params_filterUrl)

		closeWriters()


		def meta=new File (folderPath + '/meta.xml')
		
		meta<< '<?xml version="1.0"?>\n<archive xmlns="http://rs.tdwg.org/dwc/text/"  metadata="metadata.eml.xml">\n\t<core encoding="UTF-8" linesTerminatedBy="\\n" fieldsTerminatedBy="\\t" fieldsEnclosedBy="" ignoreHeaderLines="1" rowType="http://rs.tdwg.org/dwc/terms/Taxon">\n\t\t<files>\n\t\t\t<location>taxon.txt</location>\n\t\t</files>\n\t\t<id index="0"/>\n\t\t<field index="1" term="http://rs.tdwg.org/dwc/terms/scientificNameID"/>\n\t\t<field index="2" term="http://rs.tdwg.org/dwc/terms/scientificName"/>\n\t\t<field index="3" term="http://rs.tdwg.org/dwc/terms/kingdom"/>\n\t\t<field index="4" term="http://rs.tdwg.org/dwc/terms/phylum"/>\n\t\t<field index="5" term="http://rs.tdwg.org/dwc/terms/class"/>\n\t\t<field index="6" term="http://rs.tdwg.org/dwc/terms/order"/>\n\t\t<field index="7" term="http://rs.tdwg.org/dwc/terms/family"/>\n\t\t<field index="8" term="http://rs.tdwg.org/dwc/terms/genus"/>\n\t\t<field index="9" term="http://purl.org/dc/terms/modified"/>\n\t</core>\n\t<extension encoding="UTF-8" linesTerminatedBy="\\n" fieldsTerminatedBy="\\t" fieldsEnclosedBy="" ignoreHeaderLines="1" rowType="http://rs.gbif.org/terms/1.0/Description">\n\t\t<files>\n\t\t\t<location>description.txt</location>\n\t\t</files>\n\t\t<coreid index="0"/>\n\t\t<field index="1" term="http://purl.org/dc/terms/type"/>\n\t\t<field index="2" term="http://purl.org/dc/terms/description"/>\n\t\t<field index="3" term="http://purl.org/dc/terms/source"/>\n\t\t<field index="4" term="http://purl.org/dc/terms/created"/>\n\t\t<field index="5" term="http://purl.org/dc/terms/contributor"/>\n\t\t<field index="6" term="http://purl.org/dc/terms/audience"/>\n\t\t<field index="7" term="http://purl.org/dc/terms/license"/>\n\t</extension>\n\t<extension encoding="UTF-8" linesTerminatedBy="\\n" fieldsTerminatedBy="\\t" fieldsEnclosedBy="" ignoreHeaderLines="1" rowType="http://rs.gbif.org/terms/1.0/VernacularName">\n\t\t<files>\n\t\t\t<location>commonName.txt</location>\n\t\t</files>\n\t\t<coreid index="0"/>\n\t\t<field index="1" term="http://rs.tdwg.org/dwc/terms/vernacularName"/>\n\t\t<field index="2" term="http://purl.org/dc/terms/language"/>\n\t</extension>\n\t<extension encoding="UTF-8" linesTerminatedBy="\\n" fieldsTerminatedBy="\\t" fieldsEnclosedBy="" ignoreHeaderLines="1" rowType="http://rs.gbif.org/terms/1.0/Multimedia">\n\t\t<files>\n\t\t\t<location>multimedia.txt</location>\n\t\t</files>\n\t\t<coreid index="0"/>\n\t\t<field index="1" term="http://purl.org/dc/terms/description"/>\n\t\t<field index="2" term="http://purl.org/dc/terms/type"/>\n\t\t<field index="3" term="http://purl.org/dc/terms/identifier"/>\n\t\t<field index="4" term="http://purl.org/dc/terms/created"/>\n\t\t<field index="5" term="http://purl.org/dc/terms/creator"/>\n\t\t<field index="6" term="http://purl.org/dc/terms/license"/>\n\t\t<field index="7" term="http://purl.org/dc/terms/rightsHolder"/>\n\t</extension>\n</archive>'

		min_lat = ""
		min_lon = ""
		max_lat = ""
		max_lon = ""

		def eml=new File (folderPath + '/metadata.eml.xml')
		eml << '<?xml version="1.0" encoding="utf-8"?>\n<eml:eml\n        xmlns:eml="eml://ecoinformatics.org/eml-2.1.1" \n        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \n        xmlns:dc="http://purl.org/dc/terms/" \n        xsi:schemaLocation="eml://ecoinformatics.org/eml-2.1.1" \n        xml:lang="en"\n        packageId="IndiaBiodiversityPortal.observations.eml.'
  eml << dl_id
  eml << '"\n        system="http://indiabiodiversity.org" \n        scope="system">\n\t<dataset>\n\t\t<alternateIdentifier>'
  eml << params_filterUrl
  eml << '</alternateIdentifier>\n\t\t<title xml:lang="en">'
  eml << dl_id +" "+ new Date()
  eml << '</title>\n\n\n\n\n\t\t<pubDate>'
  eml << new Date()
  eml << '</pubDate>\n\t\t\t<!-- This is the RESOURCE language and not the metadata language which is at the bottom -->\n\t\t<language>en_US</language>\n\t\t<abstract>\n\t\t\t<para>'
  eml << dl_id
  eml << '</para>\n\t\t</abstract>\n\t\t<intellectualRights>\n\t\t\t<para>\n\t\t\t\tIndia Biodiversity Portal, downloaded on '
  eml << new Date()
  eml << '.\n\t\t\t\tFree for use by all individuals provided that the\n\t\t\t\trights holder is acknowledged under terms of \n\t\t\t\tCreative Commons licences in any use or publication.\n\t\t\t</para>\n\t\t</intellectualRights>\n\t\t<!-- The distributionType URL is generally meant for informational purposes, and the "function" attribute should be set to "information". -->\n\t\t<distribution scope="document">\n\t\t\t<online>\n\t\t\t\t<url function="information">'
  eml << "url of dl_id"
  eml << '</url>\n\t\t\t</online>\n\t\t</distribution>\n\t\t<coverage>\n\t\t\t<geographicCoverage>\n\t\t\t\t<geographicDescription>Bounding Box</geographicDescription>\n\t\t\t\t<boundingCoordinates>\n\t\t\t\t\t<westBoundingCoordinate>68.03215</westBoundingCoordinate>\n\t\t\t\t\t<eastBoundingCoordinate>97.40238</eastBoundingCoordinate>\n\t\t\t\t\t<northBoundingCoordinate>37.416667</northBoundingCoordinate>\n\t\t\t\t\t<southBoundingCoordinate>6.74678</southBoundingCoordinate> \n\t\t\t\t</boundingCoordinates>\n\t\t\t</geographicCoverage>\n\t\t</coverage>\n\t</dataset>\n</eml:eml> '

		return archive(directory , folderName)
	}


	protected void fillHeaders() {
		String[] taxonHeader = [	
			"id",
			"scientificNameID",
			"scientificName",  
			"kingdom", 
			"phylum", 
			"class", 
			"order", 
			"super-family",
			"family", 
			"sub-family",
			"genus",
			"sub-genus",
			"species",
			"infraspecies",
			"modified"
			//"rightsHolder",
			//"accessRights"
			
		]

		taxonWriter.writeNext(taxonHeader)

		String[] descriptionHeader = [
			"id",
			"type", 
			"description",
			"source", 
			"created", 
			//"creator", 
			"contributor",
			"audience" ,
			"license"
//			"rightsHolder"
		]

		descriptionWriter.writeNext(descriptionHeader)


		String[] vernacularHeader = [	

			"id",
			"vernacularName",
			"language"
		]

		vernacularWriter.writeNext(vernacularHeader)


		String[] mediaHeader = [

					"id", 
					"description",
					"type",
					"identifier", 
					"created", 
					"creator",
					"licence",
					"rightsHolder"
					
					
		]

		mediaWriter.writeNext(mediaHeader)


	}

	protected void initWriters(String targetDir) {
		File target = new File(targetDir)
		if(!target.exists()){
			target.mkdirs()
		}


		taxonWriter = getCSVWriter(targetDir, 'taxon.txt')

		descriptionWriter = getCSVWriter(targetDir, 'description.txt')
		vernacularWriter= getCSVWriter(targetDir, 'commonName.txt')
		mediaWriter = getCSVWriter(targetDir, 'multimedia.txt')

	}

	protected void closeWriters() {
		taxonWriter.close()
		descriptionWriter.close()
		vernacularWriter.close()
		mediaWriter.close()
	}

	public CSVWriter getCSVWriter(def directory, def fileName) {
		char separator = '\t'
		new File(directory).mkdir()
		CSVWriter writer = new CSVWriter(new FileWriter("$directory/$fileName"), separator, CSVWriter.NO_QUOTE_CHARACTER);
		return writer
	}

	public def exportSpecie(list_of_species, reqUser, dl_id , params_filterUrl) {

		exportTaxonSpecie(list_of_species, reqUser, dl_id , params_filterUrl);
		exportDescription_media(list_of_species)
		exportVernacular(list_of_species)

	} 

	public def exportTaxonSpecie( list_of_species, reqUser, dl_id , params_filterUrl) {

		String[] taxonRow
		taxonRow = new String[15]
	
list_of_species.each { specie->
		def ibpHierarchy

		taxonRow[0] = (!specie?.id) ? "" : specie?.id;
		taxonRow[1] = (!specie?.id) ? "" : specie?.id
		
		taxonRow[2] = (!specie?.taxonConcept?.name) ? "" : specie?.taxonConcept?.name; 


				specie?.taxonRegistry?.each{ hierarchies->
						String hierarchy = hierarchies?.clasification?.name 

						if( hierarchy == "IBP Taxonomy Hierarchy" ){
							ibpHierarchy =hierarchies	
						}
					}
			ibpHierarchy?.hierarchies?.eachWithIndex{  taxon, index ->
			switch(taxon?.rank) {
			    case "Kingdom" : taxonRow[3]=taxon?.name ; break;

			    case  "Phylum" : taxonRow[4]=taxon?.name ; break;
			    case  "Class" : taxonRow[5]=taxon?.name ; break; 
			    case  "Order" : taxonRow[6]=taxon?.name ; break; 
			    case  "Super-Family" : taxonRow[7]=taxon?.name ; break; 
			    case  "Family" : taxonRow[8]=taxon?.name ; break; 
			    case  "Sub-Family" : taxonRow[9]=taxon?.name ; break; 
			    case  "Genus" : taxonRow[10]=taxon?.name ; break;
			    case  "Sub-Genus" : taxonRow[11]=taxon?.name ; break;
			    case  "Species" : taxonRow[12]=taxon?.name ; break;
			    case  "Infraspecies" : taxonRow[13]=taxon?.name ; break;

			}
		}
		
		taxonRow[14] = specie?.lastRevised

			taxonWriter.writeNext(taxonRow)
	}

	}


	public def exportDescription_media(list_of_species){

list_of_species.each { specie->
		String[] descriptionRow
		String[] mediaRow
	 	descriptionRow = new String[8]
	 	mediaRow= new String[8]

		

		descriptionRow[0] = (!specie?.id) ? "" : specie?.id;
		
	
		String result=""
		specie?.fields?.each { new_field->
		
		
		
			if ((new_field?.field?.concept ) && (new_field?.field?.concept != "Nomenclature and Classification" )) {
			
				result=new_field?.field?.concept  
			             
			 	if (new_field?.field?.category ){
			 		if (new_field?.field?.subcategory){

			result=result+" : "+ new_field?.field?.category+" : "+new_field?.field?.subcategory
			}
			result=result+" : " + new_field?.field?.category
				}

			}

		descriptionRow[1]=result
		String text = new_field?.text.replaceAll("\\n", "");
		text = text.replaceAll("\\t", "")
			text = text.replaceAll(":", "")
		descriptionRow[2] = text
		String descriptionRow[3]=""

		new_field?.references?.each{
			if(descriptionRow[3]==""){
				descriptionRow[3]= it?.title + ' (' +it?.url+')'

			}
			descriptionRow[3]=descriptionRow[3]+", "+ it?.title + ' (' +it?.url+')'
		}

		descriptionRow[4]= (!new_field?.dateCreated) ? "" :new_field?.dateCreated
		descriptionRow[5]= (!new_field?.contributors) ? "" : extract_attributors(new_field?.contributors)
 		descriptionRow[6] = ""
 		descriptionRow[7] = ""

			new_field?.audienceTypes?.each {
				if(descriptionRow[6] == "")
					descriptionRow[6] = it?.name
				else
					descriptionRow[6] = ", " + it?.name
			}
			
			new_field?.licenses?.each {

				if(descriptionRow[7] == "")
					descriptionRow[7] = it?.url?.name
				else
					descriptionRow[7] = ", " + it?.url?.name
			}

 		descriptionWriter.writeNext(descriptionRow)


 		new_field?.resources?.each{ number->



 		if(number?.type!= "Icon"){ 

 		mediaRow[0]=(!specie?.id) ? "" : specie?.id;
 		mediaRow[1]= result
		mediaRow[2]=(!number?.type) ? "" : number?.type;
		mediaRow[3]=(!number?.url) ? "" : number?.url
		mediaRow[4]=(!number?.uploadTime) ? "" : number?.uploadTime
		mediaRow[5]=(!number?.uploader?.name) ? "" : number?.uploader?.name+' (http://indiabiodiversity.org/user/show/' + number?.uploader?.id+')'

		mediaRow[6]="CC-BY" 
		mediaRow[7]=mediaRow[5]
		mediaWriter.writeNext(mediaRow)
		}
		}
		
		}


				mediaRow[0]=(!specie?.id) ? "" : specie?.id;
			specie?.resource?.each { each_resource->

				if(each_resource?.type!= "Icon"){
					mediaRow[1]="Resource"
					mediaRow[2]=(!each_resource?.type) ? "" : each_resource?.type;
					mediaRow[3]=(!each_resource?.url) ? "" : each_resource?.url
					mediaRow[4]=(!each_resource?.uploadTime) ? "" : each_resource?.uploadTime
					mediaRow[5]=(!each_resource?.uploader?.name) ? "" : each_resource?.uploader?.name +'(http://indiabiodiversity.org/user/show/' + each_resource?.uploader?.name+')'
					mediaRow[6]="CC-BY" 
					mediaRow[7]=mediaRow[5]
					
					mediaWriter.writeNext(mediaRow)	
					
					}
					
			}
			
		

	}
}
	



	public def exportVernacular(list_of_species) {
		String[] vernacularRow

		list_of_species.each {
				specie->

		vernacularRow = new String[3]

		
		vernacularRow[0] = (!specie?.id) ? "" : specie?.id;
			specie?.common_names?.each { common_name->

				vernacularRow[1]=common_name?.name
				vernacularRow[2]=common_name?.language?.twoLetterCode


			vernacularWriter.writeNext(vernacularRow)	
			}			
		}
	}


			
public def extract_attributors(array){
	String result=""
	array?.each {
		if(result ==""){
		result = it?.name +' (http://indiabiodiversity.org/user/show/' + it?.id +')' 
		}

		result = result+"," +it?.name +' (http://indiabiodiversity.org/user/show/' + it?.id +')' 
	}
	return result
}



	def archive(directory, folderName) {


			def HOME = directory +"/" + folderName
			def deploymentFiles = [ folderName+'/meta.xml', folderName+'/metadata.eml.xml', folderName+'/description.txt', folderName+'/commonName.txt', folderName+'/taxon.txt', folderName+'/multimedia.txt' ]
			def zipFile = new File(HOME+ ".zip")
			new AntBuilder().zip( basedir: HOME,
                      destFile: zipFile.absolutePath,
                      includes: deploymentFiles.join( ' ' ) )
	}

}
