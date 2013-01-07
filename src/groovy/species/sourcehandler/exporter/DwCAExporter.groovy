package species.sourcehandler.exporter

import species.*
import species.Contributor
import species.TaxonomyDefinition.TaxonomyRank
import java.util.HashSet

import au.com.bytecode.opencsv.CSVWriter

/**
 * Exports data into Darwin core format
 */

class DwCAExporter {
        
        def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def fieldsConfig = config.speciesPortal.fields

        protected static DwCAExporter _instance
        private HashSet contributorsSet

        private CSVWriter taxaWriter
        private CSVWriter mediaWriter
        private CSVWriter commonNameWriter
        private CSVWriter referenceWriter
        private CSVWriter agentWriter
        

	public DwCAExporter() {
            contributorsSet = new HashSet()
        }

	//#TODO
	public static  DwCAExporter getInstance() {
		if(!_instance) {
			_instance = new DwCAExporter();
		}
		return _instance;
	}




	/**
	 * Export species
	 * #TODO : Iterate over species and export one at a time or export everything at once?
	 *
	 */
	public void exportSpeciesData(String directory) {

                initWriters(directory)
                fillHeaders()

                // percentOfInfo>0 - one way

		List<Species> speciesList = Species.list(max:100)
	        	
		for(Species specie: speciesList) {
			exportSpecies(specie)
		}
		exportAgents(directory)
                closeWriters()
                
	}


