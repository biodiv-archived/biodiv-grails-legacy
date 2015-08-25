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
import java.text.SimpleDateFormat ;
import java.util.Date;
import java.lang.*;
import java.io.File;
import species.participation.Observation;
import species.participation.DownloadLog;
import speciespage.ObvUtilService;
import species.auth.SUser;
import grails.converters.JSON;


/**
 * Exports data into Darwin core format
 */

class DwCObservationExporter {
public min_lat
public min_lon
public min_date

public max_lat
public max_lon 
public max_date 


	protected static DwCObservationExporter _instance
	private CSVWriter observationWriter
	private CSVWriter mediaWriter
	//private CSVWriter metaWriter
	/*private CSVWriter referenceWriter
	private CSVWriter agentWriter*/

	public static  DwCObservationExporter getInstance() {
		if(!_instance) {
			_instance = new DwCObservationExporter();
		}
		return _instance;
	}
	
	def exportObservationData(String directory, DownloadLog dl) {
		log.info "Darwin Core export started"
		/*	if(!directory) {
				directory = config.speciesPortal.species.speciesDownloadDir
			} */


		String folderName = "dwc_"+ + new Date().getTime()
		if(!directory)
			directory = File.createTempFile('','');

		String folderPath = directory + File.separator+ folderName + File.separator+ folderName
		initWriters(folderPath)
		fillHeaders() 

        ResourceFetcher rf = new ResourceFetcher(Observation.class.canonicalName, dl.filterUrl, null, dl.offsetParam);
        int total = 0;
        while(rf.hasNext() && total < ObvUtilService.EXPORT_BATCH_SIZE) {
            def list_of_observationInstance = rf.next();
            total += list_of_observationInstance.size();
            def obvList = [];
            list_of_observationInstance.each { 
                try {
                def json = it as JSON;
                def obv = JSON.parse(""+json);
                obvList << obv;
                } catch(e) {
                    log.debug "Error while exporting observation ${it}"
                    e.printStackTrace()
                }
            }

            exportObservation(obvList, dl.author, dl.id, dl.filterUrl)
            exportMedia(obvList)
        }
		closeWriters()

		def meta=new File (folderPath + '/meta.xml')
		meta<< '<archive xmlns="http://rs.tdwg.org/dwc/text/" metadata="metadata.eml.xml">\n\t<core encoding="UTF-8" fieldsTerminatedBy="\\t" linesTerminatedBy="\\n"  ignoreHeaderLines="1" rowType="http://rs.tdwg.org/dwc/terms/Occurrence">\n\t\t<files>\n\t\t\t<location>occurrence.txt</location>\n\t\t</files>\n\t\t<id index="0" />\n\t\t\t\t<field index="1" term="http://purl.org/dc/terms/modified"/>\n\t\t\t\t<field index="2"  term="http://purl.org/dc/terms/rightsHolder"/>\n\t\t\t\t<field index="3" default="India Biodiversity Portal" term="http://rs.tdwg.org/dwc/terms/institutionID"/>\n\t\t\t\t<field index="4" term="http://rs.tdwg.org/dwc/terms/datasetID"/>\n\t\t\t\t<field index="5" term="http://rs.tdwg.org/dwc/terms/datasetName"/>\n\t\t\t\t<field index="6" default="HumanObservation" term="http://rs.tdwg.org/dwc/terms/basisOfRecord"/>\n\t\t\t\t<field index="7" term="http://rs.tdwg.org/dwc/terms/informationWithheld"/>\n\t\t\t\t<field index="8" term="http://rs.tdwg.org/dwc/terms/occurrenceID"/>\n\t\t\t\t<field index="9" term="http://rs.tdwg.org/dwc/terms/recordedBy"/>\n\t\t\t\t<field index="10" term="http://rs.tdwg.org/dwc/terms/occurrenceRemarks"/>\n\t\t\t\t<field index="11" term="http://rs.tdwg.org/dwc/terms/verbatimEventDate"/>\n\t\t\t\t<field index="12" term="http://rs.tdwg.org/dwc/terms/habitat"/>\n\t\t\t\t<field index="13" term="http://rs.tdwg.org/dwc/terms/verbatimLocality"/>\n\t\t\t\t<field index="14" term="http://rs.tdwg.org/dwc/terms/locationRemarks"/>\n\t\t\t\t<field index="15" term="http://rs.tdwg.org/dwc/terms/decimalLatitude"/>\n\t\t\t\t<field index="16" term="http://rs.tdwg.org/dwc/terms/decimalLongitude"/>\n\t\t\t\t<field index="17" term="http://rs.tdwg.org/dwc/terms/identifiedBy"/>\n\t\t\t\t<field index="18" term="http://rs.tdwg.org/dwc/terms/dateIdentified"/>\n\t\t\t\t<field index="19" term="http://rs.tdwg.org/dwc/terms/taxonID"/>\n\t\t\t\t<field index="20" term="http://rs.tdwg.org/dwc/terms/scientificName"/>\n\t\t\t\t<field index="21" term="http://rs.tdwg.org/dwc/terms/vernacularName"/>\n\t\t\t\t<field index="22" term="http://purl.org/dc/terms/rights"/>\n\t</core>\n\n\t<extension encoding="UTF-8" fieldsTerminatedBy="\\t" linesTerminatedBy="\\n" ignoreHeaderLines="1" rowType="http://rs.gbif.org/terms/1.0/Multimedia">\n\t\t<files>\n\t\t\t<location>multimedia.txt</location>\n\t\t</files>\n\t\t<coreid index="0" />\n\t\t<field index="1" term="http://purl.org/dc/terms/type"/>\n\t\t<field index="2" term="http://purl.org/dc/terms/identifier"/>\n\t\t<field index="3" term="http://purl.org/dc/terms/created"/>\n\t\t<field index="4" term="http://purl.org/dc/terms/creator"/>\n\t\t<field index="5" term="http://purl.org/dc/terms/license"/>\n\t</extension>\n</archive>'
		File eml = returnMetaData_EML(folderPath, dl.author, dl.id, dl.filterUrl)
		return archive(directory, folderName)
	}

