package species.sourcehandler.exporter

import species.*
import species.Contributor
import species.TaxonomyDefinition.TaxonomyRank

import au.com.bytecode.opencsv.CSVWriter

class DwCAExporter {

	protected static DwCAExporter _instance;
	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	def fieldsConfig = config.speciesPortal.fields

        private CSVWriter taxaWriter
        private CSVWriter mediaWriter
        private CSVWriter commonNameWriter
        private CSVWriter referenceWriter
        private CSVWriter agentWriter
        

	public DwCAExporter() {
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
		List<Species> speciesList = Species.list()
		
		exportAgents(directory)
		for(Species specie: speciesList) {
			exportSpecies(specie)
		}
                closeWriters()
                
	}


	/**
	 * 
	 * @param species
	 * @param directory
	 */
	public void exportSpecies(Species species) {

		//to export all synonyms, taxonDefinition and its parent classification
		def classification = Classification.findByName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
		List<TaxonomyDefinition> parentTaxon = species.taxonConcept.parentTaxonRegistry(classification).get(classification);
		exportTaxon(species.taxonConcept, parentTaxon);

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
	protected def exportTaxon(TaxonomyDefinition taxon, List<TaxonomyRegistry> parentTaxon) {


		String[] taxonRow

		// for each taxon

		taxonRow = new String[15]

//		Taxon ID = taxon.id
		taxonRow[0] = taxon.id
		

		// scientificName= taxon.binomialForm

		taxonRow[1] = taxon.name

		//		Parent Taxon ID
		taxonRow[2] = parentTaxon[6].id

		//		t['Kingdom'] =  
		taxonRow[3] = parentTaxon[0].name

		//		Phylum
		taxonRow[4] = parentTaxon[1].name

		//		Class 
		taxonRow[5] = parentTaxon[2].name

		//		Order 
		taxonRow[6] = parentTaxon[3].name

		//		Family 
		taxonRow[7] = parentTaxon[4].name

		//		Genus
		taxonRow[8] = parentTaxon[6].name

		//		TaxonRank 
		taxonRow[9] = taxon.rank

		//		furtherInformationURL  #TODO
		//      speciespage url
		taxonRow[10] = ""

		//		taxonomicStatus   #TODO
		//      synonyms
		taxonRow[11] =  

		//		taxonRemarks 
		taxonRow[12] = ""

		//		namePublishedIn 
		taxonRow[13] = ""

		//              referenceID
		taxonRow[14] = ""

		taxaWriter.writeNext(taxonRow)

	}


	/**
	 *MediaID	TaxonID	Type	Subtype	Format	Subject	Title	Description	AccessURI	ThumbnailURL	FurtherInformationURL	DerivedFrom	CreateDate	Modified	Language	Rating	Audience	License	Rights	Owner	BibliographicCitation	Publisher	Contributor	Creator	AgentID	LocationCreated	GenericLocation	Latitude	Longitude	Altitude	ReferenceID
	 * 
	 * @param species
	 */
	protected void exportMedia(Species species) {

		def resources = species.resources

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
			row[7] = media.description

			//AccessURI  -- get absolute url by appending domain name and base path(/biodiv/images/)
			row[8] = media.fileName

			//ThumbnailURL - get thumbnail url-- there must be some function
			row[9] = media.fileName

			//FurtherInformationURL #TODO - put thumbnail url
			row[10] = media.url

			//DerivedFrom

			//CreateDate
			//row[12] = 

			//Modified	
			//Language	
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
                        row[17] = media.licenses.name.join(",")

			//Rights 
			//Owner #TODO: http://ns.adobe.com/xap/1.0/rights/Owner???

			//BibliographicCitation	
			//Publisher	
			//Contributor
                        row[22] = media.contributors.join(",")
			//Creator	
			//AgentID	
			//LocationCreated	
			//GenericLocation	
			//Latitude

           	 mediaWriter.writeNext(row)

		}

                // SpeciesFields of a species
                for(SpeciesField speciesField: species.fields) {
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
			row[5] = "http://rs.tdwg.org/ontology/voc/SPMInfoItems#GeneralDescription"

			//Title
			row[6] = speciesField.field.category

			//Description
			row[7] = speciesField.description

			//AccessURI
			row[8] = ""

			//ThumbnailURL
			row[9] = ""

			//FurtherInformationURL #TODO - put thumbnail url
			row[10] = ""

			//DerivedFrom

			//CreateDate
			//row[12] = 

			//Modified	
			//Language	
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
                        row[17] = speciesField.licenses.name.join(",")

			//Rights 
			//Owner #TODO: http://ns.adobe.com/xap/1.0/rights/Owner???

			//BibliographicCitation	
			//Publisher	
			//Contributor
                        row[22] = speciesField.contributors.join(",")
			//Creator	
			//AgentID	
			//LocationCreated	
			//GenericLocation	
			//Latitude

           	 mediaWriter.writeNext(row)

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
        	row[3] = cName.language.name
        	
        	//			Language Code
        	row[4] = cName.language.threeLetterCode

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
				 //PrimaryTitle
				 row[3] = reference.title
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

                
                //TODO: Should the contributors list be taken from SUser??
                
                def agents = Contributor.list()
                String[] row
                for (Contributor agent: agents) {
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

