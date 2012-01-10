package speciespage

import groovy.sql.Sql

import org.hibernate.exception.ConstraintViolationException

import species.Classification
import species.Species;
import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;
import species.TaxonomyRegistry
import species.formatReader.SpreadsheetReader
import species.sourcehandler.XMLConverter

class TaxonService {

	static transactional = false

	def grailsApplication
	def sessionFactory
	def groupHandlerService;
	def namesLoaderService;
	def speciesService;
	
	static int BATCH_SIZE = 500;

	/**
	 * 
	 * @return
	 */
	def loadTaxon(boolean createSpeciesStubsFlag) {
		loadFlowersOfIndia();
		loadEFlora();
		loadFishBase();
		loadGBIF();
		loadIBP();
		cleanUpGorm();
		
		groupHandlerService.loadGroups(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Groups.xlsx", 0, 0);		
		namesLoaderService.syncNamesAndRecos(false);
		
		if(createSpeciesStubsFlag) {
			createSpeciesStubs();
		}
	}

	/**
	 * 
	 * @return
	 */
	def loadFlowersOfIndia() {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<Map> content;
		//flowersOfIndia
		content = SpreadsheetReader.readSpreadSheet(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/FlowersByBotanicalNames.xls", 0, 0);
		int i=0;
		for (Map row : content) {
			String name = row.get("botanical name");
			String synonyms = row.get("synonyms")
			String family = row.get("family")
			String commonName = row.get("common name")
			List taxonEntries = new ArrayList();
			if(family)	 {
				Node taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", family)
				taxonEntries.add(taxon1);
			}

			if(name) {
				Node taxon2 = builder.createNode("field");
				new Node(taxon2, "subcategory", "species")
				new Node(taxon2, "data", name)
				taxonEntries.add(taxon2);
			}


			def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.FLOWERS_OF_INDIA_TAXONOMIC_HIERARCHY)

			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);

			def taxonConcept = converter.getTaxonConcept(registry, c);

			//synonyms
			Node synonymsNode = builder.createNode("field");
			synonyms.tokenize(',').each { syn ->
				new Node(synonymsNode, "data", syn.trim());
			}
			converter.createSynonyms(synonymsNode, taxonConcept);

			//commonnames
			Node commonNameNode = builder.createNode("field");
			if(commonName) {
				new Node(commonNameNode, "data", commonName.trim());
				converter.createCommonNames(commonNameNode, taxonConcept);
			}
			i++;
		}

	}

	/**
	 * 
	 * @return
	 */
	def loadEFlora() {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<Map> content;

		//eflora
		content = SpreadsheetReader.readSpreadSheet(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/eflora_data_CN.xlsx", 0, 0);
		int i=0;
		for (Map row : content) {
			String name = row.get("scientific_names");
			String synonyms = row.get("synonyms cleaned")

			String family = row.get("family")
			String commonName = row.get("common_names with separator")
			List taxonEntries = new ArrayList();

			Node taxon1;
			if(family) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", family)
				taxonEntries.add(taxon1);
			}

			if(name) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "species")
				new Node(taxon1, "data", name)
				taxonEntries.add(taxon1);
			}

