import org.codehaus.groovy.grails.commons.ApplicationHolder;

import speciespage.SpeciesUploadService;
import speciespage.SetupService;
import species.Species;
import species.*;
import species.sourcehandler.XMLConverter;
import java.util.regex.Pattern

//def s = ctx.getBean("setupService");
//s.uploadHabitats();
def speciesUploadService = ctx.getBean("speciesUploadService");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/mango";
//speciesUploadService.uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/mango/MangoMangifera_indica_prabha2.xlsx", 0, 0, 1, 4);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/grey_falcolin";
//speciesUploadService.uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/grey_falcolin/GreyFrancolin.xlsx", 0, 0, 1, 4);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/Rufous Woodpecker/images";
//speciesUploadService.uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/Rufous Woodpecker/RufousWoodepecker.xlsm");
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/Eurasian Curlew/png ec";
//speciesUploadService.uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ibp/1.0/Eurasian Curlew/EurasianCurlew.xlsm");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/atree/1.0/Dung_beetle/images";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/atree/1.0/Dung_beetle/Dung_beetle_Species_pages_IBP.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/dungbeetles_mapping.xlsx", 0, 0, 0, 0, 1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ifp/1.0/images";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ifp/1.0/Trees_descriptives.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/ifp_tree_mapping_v2.xlsx", 0, 0, 0, 0);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.0/WesternGhatsBats";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.0/WG_bats_account_01Nov11_sanjayMolur.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.0/WG_bats_account_01Nov11_sanjayMolur_mapping.xlsx", 0, 0, 0, 0,1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/careearth/1.0";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/careearth/1.0/species accounts188.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/careearth/1.0/speciesaccount188_mapping.xlsx", 0, 0, 0, 0, 1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/keystone/1.0";
//String mappingFile = grailsApplication.config.speciesPortal.data.rootDir+"/keystone/1.0/keystone_mapping.xlsx";
//speciesUploadService.uploadKeyStoneData("jdbc:mysql://localhost:3306/ezpz", "sravanthi", "sra123", mappingFile, 0, 0);

//def fa = speciesUploadService.getLogFileAppender("zoooutreach.1.1.primates");
//speciesUploadService.setLogAppender(fa);
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.1/primates.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.1/primates_mappingfile.xls", 0, 0, 0, 0,-1);
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.1/smallmammals.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.1/smallmammals_mappingfile.xls", 0, 0, 0, 0,-1);

//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/uploadready/abctrust_amphibians.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/uploadready/abctrust_amphibians_mappingfile.xls", 0, 0, 0, 0, -1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/1.5/photos_bryo/";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/1.5/abctrust_mosses_1.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/1.5/abctrust_mosses_mappingfile.xls", 0, 0, 0, 0,-1);
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/uploadready/abctrust_reptiles.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/uploadready/abctrust_reptiles_mappingfile.xls", 0, 0, 0, 0, -1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/sushantsanaye/uploadready/images";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/sushantsanaye/uploadready/Indian_Reef_Fishes_SushantSanaye.xls");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.5/NE_Butterflies_RG3";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.5/Northeast Butterflies-RG3.xlsx");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.5/NE_Butterflies_RG4";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/1.5/Northeast Butterflies-RG4.xlsx");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/uploadready/molluscs_images";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/uploadready/indian_molluscs_asr_cr1.xls");
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/uploadready/molluscs_images";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_molluscs/uploadready/indian_molluscs_asr_cr2.xls");
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_birds/uploadready/india_birds_cr1_images";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/indian_birds/uploadready/india_birds_cr1.xls");

