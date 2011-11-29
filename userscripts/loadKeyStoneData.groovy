import species.DataLoader;


def dataLoader = new DataLoader();
/*
dataLoader.uploadFields("/home/sravanthi/sravanthi/projects/westernghats/exemplarspeciespages/DefinitionsAbridged_prabha.xlsx");
dataLoader.uploadLanguages("/home/sravanthi/sravanthi/projects/westernghats/Language_iso639-2.csv");
dataLoader.uploadCountries("/home/sravanthi/sravanthi/projects/westernghats/Countries_ISO-3166-1.csv");
*/

println grailsApplication.config.speciesPortal.data.rootDir;

grailsApplication.config.speciesPortal.images.uploadDir = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/keystone/images";
String mappingFile = grailsApplication.config.speciesPortal.data.rootDir+"/speciespages/keystone/keystone_mapping_v1.xlsx";
dataLoader.uploadKeyStoneData("jdbc:mysql://localhost:3306/ezpz", "sravanthi", "sra123", mappingFile, 0, 0);