	public File returnMetaData_EML( String folderPath, reqUser, dl_id, params_filterUrl) {

		 File folder= new File(folderPath)
			 if(!folder.exists()){
					folder.mkdirs()
			}

		File eml = new File ( folder,  'metadata.eml.xml' )
				if(!eml.exists()){
					eml.createNewFile()

				}
				

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
	  eml << '</url>\n\t\t\t</online>\n\t\t</distribution>\n\t\t<coverage>\n\t\t\t<geographicCoverage>\n\t\t\t\t<geographicDescription>Bounding Box</geographicDescription>\n\t\t\t\t<boundingCoordinates>\n\t\t\t\t\t<westBoundingCoordinate>'
	  eml << min_lon
	  eml << '</westBoundingCoordinate>\n\t\t\t\t\t<eastBoundingCoordinate>'
	  eml << max_lon
	  eml << '</eastBoundingCoordinate>\n\t\t\t\t\t<northBoundingCoordinate>'
	  eml << max_lat
	  eml << '</northBoundingCoordinate>\n\t\t\t\t\t<southBoundingCoordinate>'
	  eml << min_lat
	  eml << '</southBoundingCoordinate> \n\t\t\t\t</boundingCoordinates>\n\t\t\t</geographicCoverage>\n\t\t\t<temporalCoverage>\n\t\t\t\t<rangeOfDates>\n\t\t\t\t\t<beginDate>\n\t\t\t\t\t\t<calendarDate>'
	  eml << min_date
	  eml << '</calendarDate>\n\t\t\t\t\t</beginDate>\n\t\t\t\t\t<endDate>\n\t\t\t\t\t\t<calendarDate>'
	  eml << max_date
	  eml << '</calendarDate>\n\t\t\t\t\t</endDate>\n\t\t\t\t</rangeOfDates>\n\t\t\t</temporalCoverage>\n\t\t</coverage>\n\t</dataset>\n</eml:eml> '



	return eml


	}

