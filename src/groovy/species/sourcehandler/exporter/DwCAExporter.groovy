package species.sourcehandler.exporter

import grails.util.Holders;
import org.hibernate.SessionFactory;
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

/**
 * Exports data into Darwin core format
 */

class DwCAExporter {

	private static log = LogFactory.getLog(this);
	
	def config = grails.util.Holders.config
	def fieldsConfig = config.speciesPortal.fields
	def grailsApplication

	protected static DwCAExporter _instance
	private HashMap contributorsSet

	private CSVWriter taxaWriter
	private CSVWriter mediaWriter
	private CSVWriter commonNameWriter
	private CSVWriter referenceWriter
	private CSVWriter agentWriter


	public DwCAExporter() {
		contributorsSet = new HashMap()
	}

	//#TODO
	public static  DwCAExporter getInstance() {
		if(!_instance) {
			_instance = new DwCAExporter();
		}
		return _instance;
	}



    public File exportSpeciesData(String directory) {
        log.info "Darwin Core export started"

        if(!directory) {

            /*
            File targetDir = new File(config.speciesPortal.species.speciesDownloadDir)
            targetDir.createDirs()
            directory = targetDir.getPath()
             */

            directory = config.speciesPortal.species.speciesDownloadDir

        }

        String folderName = "dwc_"+ + new Date().getTime()

        if(!directory)
            directory = File.createTempFile('','');

        String folderPath = directory + "/"+ folderName

        initWriters(folderPath)
        fillHeaders()
        int max = 10, offset = 0;
        int total = Species.countByPercentOfInfoGreaterThan(0);
        for(int i=0; i<total;) {
            def speciesList = Species.findAllByPercentOfInfoGreaterThan(0, [max:max,offset:offset, sort:'id'])
            for(Species specie: speciesList) {
                println specie.id;
                exportSpecies(specie)
                i++;
            }

            offset = offset + max;
            cleanUpGorm();
            if(!speciesList) break;
        }
        exportAgents(directory)

        closeWriters()

        //Archive the directory and return the file
        return archive(directory, folderName)
    }

	/**
	 * Export species
	 * Iterate over each species and export related data
	 */
	public File exportSpeciesData(List<Species> speciesList, String directory) {
		
		log.info "Darwin Core export started"

		if(!directory) {

			/*
			File targetDir = new File(config.speciesPortal.species.speciesDownloadDir)
			targetDir.createDirs()
			directory = targetDir.getPath()
			*/
			
			directory = config.speciesPortal.species.speciesDownloadDir

		}
		
		String folderName = "dwc_"+ + new Date().getTime()
	    
        if(!directory)
            directory = File.createTempFile('','');

		String folderPath = directory + "/"+ folderName

		initWriters(folderPath)
		fillHeaders()

		for(Species specie: speciesList) {
			exportSpecies(specie)
		}
		exportAgents(directory)


		closeWriters()

		//Archive the directory and return the file
		return archive(directory, folderName)

	}