			def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)
			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);

			def taxonConcept = converter.getTaxonConcept(registry, c);

			//synonyms
			Node synonymsNode = builder.createNode("field");
			synonyms.tokenize(',').each { syn ->
				new Node(synonymsNode, "data", syn.trim());
			}
			converter.createSynonyms(synonymsNode, taxonConcept);

			//commonnames
			Node commonNameNode = builder.createNode("field");
			commonName.split(';').each { part ->
				if(part) {
					String[] commonNames = part.split(":");
					if(commonNames.length == 2) {
						commonNames[1].tokenize(",").each {
							Node data = new Node(commonNameNode, "data", it);
							Node language = new Node(data, "language");
							new Node(language, "name", commonNames[0]);
						}
					} else {
						commonNames[0].tokenize(",").each {
							new Node(commonNameNode, "data", it);
						}
					}
				}
			}
			converter.createCommonNames(commonNameNode, taxonConcept);
			i++
		}
	}

	/**
	 * 
	 * @return
	 */
	def loadFishBase() {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<Map> content;

		//fishbase
		int i=0;
		content = SpreadsheetReader.readSpreadSheet(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/fishbase_30_11_2011.xls", 0, 0);
		for (Map row : content) {
			String name = row.get("species");
			String author = row.get("author");
			String kingdom = row.get("kingdom")
			String phylum = row.get("phylum")
			String klass = row.get("class")
			String order = row.get("order")
			String family = row.get("family")
			String commonName = row.get("common names")

			List taxonEntries = new ArrayList();
			Node taxon1
			if(kingdom) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "kingdom")
				new Node(taxon1, "data", kingdom)
				taxonEntries.add(taxon1);
			}

			if(phylum) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "phylum")
				new Node(taxon1, "data", phylum)
				taxonEntries.add(taxon1);
			}

			if(klass) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "class")
				new Node(taxon1, "data", klass)
				taxonEntries.add(taxon1);
			}

			if(order) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "order")
				new Node(taxon1, "data", order)
				taxonEntries.add(taxon1);
			}

			if(family) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", family)
				taxonEntries.add(taxon1);
			}

			if(name) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "species")
				new Node(taxon1, "data", name+" "+author)
				taxonEntries.add(taxon1);
			}

			def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.FISHBASE_TAXONOMIC_HIERARCHY)
			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);

			def taxonConcept = converter.getTaxonConcept(registry, c);

			//commonnames
			Node commonNameNode = builder.createNode("field");
			commonName.split(',').each { part ->
				if(part) {
					String[] commonNames = part.split("\\(");
					if(commonNames.length == 2) {
						commonNames[0].split(",").each {
							if(it) {
								Node data = new Node(commonNameNode, "data", it);
								Node language = new Node(data, "language");
								new Node(language, "name", commonNames[1].replace(")",""));
							}
						}
					} else {
						commonNames[0].split(",").each {
							if(it)
								new Node(commonNameNode, "data", it);
						}
					}
				}				
			}
			converter.createCommonNames(commonNameNode, taxonConcept);
			i++;
			cleanUpGorm();
		}

	}

	/**
	 * 
	 * @return
	 */
	def loadGBIF() {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<Map> content;

		//gbif
		String gbifTaxaFile = grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/GBIF taxonomy-search-13208373774487451330519969730577/taxonomy-search-1320837377448.txt"
		List taxonEntries = new ArrayList();
		new File(gbifTaxaFile).splitEachLine("\\t") { fields ->
			String name = fields[2] + " " + fields[4];
			String kingdom = fields[6];
			String phylum = fields[7];
			String klass = fields[8];
			String order = fields[9];
			String family = fields[10];
			String genus = fields[11];
			String species = fields[12];
			Node taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "kingdom")
			new Node(taxon1, "data", kingdom)
			taxonEntries.add(taxon1)
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "phylum")
			new Node(taxon1, "data", phylum)
			taxonEntries.add(taxon1)
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "class")
			new Node(taxon1, "data", klass)
			taxonEntries.add(taxon1)
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "order")
			new Node(taxon1, "data", order)
			taxonEntries.add(taxon1)
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "family")
			new Node(taxon1, "data", family)
			taxonEntries.add(taxon1)
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "genus")
			new Node(taxon1, "data", genus)
			taxonEntries.add(taxon1)
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1)
			
			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, Classification.findByName(grailsApplication.config.speciesPortal.fields.GBIF_TAXONOMIC_HIERARCHY), "");
			taxonEntries.clear();
		}
	}

	/**
	 * 
	 * @return
	 */
	def loadIBP() {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<Map> content;
		def taxonEntries;

		//ibp
		def sql = Sql.newInstance("jdbc:postgresql://localhost:5432/ibp", "postgres", "postgres123", "org.postgresql.Driver");
		sql.eachRow("select * from birdspecies_list") { row ->
			String name = row.ibp_scientific_name;
			taxonEntries = new ArrayList();

			//iucn hierarchy
			Node taxon1;
			if(row.iucn_2010_family && !row.iucn_2010_family.equalsIgnoreCase("NA") && !row.iucn_2010_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.IUCN_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.iucn_2010_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.IUCN_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);

			//wiki
			if(row.wikipedia_nov2010_kingdom && !row.wikipedia_nov2010_kingdom.equalsIgnoreCase("NA") && !row.wikipedia_nov2010_kingdom.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.WIKIPEDIA_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "kingdom")
				new Node(taxon1, "data", row.wikipedia_nov2010_kingdom)
				taxonEntries.add(taxon1);
			}
			if(row.wikipedia_nov2010_phyllum && !row.wikipedia_nov2010_phyllum.equalsIgnoreCase("NA") && !row.wikipedia_nov2010_phyllum.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.WIKIPEDIA_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "phylum")
				new Node(taxon1, "data", row.wikipedia_nov2010_phyllum)
				taxonEntries.add(taxon1);
			}
			if(row.wikipedia_nov2010_class && !row.wikipedia_nov2010_class.equalsIgnoreCase("NA") && !row.wikipedia_nov2010_class.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.WIKIPEDIA_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "class")
				new Node(taxon1, "data", row.wikipedia_nov2010_class)
				taxonEntries.add(taxon1);
			}
			if(row.wikipedia_nov2010_order && !row.wikipedia_nov2010_order.equalsIgnoreCase("NA")&& !row.wikipedia_nov2010_order.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.WIKIPEDIA_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "order")
				String n = row.wikipedia_nov2010_order.toLowerCase().capitalize();
				new Node(taxon1, "data", n)
				taxonEntries.add(taxon1);
			}
			if(row.wikipedia_nov2010_family && !row.wikipedia_nov2010_family.equalsIgnoreCase("NA")&& !row.wikipedia_nov2010_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.WIKIPEDIA_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.wikipedia_nov2010_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.WIKIPEDIA_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);

			//sibley_monroe_1996
			if(row.sibley_monroe_1996_order && !row.sibley_monroe_1996_order.equalsIgnoreCase("NA")&& !row.sibley_monroe_1996_order.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.SIBLEY_AND_MONROE_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "order")
				String n = row.sibley_monroe_1996_order.toLowerCase().capitalize();
				new Node(taxon1, "data", n)
				taxonEntries.add(taxon1);
			}
			if(row.sibley_monroe_1996_family && !row.sibley_monroe_1996_family.equalsIgnoreCase("NA")&& !row.sibley_monroe_1996_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.SIBLEY_AND_MONROE_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.sibley_monroe_1996_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.SIBLEY_AND_MONROE_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);

			//howardandmoore_3rded
			if(row.howardandmoore_3rded_family && !row.howardandmoore_3rded_family.equalsIgnoreCase("NA")&& !row.howardandmoore_3rded_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.HOWARD_AND_MOORE_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.howardandmoore_3rded_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.HOWARD_AND_MOORE_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);

			//clements_6thed_2009
			if(row.clements_6thed_2009_order && !row.clements_6thed_2009_order.equalsIgnoreCase("NA")&& !row.clements_6thed_2009_order.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.CLEMENTS_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "order")
				String n = row.clements_6thed_2009_order.toLowerCase().capitalize();
				new Node(taxon1, "data", n)
				taxonEntries.add(taxon1);
			}
			if(row.clements_6thed_2009_family && !row.clements_6thed_2009_family.equalsIgnoreCase("NA")&& !row.clements_6thed_2009_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.CLEMENTS_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.clements_6thed_2009_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.CLEMENTS_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);

			//ioc_2009
			if(row.ioc_2009_order && !row.ioc_2009_order.equalsIgnoreCase("NA")&& !row.ioc_2009_order.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.IOC_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "order")
				String n = row.ioc_2009_order.toLowerCase().capitalize();
				new Node(taxon1, "data", n);
				taxonEntries.add(taxon1);
			}
			if(row.ioc_2009_family && !row.ioc_2009_family.equalsIgnoreCase("NA")&& !row.ioc_2009_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.IOC_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.ioc_2009_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.IOC_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);

			//ebird
			if(row.ebird_2010_order && !row.ebird_2010_order.equalsIgnoreCase("NA")&& !row.ebird_2010_order.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.EBIRD_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "order")
				String n = row.ebird_2010_order.toLowerCase().capitalize();
				new Node(taxon1, "data", n)
				taxonEntries.add(taxon1);
			}
			if(row.ebird_2010_family && !row.ebird_2010_family.equalsIgnoreCase("NA")&& !row.ebird_2010_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.EBIRD_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.ebird_2010_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.EBIRD_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);

			//obc
			if(row.obc_2001_order && !row.obc_2001_order.equalsIgnoreCase("NA")&& !row.obc_2001_order.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.OBC_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "order")
				String n = row.obc_2001_order.toLowerCase().capitalize();
				new Node(taxon1, "data", n)
				taxonEntries.add(taxon1);
			}
			if(row.obc_2001_family && !row.obc_2001_family.equalsIgnoreCase("NA")&& !row.obc_2001_family.equalsIgnoreCase("null")) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.OBC_TAXONOMIC_HIERARCHY)
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", row.obc_2001_family)
				taxonEntries.add(taxon1);
			}
			taxon1 = builder.createNode("field");
			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.OBC_TAXONOMIC_HIERARCHY)
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name)
			taxonEntries.add(taxon1);
			List<TaxonomyRegistry> registry = converter.getClassifications(taxonEntries, name);

			def taxonConcept = converter.getTaxonConcept(registry,  Classification.findByName(grailsApplication.config.speciesPortal.fields.IUCN_TAXONOMIC_HIERARCHY));

			//synonyms
			HashSet synonyms = new HashSet();
			synonyms.add(row.itis_scientific_name);
			synonyms.add(row.iucn_2010_scientific_name);
			synonyms.add(row.wikipedia_nov2010_scientific_name);
			synonyms.add(row.sibley_monroe_1996_scientific_name);
			synonyms.add(row.howardandmoore_3rded_scientific_name);
			synonyms.add(row.clements_5thed_2005_scientific_name);
			synonyms.add(row.clements_6thed_2009_scientific_name);
			synonyms.add(row.ioc_2009_scientific_name);
			synonyms.add(row.ebird_2010_scientific_name);
			synonyms.add(row.obc_2001_scientific_name);
			Node synonymsNode = builder.createNode("field");
			synonyms.each { syn ->
				if(syn && !syn.equalsIgnoreCase("NA"))
					new Node(synonymsNode, "data", syn.trim());
			}
			converter.createSynonyms(synonymsNode, taxonConcept);

			//commonnames
			HashSet commonNames = new HashSet();
			if(row.ibp_common_name) commonNames.addAll(row.ibp_common_name.tokenize(","));
			if(row.itis_common_name) commonNames.addAll(row.itis_common_name.tokenize(","));
			if(row.iucn_2010_common_names) commonNames.addAll(row.iucn_2010_common_names.tokenize(","));
			if(row.wikipedia_nov2010_common_name) commonNames.addAll(row.wikipedia_nov2010_common_name.tokenize(",") );
			if(row.sibley_monroe_1996_common_name) commonNames.addAll(row.sibley_monroe_1996_common_name.tokenize(","));
			if(row.howardandmoore_3rded_common_name) commonNames.addAll(row.howardandmoore_3rded_common_name.tokenize(","));
			if(row. clements_6thed_2009_common_name) commonNames.addAll(row. clements_6thed_2009_common_name.tokenize(","));
			if(row.ioc_2009_common_name) commonNames.addAll(row.ioc_2009_common_name.tokenize(","));
			if(row.ebird_2010_common_name) commonNames.addAll(row.ebird_2010_common_name.tokenize(","));
			if(row.obc_2001_common_names) commonNames.addAll(row.obc_2001_common_names.tokenize(","));
			Node commonNameNode = builder.createNode("field");
			commonNames.each { commonName ->
				if(commonName && !commonName.equalsIgnoreCase("NA")) {
					new Node(commonNameNode, "data", commonName.trim());
				}
			}
			converter.createCommonNames(commonNameNode, taxonConcept);
		}

	}

	/**
	 * 
	 */
	private List<TaxonomyRegistry> saveTaxonEntries(converter, List taxonEntries, Classification c, String name) {
		List<TaxonomyRegistry> registry = converter.getTaxonHierarchy(new NodeList(taxonEntries), c, name);
		cleanUpGorm();
		//registry.each { e ->
		//	e.save(flush:true);
		//	e.errors.each { println it }
		//}
		return registry;
	}

	/**
	 * Creates stub species pages for all taxonconcepts without one
	 */
	def createSpeciesStubs() {
		def taxonConcepts = TaxonomyDefinition.findAll("from TaxonomyDefinition as taxonomyDefinition where taxonomyDefinition.rank = :speciesTaxonRank and not (taxonomyDefinition.id) in (select s.taxonConcept from Species as s)",[speciesTaxonRank:TaxonomyRank.SPECIES.ordinal()]);
		taxonConcepts.eachWithIndex { taxonConcept, index ->
			speciesService.createSpeciesStub(taxonConcept);
			if(index % BATCH_SIZE == 0) {
				cleanUpGorm();
			}
		}
		cleanUpGorm();
	}

	/**
	 *
	 */
	private void cleanUpGorm() {
		def hibSession = sessionFactory?.getCurrentSession()
		if(hibSession) {
			log.debug "Flushing and clearing session"
			try {
				hibSession.flush()
			} catch(ConstraintViolationException e) {
				e.printStackTrace()
			}
			hibSession.clear()
		}
	}
}