	protected void fillHeaders() {
		String[] observationHeader = [	

			"id",
			"modified",  
			"rightsHolder", 
			"institutionID", 
			"datasetID", 
			"datasetName", 
			"basisOfRecord", 
			"informationWithheld",
			"occurrenceID", 
			"recordedBy", 
			"occurrenceRemarks", 
			"verbatimEventDate",
			"habitat", 
			"verbatimLocality", 
			"locationRemarks ", 
			"decimalLatitude", 
			"decimalLongitude", 
			"identifiedBy", 
			"dateIdentified", 
			"taxonID", 
			"scientificName", 
			"vernacularName", 
			"rights"
		]

		observationWriter.writeNext(observationHeader)

		String[] mediaHeader = [

			"id", 
			"type",
			"identifier", 
			"createdDate", 
			"creator", 
			"license"
		]

		mediaWriter.writeNext(mediaHeader)
	}

	protected void initWriters(String targetDir) {
		File target = new File(targetDir)
		if(!target.exists()){
			target.mkdirs()
		}


		observationWriter = getCSVWriter(targetDir, 'occurrence.txt')
		mediaWriter = getCSVWriter(targetDir, 'multimedia.txt')

	}

	protected void closeWriters() {
		observationWriter.close()

		mediaWriter.close()

		//metaWriter.close()
	}

	public CSVWriter getCSVWriter(def directory, def fileName) {
		char separator = '\t'
		new File(directory).mkdir()
		CSVWriter writer = new CSVWriter(new FileWriter("$directory/$fileName"), separator, CSVWriter.NO_QUOTE_CHARACTER);
		return writer
	}

	public def exportObservation(List list_of_observationInstance, SUser reqUser, Long dl_id , String params_filterUrl) {
		return exportOccurence(list_of_observationInstance, reqUser, dl_id , params_filterUrl);
	} 

	public def exportOccurence(List list_of_observationInstance, SUser reqUser, Long dl_id, String params_filterUrl) {

		String[] observationRow
		String  result2 = ""
		boolean flag=true
		def date =new Date()//.getTime()

		list_of_observationInstance.each {

			observationRow = new String[23]
			observationRow[0] = (!it?.id) ? "" : it?.id;
			observationRow[1] = (!it?.lastRevised) ? "" : it?.lastRevised;  // should be last updated
			observationRow[2] = (!it?.author?.name) ? "" : it?.author?.name; //IBP users ID for Darwin core archive generation  
			observationRow[3] = "India Biodiversity Portal" //institution id
			observationRow[4] = dl_id //datasetid		
			observationRow[5]=   params_filterUrl + " " + date
			observationRow[6] = "humanObservation" //basis of record
			observationRow[7] =  informationWithheld_extraction(it)
			observationRow[8] = "http://indiabiodiversity.org/observation/show/"+it?.id 
			observationRow[9] = it?.author?.name +"(http://indiabiodiversity.org/user/show/" + it?.author?.id +")"
			observationRow[10] = notes_extraction(it)
			observationRow[11] = (!it?.fromDate) ? "" : it?.fromDate;
			observationRow[12] = (!it?.habitat?.name) ? "" : it?.habitat?.name;
			observationRow[13] = (!it?.placeName) ? "" : it?.placeName;
			observationRow[14] = (!it?.locationAccuracy) ? "" : it?.locationAccuracy;
			observationRow[15] = extract_latitude(it)

				
			def lat=extract_latitude(it)
			observationRow[16] = extract_longitude(it)
			def lon=extract_longitude(it)
		
			if(flag){

				min_lat=lat
				max_lat=lat
				min_lon=lon
				max_lon=lon
				min_date=it?.fromDate
				max_date=it?.fromDate
				flag=false
			}
			else {
				if(lat<min_lat)
					min_lat=lat
				if(lat>max_lat)
					max_lat=lat
				if(lon<min_lon)
					min_lon=lon
				if(lon>max_lon)
					max_lon=lon
				min_date=compareDate(it?.fromDate,min_date)
				max_date=compareDateMax(it?.fromDate, max_date)
			}

		
			observationRow[17] = extract_identified_by(it)  
			observationRow[18] = extract_date_identified(it)
			observationRow[19] = (!it?.maxVotedReco?.sciNameReco?.id) ? "" : it?.maxVotedReco?.sciNameReco?.id//specieNameid
			observationRow[20] = (!it?.maxVotedReco?.sciNameReco?.name) ? "Unidentified" : it?.maxVotedReco?.sciNameReco?.name		
			observationRow[21] = extract_vernacularName(it?.maxVotedReco?.commonNamesRecoList)
			observationRow[22]= "Copyright " + it?.author?.name  +  " licensed under a Creative Commons license: http://creativecommons.org/licenses/by/3.0 " 
			observationWriter.writeNext(observationRow)
		}
	}

