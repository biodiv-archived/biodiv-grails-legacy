package speciespage

import groovy.sql.Sql

import org.hibernate.exception.ConstraintViolationException

import species.Classification
import species.Species;
import species.TaxonomyDefinition;
import species.ScientificName.TaxonomyRank;
import species.TaxonomyRegistry
import species.formatReader.SpreadsheetReader
import species.sourcehandler.XMLConverter
import species.auth.SUser;
import grails.converters.JSON;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import org.springframework.web.context.request.RequestContextHolder
import species.Language;
import species.NamesMetadata.NamePosition

class TaxonService {

	static transactional = false

	def grailsApplication
	def sessionFactory
	def groupHandlerService;
	def namesLoaderService;
	def speciesService;
	def externalLinksService;
    def springSecurityService;
    def activityFeedService;
    def speciesPermissionService;
    def messageSource;
    def request;
	static int BATCH_SIZE = 100;

	/**
	 * 
	 * @return
	 */
	def loadTaxon(boolean createSpeciesStubsFlag) {
		log.info("Loading taxon information");
				loadFlowersOfIndia(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/FlowersByBotanicalNames.xls", 0, 0);
				loadFishBase(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/fishbase_30_11_2011.xls", 0, 0);
				loadGBIF(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/GBIF taxonomy-search-13208373774487451330519969730577/taxonomy-search-1320837377448.txt");
				loadEFlora(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/eflora_data_CN.xlsx", 0, 0);
//				loadIBP("jdbc:postgresql://localhost:5432/ibp", "postgres", "postgres123", "org.postgresql.Driver");
				loadIUCNRedList(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/IUCNRedList-India-12-01-2012.xlsx", 0, 0);
				loadKeystone(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/Keystone_v1.xls", 0, 0);
				cleanUpGorm();
		
		//		groupHandlerService.updateGroups();
		//		namesLoaderService.syncNamesAndRecos(false);

				if(createSpeciesStubsFlag) {
					createSpeciesStubs();
				}
//				externalLinksService.updateExternalLinks();
	}

	/**
	 * 
	 * @return
	 */
	def loadFlowersOfIndia(String file, int sheetNo, int headerRowNo) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<Map> content = SpreadsheetReader.readSpreadSheet(file, sheetNo, headerRowNo);
		def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)
		
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

			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);

			def taxonConcept = converter.getTaxonConcept(registry, c);
			if(!taxonConcept.isAttached()) {
				taxonConcept.attach();
			}
			
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
			cleanUpGorm();
		}

	}

	/**
	 * 
	 * @return
	 */
	def loadEFlora(String file, int sheetNo, int headerRowNo) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<Map> content = SpreadsheetReader.readSpreadSheet(file, sheetNo, headerRowNo);
		def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)
		
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

			
			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);