	def archive(directory, folderName) {
		String zipFileName = folderName+ ".zip"
		String inputDir = directory + "/" + folderName
		
        File f = new File(directory+"/"+zipFileName);
		ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(f))
		new File(inputDir).eachFile() { file ->
			zipFile.putNextEntry(new ZipEntry(file.getName()))
			def buffer = new byte[1024]
			file.withInputStream { i ->
				def l = i.read(buffer)
				// check wether the file is empty
				if (l > 0) {
					zipFile.write(buffer, 0, l)
	 			}
	  		}
			zipFile.closeEntry()
	 	}
		zipFile.close()
        return f;
	} 


	/**
	 * 
	 * @param species	
	 */
	public void exportSpecies(Species species) {
		exportTaxon(species)

		exportCommonNames(species.taxonConcept)
		exportMedia(species)
		exportReferences(species)
	} 


	/**
	 *    TaxonId	ScientificName	Parent TaxonID	Kingdom	Phylum	Class	Order	Family	Genus	TaxonRank	FurtherInformationURL	TaxonomicStatus	TaxonRemarks	NamePublishedIn	ReferenceID
	 * 
	 * @param taxon
	 * @return
	 */
	protected def exportTaxon(Species species) {

		//to export all synonyms, taxonDefinition and its parent classification
		def classification = Classification.findByName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
		List<TaxonomyDefinition> parentTaxon = species.taxonConcept.parentTaxonRegistry(classification).get(classification)
		TaxonomyDefinition taxon = species.taxonConcept

		String[] taxonRow
		// for each taxon

		taxonRow = new String[15]

		//		Taxon ID = taxon.id
		taxonRow[0] = 'accepted_'+taxon.id


		// scientificName

		taxonRow[1] = taxon.name

		//		Parent Taxon ID

		for(TaxonomyDefinition parentTaxonEntry: parentTaxon) {
			switch(parentTaxonEntry.rank) {

				case TaxonomyRank.KINGDOM.ordinal():
					taxonRow[3] = parentTaxonEntry.name
					break
				case TaxonomyRank.PHYLUM.ordinal():
					taxonRow[4] = parentTaxonEntry.name
					break
				case TaxonomyRank.CLASS.ordinal():
					taxonRow[5] = parentTaxonEntry.name
					break
				case TaxonomyRank.ORDER.ordinal():
					taxonRow[6] = parentTaxonEntry.name
					break
				case TaxonomyRank.FAMILY.ordinal():
					taxonRow[7] = parentTaxonEntry.name
					break
				case TaxonomyRank.GENUS.ordinal():
					taxonRow[8] = parentTaxonEntry.name

					break;
			}
		}

		//		TaxonRank = taxon.rank
		taxonRow[9] = "Species"

		//		furtherInformationURL  #TODO
		//      speciespage url
		taxonRow[10] = "${grails.util.Holders.config.grails.serverURL}/species/show/" + species.id
		taxonRow[10] = taxonRow[10].replace(":8080", "");
		taxonRow[10] = taxonRow[10].replace("/biodiv", "");

		//		taxonomicStatus   #TODO
		//      synonyms
		taxonRow[11] =  "accepted"

		//		taxonRemarks
		//taxonRow[12] = ""

		//		namePublishedIn
		//taxonRow[13] = ""

		//              referenceID
		def referenceID = []
		for(SpeciesField field: species.fields) {
		 	for(Reference ref: field.references) {
		 		if(field.id == 15) {
					referenceID.add(String.valueOf(ref.id))
				}
		  	}
		} 

		taxonRow[14] = referenceID.join(",")

		taxaWriter.writeNext(taxonRow)


		// Get sysnonyms for a taxon
		Synonyms[] synonyms = Synonyms.findByTaxonConcept(taxon)
		for(Synonyms synonym: synonyms) {

			taxonRow = new String[15]

			// Taxon ID = taxon.id
			taxonRow[0] = synonym.relationship.value().toLowerCase() +'_'+synonym.id


			// scientificName
			taxonRow[1] = synonym.name

			//parentTaxon
			taxonRow[2] = 'accepted_'+taxon.id.toString();

			//		TaxonRank
			taxonRow[9] = "Species"

			//		furtherInformationURL  #TODO
			//      speciespage url
			taxonRow[10] = "${grails.util.Holders.config.grails.serverURL}/species/show/" + species.id
			taxonRow[10] = taxonRow[10].replace(":8080", "");
			taxonRow[10] = taxonRow[10].replace("/biodiv", "");
			//		taxonomicStatus   #TODO
			//      synonyms
			taxonRow[11] = synonym.relationship.value().toLowerCase();


			//		taxonRemarks
			//taxonRow[12] = ""

			//		namePublishedIn
			//taxonRow[13] = ""

			//              referenceID
			//taxonRow[14] =""

			taxaWriter.writeNext(taxonRow)

		} 


	}


	/**
	 *MediaID	TaxonID	Type	Subtype	Format	Subject	Title	Description	AccessURI	ThumbnailURL	FurtherInformationURL	DerivedFrom	CreateDate	Modified	Language	Rating	Audience	License	Rights	Owner	BibliographicCitation	Publisher	Contributor     Attributor	Creator	AgentID	LocationCreated	GenericLocation	Latitude	Longitude	Altitude	ReferenceID
	 * 
	 * @param species
	 */
	protected void exportMedia(Species species) {

		def resources = species.getImages();

		String[] row

		for(Resource media: resources) {
			row = new String[32]
			// Media ID
			row[0] = 'res_'+media.id

			//TaxonID
			row[1] = 'accepted_'+species.taxonConcept.id

			//Type  #TODO: confirm  -- There are resources of type ICON also(very few)
			//row[2] = media.type.value()   //returns only as 'image'
			row[2] = "http://purl.org/dc/dcmitype/StillImage"

			//Subtype
			//row[3] = ""

			//Format  #TODO: confirm
			//row[4] = media.mimeType  //returns nothing
			row[4] = "image/jpeg"

			//Subject #TODO
			//row[5] = ""

			//Title
			row[6] = media.description?.replaceAll("\\t|\\n", ' ');

			//Description
			//row[7] = ""

			//AccessURI  -- get absolute url by appending domain name and base path(/biodiv/images/)
			row[8] = ""+grails.util.Holders.config.speciesPortal.resources.serverURL+media.fileName
			row[8] = row[8].replace(":8080", "");
			//ThumbnailURL - get thumbnail url-- there must be some function
			//row[9] =""

			//FurtherInformationURL #TODO - put thumbnail url
				row[10] = "${grails.util.Holders.config.grails.serverURL}/species/show/" + species.id
				row[10] = row[10].replace(":8080", "");
				row[10] = row[10].replace("/biodiv", "");


			//DerivedFrom
			row[11] = media.url

			//CreateDate
			//row[12] = ""

			//Modified
			//row[13] = ""
			//Language
			//row[14] = ""
			//Rating
			//Audience #Do not export audience field
			/*
			 String str=""
			 for(AudienceType aud : media.speciesFields.audienceTypes) {
			 str.append(aud.value()).append(",")
			 }
			 //row[16] = media.speciesFields.audienceTypes.join(",")
			 row[16] = str
			 */


			//License
			row[17] = media.licenses.url.join(",")

			//Rights
			//Owner #TODO: http://ns.adobe.com/xap/1.0/rights/Owner???

			//BibliographicCitation
			//Publisher
			//Contributor
			def contributors = []
			for(Contributor contributor: media.contributors) {
                if(contributorsSet.containsKey(contributor.name)) {
                    contributor = contributorsSet.get(contributor.name);
                }
				contributors.add(String.valueOf(contributor.id))
				contributorsSet.put(contributor.name, contributor)
			}
			row[25] = contributors.join(",")
			def attributors = []
			for(Contributor attributor: media.attributors) {
                if(contributorsSet.containsKey(attributor.name)) {
                    attributor = contributorsSet.get(attributor.name);
                }
				attributors.add(String.valueOf(attributor.id))
				contributorsSet.put(attributor.name, attributor)
			}
			row[23] = attributors.join(",")
			//Creator
			//AgentID
			//LocationCreated
			//GenericLocation
			//Latitude
			//Altitude
			//ReferenceID


			mediaWriter.writeNext(row)

		}

		// SpeciesFields of a species
		for(SpeciesField speciesField: species.fields) {
			if((speciesField.field.id >= 3 && speciesField.field.id <= 10) || (speciesField.field.id >= 37 && speciesField.field.id <= 75 && speciesField.field.id != 65) ) {
                if(speciesField.description) {
				row = new String[32]

				// Media ID
				row[0] = 'txt_'+speciesField.id

				//TaxonID
				row[1] = 'accepted_'+species.taxonConcept.id

				//Type
				row[2] = "http://purl.org/dc/dcmitype/Text"

				//Subtype
				//row[3] = speciesField.field.subCategory

				//Format
				row[4] = "text/html"

				//Subject #TODO
				row[5] = speciesField.field.urlIdentifier

				//Title
				row[6] = speciesField.field.category

				//Description
				row[7] = speciesField.description?.replaceAll("\\t|\\n", ' ');

				//AccessURI
				//row[8] = ""

				//ThumbnailURL
				//row[9] = ""

				//FurtherInformationURL #TODO - put thumbnail url
				row[10] = "${grails.util.Holders.config.grails.serverURL}/species/show/" + species.id
				row[10] = row[10].replace(":8080", "");
				row[10] = row[10].replace("/biodiv", "");
				//DerivedFrom
				//CreateDate
				//row[12] =

				//Modified
				//Language
				row[14] = "eng"
				//Rating
				//Audience #TODO: comma separated values from speciesFields
				/*
				 String str=""
				 for(AudienceType aud : media.speciesFields.audienceTypes) {
				 str.append(aud.value()).append(",")
				 }
				 //row[16] = media.speciesFields.audienceTypes.join(",")
				 row[16] = str
				 */


				//License
				row[17] = speciesField.licenses.url.join(",")

				//Rights
				//Owner #TODO: http://ns.adobe.com/xap/1.0/rights/Owner???

				//BibliographicCitation
				//Publisher
				//Contributor = speciesField.contributors.join(",")
				def contributors = []
				for(Contributor contributor: speciesField.contributors) {
                    if(contributorsSet.containsKey(contributor.name)) {
                        contributor = contributorsSet.get(contributor.name);
                    }
					contributors.add(String.valueOf(contributor.id))
					contributorsSet.put(contributor.name, contributor)
				}
				row[25] = contributors.join(",")
				def attributors = []
				for(Contributor attributor: speciesField.attributors) {
                    if(contributorsSet.containsKey(attributor.name)) {
                        attributor = contributorsSet.get(attributor.name);
                    }
					attributors.add(String.valueOf(attributor.id))
					contributorsSet.put(attributor.name, attributor)
				}
				row[23] = attributors.join(",")

				//Creator
				//AgentID
				//LocationCreated
				//GenericLocation
				//Latitude
				//Altitude
				//ReferenceId
				def referenceID = []
				for(Reference ref: speciesField.references) {
					referenceID.add(String.valueOf(ref.id))
				}
				row[31] = referenceID.join(",")

				mediaWriter.writeNext(row)
                }
			}

		}


	}



	/**
	 * TaxonID	Name	Source Reference	Language	Locality	CountryCode	IsPreferredName	TaxonRemarks
	 * 
	 * @param species
	 */
	protected void exportCommonNames(TaxonomyDefinition taxonConcept) {

		List<CommonNames> commonNames = CommonNames.findAllByTaxonConcept(taxonConcept)
		String[] row
		for(CommonNames cName: commonNames) {
			row = new String[7]

			//			taxonId
			row[0] = 'accepted_'+cName.taxonConcept.id

			//			Name
			row[1] = cName.name

			//			Source Reference
			//row[2] = ""

			//			Language
			def languageCode = cName.language?.threeLetterCode?:''
            if(Utils.isInteger(languageCode))
                row[3] = languageCode;
            
			//			Language Code
			//row[4] = cName.language?.threeLetterCode

			//			Locality == cName.language.region?? #TODO
			//row[5] = ""

			//			CountryCode
			//			IsPreferredName

			commonNameWriter.writeNext(row)
		}

	}


	/**
	 *ReferenceID	PublicationType	Full Reference	PrimaryTitle	SecondaryTitle	Pages	PageStart	PageEnd	Volume	Edition	Publisher	AuthorList	EditorList      DateCreated	Language	URL	DOI	LocalityOfPublisher
	 * @param species
	 */
	protected void exportReferences(Species species) {

		def fields = species.fields

		String[] row
		for (SpeciesField field: fields) {
			for(Reference reference: field.references ) {

				row = new String[18]

				//ReferenceID

				row[0] = reference.id


				//PublicationType
				//Full Reference
				row[2] = reference.title?.replaceAll("\\t|\\n", " ")
                row[2] = row[2]?:reference.url
				//PrimaryTitle
				//row[3] = ""
				//SecondaryTitle
				//Pages
				//PageStart
				//PageEnd
				//Volume
				//Edition
				//Publisher
				//AuthorList
				//EditorList
				//DateCreated
				//Language
				//URL
				row[15] = reference.url
				//DOI
				//LocalityOfPublisher

				referenceWriter.writeNext(row)

			}
		}

	}

	/**
	 *AgentID	Full Name	First Name	Family Name	Role	Email	Homepage	Logo URL	Project	Organization	AccountName	OpenID 
	 * @param species
	 */
	protected void exportAgents(String targetDir) {

		String[] row
		contributorsSet.each {key, agent ->
			row = new String[12]

			//AgentID
			row[0] = agent.id

			//Full Name
			row[1] = agent.name

			//First Name	Family Name	Role	Email	Homepage	Logo URL	Project	Organization	AccountName	OpenID

			agentWriter.writeNext(row)
		}

	}


	protected void fillHeaders() {
		String[] taxaHeader = [
			"TaxonID",
			"ScientificName",
			"Parent TaxonID",
			"Kingdom",
			"Phylum",
			"Class",
			"Order",
			"Family",
			"Genus",
			"TaxonRank",
			"FurtherInformationURL",
			"TaxonomicStatus",
			"TaxonRemarks",
			"NamePublishedIn",
			"ReferenceID"
		]
		taxaWriter.writeNext(taxaHeader)

		String[] mediaHeader = [
			"MediaID",
			"TaxonID",
			"Type",
			"Subtype",
			"Format",
			"Subject",
			"Title",
			"Description",
			"AccessURI",
			"ThumbnailURL",
			"FurtherInformationURL",
			"DerivedFrom",
			"CreateDate",
			"Modified",
			"Language",
			"Rating",
			"Audience",
			"License",
			"Rights",
			"Owner",
			"BibliographicCitation",
			"Publisher",
			"Contributor",
			"Attributor",
			"Creator",
			"AgentID",
			"LocationCreated",
			"GenericLocation",
			"Latitude",
			"Longitude",
			"Altitude",
			"ReferenceID"
		]
		mediaWriter.writeNext(mediaHeader)

		String[] commonNameHeader = [
			"TaxonID",
			"Name",
			"Source Reference",
			"Language",
			//"Language Code",
			"Locality",
			"CountryCode",
			"IsPreferredName",
			"TaxonRemarks"
		]
		commonNameWriter.writeNext(commonNameHeader)

		String[] referenceHeader = [
			"ReferenceID",
			"PublicationType",
			"Full Reference",
			"PrimaryTitle",
			"SecondaryTitle",
			"Pages",
			"PageStart",
			"PageEnd",
			"Volume",
			"Edition",
			"Publisher",
			"AuthorList",
			"EditorList",
			"DateCreated",
			"Language",
			"URL",
			"DOI",
			"LocalityOfPublisher"
		]
		referenceWriter.writeNext(referenceHeader)

		String[] agentHeader = [
			"AgentID",
			"Full Name",
			"First Name",
			"Family Name",
			"Role",
			"Email",
			"Homepage",
			"Logo URL",
			"Project",
			"Organization",
			"AccountName",
			"OpenID"
		]
		agentWriter.writeNext(agentHeader)

	}

	protected void initWriters(String targetDir) {
		
		File target = new File(targetDir)
		if(!target.exists())
			target.mkdirs()

		taxaWriter = getCSVWriter(targetDir, 'taxa.txt')
		mediaWriter = getCSVWriter(targetDir, 'media.txt')
		commonNameWriter = getCSVWriter(targetDir, 'common names.txt')
		referenceWriter = getCSVWriter(targetDir, 'references.txt')
		agentWriter = getCSVWriter(targetDir, 'agents.txt')
	}

	protected void closeWriters() {
		taxaWriter.close()
		mediaWriter.close()
		commonNameWriter.close()
		referenceWriter.close()
		agentWriter.close()
	}

	/**
	 * get Writer
	 */
	public CSVWriter getCSVWriter(def directory, def fileName) {
		char separator = '\t'
		new File(directory).mkdir()
		CSVWriter writer = new CSVWriter(new FileWriter("$directory/$fileName"), separator, CSVWriter.NO_QUOTE_CHARACTER);
		return writer
	}


    /**
     *
     */
    private void cleanUpGorm() {
        def ctx = grails.util.Holders.getGrailsApplication().getMainContext();
        SessionFactory sessionFactory = ctx.getBean("sessionFactory")
        def hibSession = sessionFactory?.getCurrentSession()
        if(hibSession) {
            log.debug "Flushing and clearing session"
            try {
                //hibSession.flush()
            } catch(ConstraintViolationException e) {
                e.printStackTrace()
            }
            hibSession.clear()
        }
    }




}

