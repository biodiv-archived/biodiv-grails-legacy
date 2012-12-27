import org.codehaus.groovy.grails.commons.ApplicationHolder;

import speciespage.SpeciesService;
import speciespage.SetupService;

//def s = ctx.getBean("setupService");
//s.uploadHabitats();
def speciesService = ctx.getBean("speciesService");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/mango";
//speciesService.uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/mango/MangoMangifera_indica_prabha2.xlsx", 0, 0, 1, 4);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/grey_falcolin";
//speciesService.uploadSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/grey_falcolin/GreyFrancolin.xlsx", 0, 0, 1, 4);
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/Rufous Woodpecker/images";
//speciesService.uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/Rufous Woodpecker/RufousWoodepecker.xlsm");	
//
//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/Eurasian Curlew/png ec";
//speciesService.uploadNewSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ibp/1.0/Eurasian Curlew/EurasianCurlew.xlsm");

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/atree/1.0/Dung_beetle/images";
//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/atree/1.0/Dung_beetle/Dung_beetle_Species_pages_IBP.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/dungbeetles_mapping.xlsx", 0, 0, 0, 0);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ifp/1.0/images";
//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/ifp/1.0/Trees_descriptives.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/ifp_tree_mapping_v2.xlsx", 0, 0, 0, 2);
//
////grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/images";
//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/Bats/WG_bats_account_01Nov11_sanjayMolur.xls", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/WG_bats_account_01Nov11_sanjayMolurspecies_mapping_v2.xlsx", 0, 0, 0, 0);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespages";
//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/speciespages/species accounts188_v2.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/mappings/speciesaccount188_mapping_v1.xlsx", 0, 0, 0, 0);

//grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/keystone/1.0";
//String mappingFile = grailsApplication.config.speciesPortal.data.rootDir+"/keystone/1.0/keystone_mapping.xlsx";
//speciesService.uploadKeyStoneData("jdbc:mysql://localhost:3306/ezpz", "sravanthi", "sra123", mappingFile, 0, 0);

//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/primates.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/primates_mappingfile.xls", 0, 0, 0, 0);
//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/smallmammals.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/zoooutreach/uploadready/smallmammals_mappingfile.xls", 0, 0, 0, 0);

//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/abct/uploadready/abctrust_amphibians.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/abct/uploadready/abctrust_amphibians_mappingfile.xls", 0, 0, 0, 0);
//speciesService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/abct/uploadready/abctrust_mosses.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/abct/uploadready/abctrust_mosses_mappingfile.xls", 0, 0, 0, 0);

grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/sushantsanaye/uploadready/images";
speciesService.uploadNewSimpleSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/sushantsanaye/uploadready/Indian_Reef_Fishes_SushantSanaye.xls");

