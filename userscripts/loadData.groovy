import org.codehaus.groovy.grails.commons.ApplicationHolder;

import speciespage.SpeciesUploadService;
import speciespage.SetupService;
import species.Species;

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

speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/odonates.xls", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/odonates_mapping.xls", 0, 0, 0, 0,1,grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/zoooutreach/uploadready/odonates.xls");

//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/associate_plants_of_grasslands_of_palni_hills.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/associate_plants_of_grasslands_of_Palni_hills_mapping.xlsx", 0, 0, 0, 0,-1,grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/associate_plants_of_grasslands_of_Palni_hills");

//speciesUploadService.uploadMappedSpreadsheet(grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/grasses_of_palni_hills.xlsx", grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/grasses_of_palni_hills_mapping.xlsx", 0, 0, 0, 0,-1,grailsApplication.config.speciesPortal.data.rootDir+"/datarep2/species/PHCC/uploadready/grasses_of_palni_hills");