//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.6/aquaticplants.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/1.6/aquaticplants_mapping.xls", 0, 0, 0, 0, -1);
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/fish_dk.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/fish_mapping.xls", 0, 0, 0, 0, -1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+" /datarep2/species/thomas/uploadready/images";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/thomas/uploadready/MammalsspeciesPages.xlsx");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/uploadready/NE_Butterflies_RG1";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/uploadready/NortheastButterflies-RG1.xlsx");
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/uploadready/NE_Butterflies_RG25";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/chitra/ne_butterflies/uploadready/NortheastButterflies-RG25.xlsx");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ranwa/uploadready/";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ranwa/uploadready/plant_speciesimages.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/ranwa/uploadready/plant_speciesimages_mapping.xlsx", 0, 0, 0, 0, -1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/uploadready/";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/uploadready/keralafloweringplants.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/abct/uploadready/keralafloweringplants_mappingfile.xls", 0, 0, 0, 0, -1);
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/mcccollege/uploadready/Database_on_Diots_of_Western_Ghats.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/mcccollege/uploadready/Database_on_Diots_of_Western_Ghats_mappingfile.xls", 0, 0, 0, 0, -1);
//

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/rawdata/forest_plants_HTML/climbers_images";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/climbers.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/climbers_mapping.xlsx", 0, 0, 0, 0,1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/rawdata/forest_plants_HTML/epi_saprophytes_images";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/epi_saprophytes.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/epi_saprophytes_mapping.xlsx", 0, 0, 0, 0,1);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/rawdata/forest_plants_HTML/herbs_images";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/herbs.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/herbs_mapping.xlsx", 0, 0, 0, 0,1);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/rawdata/forest_plants_HTML/shrubs_images";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/shrubs.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/shrubs_mapping.xlsx", 0, 0, 0, 0,1);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/rawdata/forest_plants_HTML/trees_images";
//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/trees.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/keystone/inprocess/trees_mapping.xlsx", 0, 0, 0, 0,1);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/aparnawatve/uploadready/images";
//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/aparnawatve/uploadready/aparnawatve.xlsx");

//speciesUploadService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/thomas/rawdata/BirdsspeciesPages.xlsx",grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/thomas/rawdata/bird_images");

//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/odonates.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/odonates_mapping.xls", 0, 0, 0, 0,1,grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/odonates.xls");

//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/associate_plants_of_grasslands_of_palni_hills.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/associate_plants_of_grasslands_of_Palni_hills_mapping.xlsx", 0, 0, 0, 0,-1,grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/associate_plants_of_grasslands_of_Palni_hills");

//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/grasses_of_palni_hills.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/grasses_of_palni_hills_mapping.xlsx", 0, 0, 0, 0,-1,grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/grasses_of_palni_hills");

converter = new XMLConverter();
noChange  = 0;
change = 0;
c = []
new File('licenses.csv').splitEachLine("\\t") {
	c << [it[0], it[1]];
}


def checkLicense(sField) {
	def isDirty = false;
	if(sField.licenses.size() > 1) {
		println "Multiple licenses exist.. not updating";
		return;
	}
	def contributor, license;

	if(sField.licenses) 
		license = sField.licenses.iterator().next(); 	
	if(sField.contributors) 
		contributor = sField.contributors.iterator().next(); 	
	if(contributor && license) {
println '1---'
println contributor.name+'  '+license.name;
println '2---'

		def lic = getLicense(contributor, license);
		if(lic) {
			change++;
			sField.licenses.clear();
			License l = converter.getLicenseByType(lic, false);
			sField.addToLicenses(l);
			isDirty = true;
		} else {
			noChange++;
		}
	} else {
		if(sField.instanceOf(SpeciesField.class)) {
			if(sField.field.id < 20 && sField.field.id>34) 
				println "**************"+sField+"  "+sField.contributors+"  "+sField.licenses;
		} else
			println "**************"+sField+"  "+sField.contributors+"  "+sField.licenses;
	}
	
	return isDirty;
}
def getLicense(contributor, license) {
	boolean isChanged = false;
	def l;
	c.each {
		def pattern = ".*${it[1].toLowerCase()}.*";
		if(contributor.name.toLowerCase() =~ /${pattern}/) {
			if(it[0] == license.name.value()) {
				isChanged = false;
			} else {
				isChanged = true;
				l = it[0];
			}
			return;
		}
	}
	if(isChanged)
		return l;
}

def updateLicense() {
	int offset = 0,limit=10; int count = Species.count();
	int noOfUpdations = 0;
	while(offset<count) {
		Species.withTransaction { status ->
			Species.list(max: limit, offset: offset, sort: "id", order: "desc").each { species ->
				println species.id;
				boolean isDirty = false;
				species.fields.each { sField ->
					isDirty = checkLicense(sField);
					sField.resources.each { res->
						isDirty = checkLicense(res);
					}			
				}
				species.resources.each {res ->
					isDirty = checkLicense(res);
				}
				//change resources licenses
				if(isDirty) {
					if(species.save(flush:true)) {
						println species.errors.each { println it}
					}
					noOfUpdations++;
				}
			}
		}
		offset += limit;
	}
	println change+"  "+noChange;
	println noOfUpdations
}

def getContributor(contributor) {
	def contribs = Contributor.findAllByNameIlike('%'+contributor+'%');
	println contributor+"> : "+contribs.collect {it.name}.join(',')
		println '---------------------------------------'
}

def getContributorContributions(contributor) {
	def sFields = SpeciesField.withCriteria {
		contributors {
			like('name', '%'+contributor+'%')
		}
	}
}
updateLicense();