			def taxonConcept = converter.getTaxonConcept(registry, c);
			if(!taxonConcept.isAttached()) {
				taxonConcept.attach();
			}
			
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
			cleanUpGorm();
		}
	}

	/**
	 * 
	 * @return
	 */
	def loadFishBase(String file, int sheetNo, int headerRowNo) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		int i=0;
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, sheetNo, headerRowNo);
		def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.FISHBASE_TAXONOMIC_HIERARCHY)
		for (Map row : content) {
			String name = row.get("species");
			String commonName = row.get("common names")
			
			def taxonEntries = getTaxonNodes(builder, row);
			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);
			
			def taxonConcept = converter.getTaxonConcept(registry, c);
			if(!taxonConcept.isAttached()) {
				taxonConcept.attach();
			}
			
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

	private List getTaxonNodes(NodeBuilder builder, Map row) {
		String name = row.get("species") +" "+ (row.get("author")?:"");
		String kingdom = row.get("kingdom").toLowerCase().capitalize()
		String phylum = row.get("phylum").toLowerCase().capitalize()
		String klass = row.get("class").toLowerCase().capitalize()
		String order = row.get("order").toLowerCase().capitalize()
		String family = row.get("family").toLowerCase().capitalize()
		String genus = row.get("genus").toLowerCase().capitalize()
		

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

		if(genus) {
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "family")
			new Node(taxon1, "data", genus)
			taxonEntries.add(taxon1);
		}
		
		if(name) {
			taxon1 = builder.createNode("field");
			new Node(taxon1, "subcategory", "species")
			new Node(taxon1, "data", name.trim())
			taxonEntries.add(taxon1);
		}

		
		return taxonEntries;

	}
	/**
	 * 
	 * @return
	 */
	def loadGBIF(String gbifTaxaFile) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		
		Classification c = Classification.findByName(grailsApplication.config.speciesPortal.fields.GBIF_TAXONOMIC_HIERARCHY);
		
		List taxonEntries = new ArrayList();
		int i=0;
		new File(gbifTaxaFile).splitEachLine("\\t") { fields ->
			if(i++>0) {
			String name = fields[12] + " " + fields[4];
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

			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, "");
			taxonEntries.clear();
			
			def taxonConcept = converter.getTaxonConcept(registry, c);
			if(!taxonConcept.isAttached()) {
				taxonConcept.attach();
			}
			
			// populating gbif id
			if(fields[13]) {
				externalLinksService.updateExternalLink(taxonConcept, "gbif", (Double.parseDouble(fields[13]).intValue()).toString(), false);
			}
			
			taxonConcept = taxonConcept.merge();
			if(!taxonConcept.save()) {
				taxonConcept.errors.each { log.error it}
			}
			cleanUpGorm();
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	def loadIBP(String dbUrl, String userName, String password, String driverClass) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		def taxonEntries;

		//ibp
		def sql = Sql.newInstance(dbUrl, userName, password, driverClass);
		sql.eachRow("select * from birdspecies_list") { row ->
			String name = row.ibp_scientific_name;
			taxonEntries = new ArrayList();

//			//iucn hierarchy
//			Node taxon1;
//			if(row.iucn_2010_family && !row.iucn_2010_family.equalsIgnoreCase("NA") && !row.iucn_2010_family.equalsIgnoreCase("null")) {
//				taxon1 = builder.createNode("field");
//				new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.IUCN_TAXONOMIC_HIERARCHY)
//				new Node(taxon1, "subcategory", "family")
//				new Node(taxon1, "data", row.iucn_2010_family)
//				taxonEntries.add(taxon1);
//			}
//			taxon1 = builder.createNode("field");
//			new Node(taxon1, "category", grailsApplication.config.speciesPortal.fields.IUCN_TAXONOMIC_HIERARCHY)
//			new Node(taxon1, "subcategory", "species")
//			new Node(taxon1, "data", name)
//			taxonEntries.add(taxon1);

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
			if(!taxonConcept.isAttached()) {
				taxonConcept.attach();
			}

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
			cleanUpGorm();
		}

	}

	/**
	 *
	 * @return
	 */
	def loadIUCNRedList(String file, int sheetNo, int headerRowNo) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		List<TaxonomyDefinition> taxonEntries = [];
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, sheetNo, headerRowNo);

		Classification c = Classification.findByName(grailsApplication.config.speciesPortal.fields.IUCN_TAXONOMIC_HIERARCHY);
		
		for (Map row : content) {
			
			String name = row.get("genus")+" "+row.get("species")+" "+row.get("authority")+" "+row.get("infraspecific rank")+" "+row.get("infraspecific name")+" "+row.get("infraspecific authority");
			String kingdom = row.get("kingdom").toLowerCase().capitalize();
			String phylum = row.get("phylum").toLowerCase().capitalize();
			String klass = row.get("class").toLowerCase().capitalize();
			String order = row.get("order").toLowerCase().capitalize();
			String family = row.get("family").toLowerCase().capitalize();
			String genus = row.get("genus");
			
			Node taxon1;
			if(kingdom) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "kingdom")
				new Node(taxon1, "data", kingdom)
				taxonEntries.add(taxon1)
			}

			if(phylum) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "phylum")
				new Node(taxon1, "data", phylum)
				taxonEntries.add(taxon1)
			}

			if(klass) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "class")
				new Node(taxon1, "data", klass)
				taxonEntries.add(taxon1)
			}

			if(order) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "order")
				new Node(taxon1, "data", order)
				taxonEntries.add(taxon1)
			}

			if(family) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "family")
				new Node(taxon1, "data", family)
				taxonEntries.add(taxon1)
			}

			if(genus) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "genus")
				new Node(taxon1, "data", genus)
				taxonEntries.add(taxon1)
			}

			if(name) {
				taxon1 = builder.createNode("field");
				new Node(taxon1, "subcategory", "species")
				new Node(taxon1, "data", name)
				taxonEntries.add(taxon1)
			}

			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);
			taxonEntries.clear();

			def taxonConcept = converter.getTaxonConcept(registry, c);
			if(!taxonConcept.isAttached()) {
				taxonConcept.attach();
			}
			
			// populating threatened status 
			if(row.get("red list status")) {
				taxonConcept.threatenedStatus = row.get("red list status");
				log.debug "Setting threatened status to "+taxonConcept.threatenedStatus
//				if(!taxonConcept.save()) {
//					taxonConcept.errors.each { log.error it}
//				}
			}
			
			// populating iucn id
			if(row.get("species id")) {
				externalLinksService.updateExternalLink(taxonConcept, "iucn", (Double.parseDouble(row.get("species id")).intValue()).toString(), false);
			}
			
			taxonConcept = taxonConcept.merge();
			if(!taxonConcept.save()) {
				taxonConcept.errors.each { log.error it}	
			}
			
			//synonyms
			String synonyms = row.get("Synonyms")
			if(synonyms) {
				Node synonymsNode = builder.createNode("field");
				synonyms.tokenize('|').each { syn ->
					new Node(synonymsNode, "data", syn.trim());
				}
				converter.createSynonyms(synonymsNode, taxonConcept);
			}


			//commonnames
			String commonNames = row.get("Common names (Eng)")
			if(commonNames) {
				Node commonNameNode = builder.createNode("field");
				commonNames.tokenize(',').each { commonName ->
					Node data = new Node(commonNameNode, "data", commonName.trim());
					Node language = new Node(data, "language");
					new Node(language, "name", "English");
				}
				converter.createCommonNames(commonNameNode, taxonConcept);
			}
			
			cleanUpGorm();
			
			
		}
	}
	
		/**
	 * 
	 * @return
	 */
	def loadKeystone(String file, int sheetNo, int headerRowNo) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();

		int i=0;
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, sheetNo, headerRowNo);
		def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY)
		for (Map row : content) {
			String name = row.get("species");
			def taxonEntries = getTaxonNodes(builder, row);
			List<TaxonomyRegistry> registry = saveTaxonEntries(converter, taxonEntries, c, name);
			cleanUpGorm();
		}

	}

    /**
    *
    */

    def addTaxonHierarchy(String speciesName, List taxonRegistryNames, Classification classification, SUser contributor, Language language, boolean abortOnNewName = false ,boolean fromCOL = false , otherParams = null) {
    	 if(request == null) request = RequestContextHolder.currentRequestAttributes().request
        List errors = [];
        if(!classification) {
        	def messagesourcearg = new Object[1];
                 messagesourcearg[0] =classification?.name;
            return [success:false, msg:messageSource.getMessage("info.not.valid", messagesourcearg, RCU.getLocale(request))]
        }
        
        XMLConverter converter = new XMLConverter();
        def taxonRegistryNodes = converter.createTaxonRegistryNodes(taxonRegistryNames, classification.name, contributor, language);
        println "======YAHAN HAI=========";
        List<TaxonomyRegistry> taxonRegistry = converter.getClassifications(taxonRegistryNodes, speciesName, true, abortOnNewName, fromCOL, otherParams);
/*        //check if user has permission to contribute to the taxon hierarchy
        if(speciesPermissionService.isTaxonContributor(taxonRegistry, contributor)) {
            taxonRegistry = converter.getClassifications(taxonRegistryNodes, speciesName, true);            
        } else {
            return ['success':false, code:'requirePermission', msg:"Sorry, you dont have persmission to edit taxon registry nodes ${taxonRegistry}"]
        }
*/
        if(taxonRegistry) {
            int maxRank = 0;
            TaxonomyRegistry reg;
            String hier = ""
            taxonRegistry.each { 
                if(it.errors.getErrorCount() > 0)
                    errors.addAll(it.errors.collect {it.toString()})
                if(it.taxonDefinition.rank > maxRank) {
                    reg = it;
                    maxRank = it.taxonDefinition.rank;
                }
                hier += it.taxonDefinition.name +" > "
            }
            def res = ['success':true, msg:messageSource.getMessage("info.success.added.hierarchy", null, RCU.getLocale(request)), activityType:activityFeedService.SPECIES_HIERARCHY_CREATED+" : "+hier, 'reg' : reg, errors:errors]
            if(!(taxonRegistry[-1].taxonDefinition.status)) {
                res['newlyCreated'] = true
                res['newlyCreatedName'] = taxonRegistry[-1].taxonDefinition.name
            }
            return res   
        }
        return ['success':false, msg:messageSource.getMessage("info.error.adding.hierarchy", null, RCU.getLocale(request)), errors:errors]
    }

    def deleteTaxonHierarchy(TaxonomyRegistry reg, boolean force = false) {
        return deleteTaxonEntries(reg, force);
    } 

    private def deleteTaxonEntries(TaxonomyRegistry reg, boolean force = false) {
    	 if(request == null) request = RequestContextHolder.currentRequestAttributes().request
        String msg = '';
        def content;
        List errors = [];
        Map r = [errors:errors];

        List toDelete = [];

        if(!reg) {
            return [success:false, msg:"Taxonomy registry is null", errors:errors]
         } 
        /* JUST FOR DEMO
        if(!reg.isContributor()) {
            return [success:false, msg:messageSource.getMessage("info.no.delete.permission", null, RCU.getLocale(request)), errors:errors]
        }
        */
        def otherHierarchiesCount = TaxonomyRegistry.withCriteria {
            projections {
                count('id')
            }
            taxonDefinition {
                eq('id', reg.taxonDefinition.id)
            }
            like('path', '%_'+reg.taxonDefinition.id)
        }

        if(force == false && otherHierarchiesCount[0] == 1) {
            //if this is the only hierarchy for the species ... then dont delete it.
            return [success:false, msg:messageSource.getMessage("info.annot.remove.hierarchy", null, RCU.getLocale(request)), errors:errors]
        }

        try {
            if(reg) {
                String hier = "";
                def contributor = springSecurityService.currentUser;
                while(reg != null) {
                    def c = TaxonomyRegistry.withCriteria () {
                        projections {
                            count('id')
                        }
                        eq('parentTaxon', reg)
                        contributors {
                            eq('id', contributor.id)
                        }
                    }
                    if(c[0] > 1) {
                        //there is another hierarchy sharing same nodes and with same contributor.
                        // so leaving this portion untouched
                        r.success = true;
                        r.msg = 'Successfully removed registry';
                        r.errors << errors
                        break;
                    }
                    //reg.removeFromContributors(contributor);

                    toDelete << reg;
                    hier += reg.taxonDefinition.name +" > "
                        //reg.delete(failOnError:true)
                    reg = reg.parentTaxon;

                } 
                
                int maxRank = 0, regId;
                TaxonomyRegistry.withTransaction { status ->
                    toDelete.each { r2 ->

                        if(r2.taxonDefinition.rank > maxRank) {
                            regId = r2.id;
                            maxRank = r2.taxonDefinition.rank;
                        }

                        r2.removeFromContributors(contributor);

                        if(r2.contributors.size() == 0) {
                            r2.delete(failOnError:true)
                        } else if(!r2.save()) {
                            r2.errors.each { errors << it; log.error it }
                            r.success = false;
                            r.msg = "Error while deleting registry ${r2.path}"
                            r.errors << errors
                            throw new RuntimeException(r.msg + r.errors)
                            return r;
                        } 
                    }
                }
                r.success = true;
                r.msg = 'Successfully removed registry';
                r.activityType = activityFeedService.SPECIES_HIERARCHY_DELETED+" : "+hier;
                r.errors << errors
                r.regId = regId
                return r;
            } else {
                return r;
            }

        } catch(e) {
            e.printStackTrace();
            log.error e.getMessage();
            r.success = false;
            r.msg = "Error while deleting registry ${e.getMessage()}"
            r.errors = errors
            return r;
        }

    }

    List getTaxonHierarchyList(Map list) {
        List t = [];

        list.each { key, value ->
            if(value) {
                int rank = Integer.parseInt(key).intValue();
                t.putAt(rank, value.trim().capitalize());
            }
        }
        return t;
    }

    /**
	 * 
	 */
	private List<TaxonomyRegistry> saveTaxonEntries(converter, List taxonEntries, Classification c, String name) {
		List<TaxonomyRegistry> registry = converter.getTaxonHierarchy(new NodeList(taxonEntries), c, name);
		//cleanUpGorm();
		//registry.each { e ->
		//	e.save(flush:true);
		//	e.errors.each { println it }
		//}
		if(!c.isAttached()) {
			c.attach();
		}
		return registry;
	}

	/**
	 * Creates stub species pages for all taxonconcepts without one
	 */
	def createSpeciesStubs() {
		log.info "Creating stubs for species stubs for all taxon concepts with out one"
		int notOfStubs = 0;

		//TODO: hanging when result is null
//		def taxonConcepts = TaxonomyDefinition.findAll(
//			"from TaxonomyDefinition as taxonomyDefinition left outer join Species as s on s.taxonConcept = taxonomyDefinition where taxonomyDefinition.rank = :speciesTaxonRank and s.id is null",[speciesTaxonRank:TaxonomyRank.SPECIES.ordinal()]); 
		//TaxonomyDefinition.executeQuery("select * from taxonomy_definition t left outer join species s on s.taxon_concept_id = t.id where s.id is null and t.rank = :speciesTaxonRank", [speciesTaxonRank:TaxonomyRank.SPECIES.ordinal()]);
		def taxonConcepts = TaxonomyDefinition.findAll("from TaxonomyDefinition as taxonomyDefinition where taxonomyDefinition.rank = :speciesTaxonRank and not (taxonomyDefinition.id) in (select s.taxonConcept from Species as s)",[speciesTaxonRank:TaxonomyRank.SPECIES.ordinal()]);
		
		List<Species> s = [];
		taxonConcepts.eachWithIndex { taxonConcept, index ->
			def species = speciesService.createSpeciesStub(taxonConcept);

			Species existingSpecies = Species.findByTaxonConcept(taxonConcept);
			if(!existingSpecies) {
				s.add(species);
			}

			if(s.size() % BATCH_SIZE == 0) {
				notOfStubs += speciesService.saveSpecies(s);
				s.clear();
			}
		}

		if(s) {
			notOfStubs += speciesService.saveSpecies(s);
			s.clear();
		}
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

    boolean validateHierarchy(List<String> taxonEntries) {
        for (int i=0; i< taxonEntries.size(); i++) {
            if(TaxonomyRank.list()[i] != TaxonomyRank.SUB_FAMILY && TaxonomyRank.list()[i] != TaxonomyRank.SUB_GENUS && TaxonomyRank.list()[i] != TaxonomyRank.SUPER_FAMILY) {
                if(!taxonEntries[TaxonomyRank.list()[i].ordinal()]) {
                    log.debug "${TaxonomyRank.list()[i]} is missing" 
                    return false;
                }
            }
        }
        return true;
    }
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////Navigator query related ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	
	List getNodeChildren(params){
		
		int level = params.level?.toInteger()
		int limit = params.limit?(params.limit.toInteger()):10
		long offset = params.offset?(params.offset.toLong):0
		
		TaxonomyRegistry tr = getTaxonReg(params)
		if(!tr){
			log.error "No taxon registry found for params " + params
			return
		}
		
		String searchablePrefix = tr.path + "_" +"%" 
		
		return TaxonomyRegistry.createCriteria().list(max:limit, offset:offset){
			projections {
				property("taxonDefinition")
			}
			and{
				like('path', searchablePrefix)
				if(level){
					eq('level', level)
				}
			}
			order 'id', 'asc'
		}
	}
	
	TaxonomyRegistry getTaxonRegParent(params){
		return getTaxonReg(params)?.parentTaxon.taxonDefinition
	}
	
	private TaxonomyRegistry getTaxonReg(params){
		long taxonId = params.taxonId.toLong()
		long classificationId = params.classificationId.toLong()
		return TaxonomyRegistry.findByTaxonDefinitionAndClassification(TaxonomyDefinition.read(taxonId), Classification.read(classificationId))
	}
	
	def addLevelToTaxonReg(){
		int i = 0
		TaxonomyRegistry.withTransaction { status ->
			TaxonomyRegistry.list().each { tr ->
				tr.level = tr.path.split("_").size()
				tr.save()
				i++
				if(i%100 == 0)
					println i
			}
		}
	}

    def moveToWKG(taxonRegistry) {
        def lastTaxonReg = taxonRegistry[-1]
        def taxonDef = lastTaxonReg.taxonDefinition
        if(!taxonDef.status) {
            return false
        }
        def classification = lastTaxonReg.classification
        def taxonsMap = taxonDef.parentTaxonRegistry(classification)
        println "=======TAXONS MAP====== " + taxonsMap.get(classification)
        def taxons = taxonsMap.get(classification);
        taxons.add(taxonDef);
        taxons.each {
            if(it.position == NamePosition.DIRTY) {
                println "=====CHANGING POSITION to WORKING ====="
                it.position = NamePosition.WORKING
            }
            if(!it.save()) {
                it.errors.each { log.error it}
            } 
        }
        return true;
    }
}