    public def exportMedia(List list_of_observationInstance) {
        String[] mediaRow = new String[6]

        list_of_observationInstance.each {   second->

            second?.resource?.each { 

                mediaRow[0] = (!second?.id) ? "" : second?.id;
                mediaRow[1] = (!it?.type) ? "" : it?.type;
                mediaRow[2] = (!it?.url) ? "" : it?.url;
                mediaRow[3] =(!it?.uploadTime) ? "" : it?.uploadTime; 
                mediaRow[4] =(!it?.uploader?.name) ? "" : it?.uploader?.name+ ' (http://indiabiodiversity.org/user/show/' + it?.uploader?.id+')'
                mediaRow[5] = "Copyright " + it?.uploader?.name  +  " licensed under a Creative Commons "+it?.licenses+" license: http://creativecommons.org/licenses/by/3.0/" //this is temporary the actual licence should be pulled from the server 	


                mediaWriter.writeNext(mediaRow)		
            }
        }
    }

	public def occurrence_extraction(observationInstance) {
		String  result ="";
		if (observationInstance?.group?.name) {
			if(observationInstance.summary ) {
				result=observationInstance?.group?.name;
				String observed=observationInstance?.summary
				println observed
				String	result2 = observed.substring(0,1).toLowerCase() + observed.substring(1);
				println result2
				result2=result2.replaceAll("<b>", "")
				result2=result2.replaceAll("</b>", "")
				result2=result2.replaceAll("</a>", "")
				result2=result2.replaceAll("<b>", "")
				result2=result2.replaceAll("'", "")
				result2=result2.replaceAll("<.*?>", "")
				String word = observationInstance?.group?.name 
				String name = word.substring(0, word.length()-1)
				return name +" "+result2
				
			}
			println result
			return result 
		}
			
	}

	def informationWithheld_extraction(observationInstance) {
	String final_result= ""
	Boolean test = observationInstance?.geoPrivacy
		 
			if(test) {
			final_result= "The location data for this observation has been obfuscated by the user."

			}

			if((observationInstance?.fromDate != observationInstance?.toDate) && (observationInstance?.toDate)){

				if(!final_result){
			final_result= "The dates provided by the user were "+ observationInstance?.fromDate + " and "+ observationInstance?.toDate + ", here, we only considered " + observationInstance?.fromDate +"."

						}
				else{
				final_result=final_result+ "and the dates provided by the user are "+ observationInstance?.fromDate + " and "+ observationInstance?.toDate + ", we only consider this date " + observationInstance?.fromDate +"."

				}
			
			}

		return final_result
	}

	def extract_identified_by(observationInstance) {
	
	if(observationInstance?.maxVotedReco?.sciNameReco?.name){

	String name=observationInstance?.maxVotedReco?.sciNameReco?.name
	String recommendationName= ""

		observationInstance?.recommendationVote.each{

			if(it?.recommendation?.name==name){
				if(recommendationName==""){

					recommendationName= it?.author?.name+"(http://indiabiodiversity.org/user/show/" + it?.author?.id +")"

			}
				else{

					recommendationName=recommendationName+", "+ it?.author?.name+"(http://indiabiodiversity.org/user/show/" + it?.author?.id +")"

				}
			}	
		}
			
			return recommendationName
		}
		else{	
			return "There is no scientific name for this observation. "

		}
	}

def extract_date_identified(observationInstance) {

	if(observationInstance?.maxVotedReco?.sciNameReco?.name) {

	String id=observationInstance?.maxVotedReco?.sciNameReco?.id
	String recommendation_date=""

		observationInstance?.recommendationVote.each{
		String a=it?.recommendation?.id
	
			if(a == id){
				if(recommendation_date == "") {
					
					 recommendation_date=it?.votedOn
					 
			}
			else {
					String  datum=it?.votedOn
					
					
					recommendation_date = compareDate(datum,recommendation_date)
					
			}
			}	
			
		}
		return recommendation_date

	}	
	else{

		return ""
	}
}