	/**
	 * 
	 * @param species
	 * @param directory
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
		taxonRow[0] = taxon.id
		

		// scientificName

		taxonRow[1] = taxon.name

		//		Parent Taxon ID
		
                for(TaxonomyDefinition parentTaxonEntry: parentTaxon) {
                    switch(parentTaxonEntry.rank) {

                        case TaxonomyRank.KINGDOM:
        	            taxonRow[3] = parentTaxonEntry.name
                            break                            
                        case TaxonomyRank.PHYLUM:
			    taxonRow[4] = parentTaxonEntry.name
                            break
                        case TaxonomyRank.CLASS:
                            taxonRow[5] = parentTaxonEntry.name
                            break
                        case TaxonomyRank.ORDER:
                            taxonRow[6] = parentTaxonEntry.name
                            break
                        case TaxonomyRank.FAMILY:
                            taxonRow[7] = parentTaxonEntry.name
                            break
                        case TaxonomyRank.GENUS:
                            taxonRow[8] = parentTaxonEntry.name
//                            taxonRow[2] = parentTaxonEntry.id
                            break;
                        }
                }

		//		TaxonRank = taxon.rank
		taxonRow[9] = "Species"

		//		furtherInformationURL  #TODO
		//      speciespage url
		taxonRow[10] = "${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.grails.serverURL}/species/show/" + species.id

		//		taxonomicStatus   #TODO
		//      synonyms
		taxonRow[11] =  "accepted"

		//		taxonRemarks 
		taxonRow[12] = ""

		//		namePublishedIn 
		taxonRow[13] = ""

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
					taxonRow[0] = synonym.id
					
			
					// scientificName
					taxonRow[1] = synonym.name
					taxonRow[2] = taxon
					//		TaxonRank 
					taxonRow[9] = "Species"
			
					//		furtherInformationURL  #TODO
					//      speciespage url
					taxonRow[10] = "${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.grails.serverURL}/species/show/" + species.id
			
					//		taxonomicStatus   #TODO
					//      synonyms
					taxonRow[11] = synonym.relationship.value() 
			
					//		taxonRemarks 
					taxonRow[12] = ""
			
					//		namePublishedIn 
					taxonRow[13] = ""
			
					//              referenceID
					taxonRow[14] =""
			
					taxaWriter.writeNext(taxonRow)
                    
                }


	}


	/**
	 *MediaID	TaxonID	Type	Subtype	Format	Subject	Title	Description	AccessURI	ThumbnailURL	FurtherInformationURL	DerivedFrom	CreateDate	Modified	Language	Rating	Audience	License	Rights	Owner	BibliographicCitation	Publisher	Contributor	Creator	AgentID	LocationCreated	GenericLocation	Latitude	Longitude	Altitude	ReferenceID
	 * 
	 * @param species
	 */
	protected void exportMedia(Species species) {

		def resources = species.getImages();

		String[] row

		for(Resource media: resources) {
			row = new String[31]
			// Media ID
			row[0] = media.id

			//TaxonID
			row[1] = species.taxonConcept.id

			//Type  #TODO: confirm  -- There are resources of type ICON also(very few)
			//row[2] = media.type.value()   //returns only as 'image'
                        row[2] = "http://purl.org/dc/dcmitype/StillImage"

			//Subtype
                        row[3] = ""

			//Format  #TODO: confirm
			//row[4] = media.mimeType  //returns nothing
                        row[4] = "image/jpeg"

			//Subject #TODO
			row[5] = ""

			//Title
			row[6] = media.description

			//Description
			row[7] = ""

			//AccessURI  -- get absolute url by appending domain name and base path(/biodiv/images/)
			row[8] = "${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.grails.serverURL}${media.fileName}"

			//ThumbnailURL - get thumbnail url-- there must be some function
			row[9] ="" 

			//FurtherInformationURL #TODO - put thumbnail url
			row[10] = media.url

			//DerivedFrom
            row[11] = ""

			//CreateDate
			row[12] = ""

			//Modified	
            row[13] = ""
			//Language
            row[14] = ""
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
                            contributors.add(String.valueOf(contributor.id))
                            contributorsSet.add(contributor)
                        }
                        row[22] = contributors.join(",")
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
                    if((speciesField.field.id >= 3 && speciesField.field.id <= 10) || (speciesField.field.id >= 37 && speciesField.field.id <= 75) ) {
			row = new String[31]
			// Media ID
			row[0] = speciesField.id

			//TaxonID
			row[1] = species.taxonConcept.id

			//Type
			row[2] = "http://purl.org/dc/dcmitype/Text"

			//Subtype
                        row[3] = speciesField.field.subCategory

			//Format
			row[4] = "text/html"

			//Subject #TODO
			row[5] = speciesField.field.urlIdentifier

			//Title
			row[6] = speciesField.field.category

			//Description
			row[7] = speciesField.description

			//AccessURI
			row[8] = ""

			//ThumbnailURL
			row[9] = ""

			//FurtherInformationURL #TODO - put thumbnail url
			row[10] = "${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.grails.serverURL}/species/show/" + species.id

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
                            contributors.add(String.valueOf(contributor.id))
                            contributorsSet.add(contributor)
                        }
                        row[22] = contributors.join(",")
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
                        row[30] = referenceID.join(",")

           	    mediaWriter.writeNext(row)
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
			row = new String[8]

	        //			taxonId
	        row[0] = cName.taxonConcept.id

	        //			Name
	        row[1] = cName.name

	        //			Source Reference
	        row[2] = ""

        	//			Language
        	row[3] = cName.language?.name
        	
        	//			Language Code
        	row[4] = cName.language?.threeLetterCode

        	//			Locality == cName.language.region?? #TODO
	        row[5] = ""							

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
                                 row[2] = reference.title
				 //PrimaryTitle
				 row[3] = ""
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
                for (Contributor agent: contributorsSet) {
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
                String[] taxaHeader = ["TaxonID", "ScientificName", "Parent TaxonID", "Kingdom", "Phylum", "Class", "Order", "Family", "Genus", "TaxonRank", "FurtherInformationURL", "TaxonomicStatus", "TaxonRemarks", "NamePublishedIn", "ReferenceID"]
                taxaWriter.writeNext(taxaHeader)

                String[] mediaHeader = ["MediaID", "TaxonID", "Type", "Subtype", "Format", "Subject", "Title", "Description", "AccessURI", "ThumbnailURL", "FurtherInformationURL", "DerivedFrom", "CreateDate", "Modified", "Language", "Rating", "Audience", "License", "Rights", "Owner", "BibliographicCitation", "Publisher", "Contributor", "Creator", "AgentID", "LocationCreated", "GenericLocation", "Latitude", "Longitude", "Altitude", "ReferenceID"]
                mediaWriter.writeNext(mediaHeader)

                String[] commonNameHeader = ["TaxonID", "Name", "Source Reference", "Language", "Language Code", "Locality", "CountryCode", "IsPreferredName", "TaxonRemarks"]
                commonNameWriter.writeNext(commonNameHeader)

                String[] referenceHeader = ["ReferenceID", "PublicationType", "Full Reference", "PrimaryTitle", "SecondaryTitle", "Pages", "PageStart", "PageEnd", "Volume", "Edition", "Publisher", "AuthorList", "EditorList", "DateCreated", "Language", "URL", "DOI", "LocalityOfPublisher"]
                referenceWriter.writeNext(referenceHeader)

                String[] agentHeader = ["AgentID", "Full Name", "First Name", "Family Name", "Role", "Email", "Homepage", "Logo URL", "Project", "Organization", "AccountName", "OpenID"]
                agentWriter.writeNext(agentHeader)

        }

        protected void initWriters(String targetDir) {
            
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
		CSVWriter writer = new CSVWriter(new FileWriter("$directory/$fileName"), separator)
		return writer
	}




}

