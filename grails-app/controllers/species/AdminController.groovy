package species

import org.hibernate.exception.ConstraintViolationException;

import species.participation.Recommendation;
import speciespage.GroupHandlerService;
import speciespage.NamesLoaderService;

import groovy.sql.GroovyRowResult;
import groovy.sql.Sql;

import species.formatReader.SpreadsheetReader;
import species.sourcehandler.XMLConverter;
import species.Classification;
import species.TaxonomyRegistry

class AdminController {

    
	
	def searchService;
	def namesLoaderService;
	def namesIndexerService;
	def groupHandlerService;
	def sessionFactory;
	
	def index = {
		redirect (action:"names");
	}
	
	def names = {
	
	}
	
	def setup = {
		def dataLoader = new DataLoader();
		
		dataLoader.uploadFields(grailsApplication.config.speciesPortal.data.rootDir+"/templates/DefinitionsAbridged_prabha.xlsx");
		dataLoader.uploadLanguages(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Language_iso639-2.csv");
		dataLoader.uploadCountries(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Countries_ISO-3166-1.csv");
		dataLoader.uploadClassifications(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Classifications.xlsx", 0, 0);
		
		def allGroup = new SpeciesGroup(name:"All");
		allGroup.save(flush:true, failOnError:true);
		def othersGroup = new SpeciesGroup(name:"Others", parentGroup:allGroup);
		othersGroup.save(flush:true, failOnError:true);
		groupHandlerService.loadGroups(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Groups.xlsx", 0, 0);
	}
	
	def loadData = {
		def dataLoader = new DataLoader();
		
		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/mango/mango";
		//dataLoader.uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/mango/mango/MangoMangifera_indica_prabha_v4 (copy).xlsx", 0, 0, 1, 4);
		//
		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/grey_falcolin";
		//dataLoader.uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespageszip/grey_falcolin/GreyFrancolin_v4.xlsx", 0, 0, 1, 4);
		
		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Rufous Woodpecker/images";
		dataLoader.uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Rufous Woodpecker/RufousWoodepecker_v4_1.xlsm");
		
		grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Eurasian Curlew/png ec";
		dataLoader.uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Eurasian Curlew/EurasianCurlew_v4_2.xlsm");
		
		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		//dataLoader.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Dung_beetle_Species_pages_IBP_v13.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/dungbeetles_mapping.xlsx", 0, 0, 0, 0);
		//
		//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		//dataLoader.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Trees_descriptives_prabha_final_6.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/ifp_tree_mapping_v2.xlsx", 0, 0, 0, 2);
		//
		////grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
		//dataLoader.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Bats/WG_bats_account_01Nov11_sanjayMolur.xls", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/WG_bats_account_01Nov11_sanjayMolurspecies_mapping_v2.xlsx", 0, 0, 0, 0);
		
	}
	
	def loadNames = {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();
		
		/*
		 //flowersOfIndia
		 List<Map> content = SpreadsheetReader.readSpreadSheet(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/FlowersByBotanicalNames.xls", 0, 0);
		 for (Map row : content) {
		 String name = row.get("botanical name");
		 String synonyms = row.get("synonyms")
		 String family = row.get("family")
		 String commonName = row.get("common name")
		 Node taxon1 = builder.createNode("field");
		 new Node(taxon1, "subcategory", "family")
		 new Node(taxon1, "data", family)
		 Node taxon2 = builder.createNode("field");
		 new Node(taxon2, "subcategory", "species")
		 new Node(taxon2, "data", name)
		 List taxonEntries = new ArrayList();
		 taxonEntries.add(taxon1);
		 taxonEntries.add(taxon2);
		 def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.FLOWERS_OF_INDIA_TAXONOMIC_HIERARCHY)
		 List<TaxonomyRegistry> registry = converter.getTaxonHierarchy(new NodeList(taxonEntries), c, name);
		 registry.each { e ->
		 e.save(flush:true);
		 e.errors.each { println it }
		 }
		 def taxonConcept = converter.getTaxonConcept(registry, c);
		 //synonyms
		 Node synonymsNode = builder.createNode("field");
		 synonyms.split(',').each { syn ->
		 if(syn) {
		 new Node(synonymsNode, "data", syn.trim());
		 }
		 }
		 converter.createSynonyms(synonymsNode, taxonConcept);
		 //commonnames
		 Node commonNameNode = builder.createNode("field");
		 if(commonName) {
		 new Node(commonNameNode, "data", commonName.trim());
		 converter.createCommonNames(commonNameNode, taxonConcept);
		 }
		 }
		 */
		
		/*
		 //gbif
		 String gbifTaxaFile = grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/GBIF taxonomy-search-13208373774487451330519969730577/taxonomy-search-1320837377448.txt"
		 int i=0;
		 List taxonEntries = new ArrayList();
		 new File(gbifTaxaFile).splitEachLine("\\t") { fields ->
		 if(i>0) {
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
		 }
		 i++;
		 }
		 List<TaxonomyRegistry> registry = converter.getTaxonHierarchy(new NodeList(taxonEntries), Classification.findByName(grailsApplication.config.speciesPortal.fields.GBIF_TAXONOMIC_HIERARCHY), "");
		 registry.each { e ->
		 e.save(flush:true);
		 e.errors.each { println it }
		 }
		 */
		
		/*
		 //eflora
		 List<Map> content = SpreadsheetReader.readSpreadSheet(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/eflora_data_CN.xlsx", 0, 0);
		 for (Map row : content) {
		 String name = row.get("scientific_names");
		 String synonyms = row.get("synonyms cleaned")
		 String family = row.get("family")
		 String commonName = row.get("common_names with separator")
		 List taxonEntries = new ArrayList();
		 Node taxon1 = builder.createNode("field");
		 new Node(taxon1, "subcategory", "family")
		 new Node(taxon1, "data", family)
		 taxonEntries.add(taxon1);
		 taxon1 = builder.createNode("field");
		 new Node(taxon1, "subcategory", "species")
		 new Node(taxon1, "data", name)
		 taxonEntries.add(taxon1);
		 def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.EFLORA_TAXONOMIC_HIERARCHY)
		 List<TaxonomyRegistry> registry = converter.getTaxonHierarchy(new NodeList(taxonEntries), c, name);
		 registry.each { e ->
		 e.save(flush:true);
		 e.errors.each { println it }
		 }
		 def taxonConcept = converter.getTaxonConcept(registry, c);
		 //synonyms
		 Node synonymsNode = builder.createNode("field");
		 synonyms.split(',').each { syn ->
		 if(syn) {
		 println syn;
		 new Node(synonymsNode, "data", syn.trim());
		 }
		 }
		 converter.createSynonyms(synonymsNode, taxonConcept);
		 //commonnames
		 Node commonNameNode = builder.createNode("field");
		 commonName.split(';').each { part ->
		 if(part) {
		 String[] commonNames = part.split(":");
		 if(commonNames.length == 2) {
		 commonNames[1].split(",").each {
		 Node data = new Node(commonNameNode, "data", it);
		 Node language = new Node(data, "language");
		 new Node(language, "name", commonNames[0]);
		 }
		 } else {
		 commonNames[0].split(",").each {
		 Node data = new Node(commonNameNode, "data", it);
		 }
		 }
		 }
		 converter.createCommonNames(commonNameNode, taxonConcept);
		 }
		 }
		 */
		
		/*
		 //fishbase
		 List<Map> content = SpreadsheetReader.readSpreadSheet(grailsApplication.config.speciesPortal.data.rootDir+"/dictionaries/fishbase_22_11_2011.xls", 0, 0);
		 for (Map row : content) {
		 String name = row.get("species");
		 String author = row.get("author");
		 String family = row.get("family")
		 String commonName = row.get("common names")
		 List taxonEntries = new ArrayList();
		 Node taxon1 = builder.createNode("field");
		 new Node(taxon1, "subcategory", "family")
		 new Node(taxon1, "data", family)
		 taxonEntries.add(taxon1);
		 taxon1 = builder.createNode("field");
		 new Node(taxon1, "subcategory", "species")
		 new Node(taxon1, "data", name+" "+author)
		 taxonEntries.add(taxon1);
		 def c = Classification.findByName(grailsApplication.config.speciesPortal.fields.FISHBASE_TAXONOMIC_HIERARCHY)
		 List<TaxonomyRegistry> registry = converter.getTaxonHierarchy(new NodeList(taxonEntries), c, name);
		 registry.each { e ->
		 e.save(flush:true);
		 e.errors.each { println it }
		 }
		 def taxonConcept = converter.getTaxonConcept(registry, c);
		 //commonnames
		 Node commonNameNode = builder.createNode("field");
		 commonName.split(',').each { part ->
		 if(part) {
		 String[] commonNames = part.split("\\(");
		 if(commonNames.length == 2) {
		 commonNames[0].split(",").each {
		 Node data = new Node(commonNameNode, "data", it);
		 Node language = new Node(data, "language");
		 new Node(language, "name", commonNames[1].replace("\\)",""));
		 }
		 } else {
		 commonNames[0].split(",").each {
		 Node data = new Node(commonNameNode, "data", it);
		 }
		 }
		 }
		 converter.createCommonNames(commonNameNode, taxonConcept);
		 }
		 }
		 */
		
		/*
		//ibp
		def sql = Sql.newInstance("jdbc:postgresql://localhost:5432/ibp", "postgres", "postgres123", "org.postgresql.Driver");
		
		sql.eachRow("select * from birdspecies_list") { row ->
			String name = row.ibp_scientific_name;
		
			List taxonEntries = new ArrayList();
		
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
			registry.each { e ->
				e.save(flush:true);
				e.errors.each { println it }
			}
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
				converter.createCommonNames(commonNameNode, taxonConcept);
			}
		
		}
		*/
	}
	
	def reloadNames = {
		try {
			log.debug "Reloading all names into recommendations"			
			namesLoaderService.syncNamesAndRecos(true);
			flash.message = "Successfully loaded all names into recommendations"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		
		redirect(action: "names")
	}
	
	def reloadSearchIndex = {
		try {
			searchService.publishSearchIndex(Species.list());
			flash.message = "Successfully created search index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "names")
	}
	
	def reloadNamesIndex = {
		try {
			namesIndexerService.rebuild();
			flash.message = "Successfully created names index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "names")
	}
	
	def updateGroups = {
		try {
			groupHandlerService.loadGroups(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Groups.xlsx", 0, 0);
			flash.message = "Successfully updated all taxonconcept group associations"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "names")
	}
}