	def extract_year(String date) {

		String year = date.substring(0, 4)	
	
		return year

		}

		def extract_month(String date) {

			String month=date.substring(5,7)
		
			return month
		}

		def extract_day(String date) {

		String day=date.substring(8, 10)
	
			return day
		}

		def extract_time(String date) {

		String time=date.substring(11, 19)
			
			return time
	}

	def compareDate(String date1, String date2) {     
			  
			
		String year1=extract_year(date1)
		String year2=extract_year(date2)
		String month1=extract_month(date1)
		String month2=extract_month(date2)
		String day1=extract_day(date1)
		String day2=extract_day(date2)
		String time1=extract_time(date1)
		String time2= extract_time(date2)

			if(year1<year2 ){
			
				return date1
			}
			else if (year2<year1 ) {
				
				return date2
				}
			else if(month1<month2){
				
					return date1
				}

			else if(month2<month1){
				
					return date2
				}
			else if(day1<day2) {
				
				return date1
			}
			else if (day2<day1) {
				
				return date2
			}
			else if (time1<time2){
				
			return date1
			}
			else if (time2<time1){
				
			return date2
			}
			
		return date1
	}

	def compareDateMax(String date1, String date2) {     
		  
		
	String year1=extract_year(date1)
	String year2=extract_year(date2)
	String month1=extract_month(date1)
	String month2=extract_month(date2)
	String day1=extract_day(date1)
	String day2=extract_day(date2)
	String time1=extract_time(date1)
	String time2= extract_time(date2)

		if(year1<year2 ){
		
			return date2
		}
		else if (year2<year1 ) {
			return date1
			
			}
		else if(month1<month2){
			
				return date2
			}

		else if(month2<month1){
			
				return date1

			}
		else if(day1<day2) {
			
			return date2

		}
		else if (day2<day1) {
			
			return date1

		}
		else if (time1<time2){
			
		return date2

		}
		else if (time2<time1){
			
			return date1
		}
		
		return date2
	}

	def extract_latitude(observationInstance) {
		String topo= observationInstance?.topology
		if(topo){
		topo=topo.replaceAll("POINT ",  "")
		
		def values =topo.split(" ")
		String lat=values[1][0..-2]
		return lat

		}
		return ""
	}

	def extract_longitude(observationInstance) {
		String topo= observationInstance?.topology
		if(topo){
		topo=topo.replaceAll("POINT ",  "")

		def values =topo.split(" ")
		String lon = values[0].substring(1)
		return lon
	}
	return ""
	}

	def notes_extraction(observationInstance) {



		String note=observationInstance?.notes
		if(note){
		note=note.replaceAll("<p>", "")
		note=note.replaceAll("</p>", "")
		note=note.replaceAll("<div>", "")
		note=note.replaceAll("</div>", "")
		note=note.replaceAll("&nbsp;", "")
		note=note.replaceAll("\n", "")
		note=note.replaceAll("\t", "")
		note=note.replaceAll("\r", "")
		note=note.substring(0,note.length())
		note=note.trim()	
		println note
		return note
		}
		return ""
	}

	def extract_vernacularName(observationInstance){
		String result=""

			observationInstance.each{
				if (result=="") {
					result= it?.language?.name + " : "+ it?.name	//language should be exchanged into 2 letters hashmap
				}
				else{
					result=result +", "+ it?.language?.name + " : "+ it?.name	 
				}
			}
		return result

	}

	def archive(directory, folderName) {

		def HOME = directory +File.separator + folderName
		def deploymentFiles = [ folderName+'/meta.xml', folderName+'/metadata.eml.xml', folderName+'/multimedia.txt', folderName+'/occurrence.txt' ]
		def zipFile = new File(HOME + ".zip")			
		new AntBuilder().zip( basedir: HOME,
                      destFile: zipFile.absolutePath,
                      includes: deploymentFiles.join(' ') )
			return zipFile
            
	}

}
