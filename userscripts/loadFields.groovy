import species.DataLoader;
import species.SpeciesGroup;
import speciespage.GroupHandlerService;


def speciesService = ctx.getBean("setupService");

setupService.uploadFields(grailsApplication.config.speciesPortal.data.rootDir+"/datarep/species/templates/Definitions.xlsx");
//dataLoader.uploadLanguages(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Language_iso639-2.csv");
//dataLoader.uploadCountries(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Countries_ISO-3166-1.csv");
//dataLoader.uploadClassifications(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Classifications.xlsx", 0, 0);
//
//def groupHandlerService = new GroupHandlerService();
//def allGroup = new SpeciesGroup(name:"All");
//allGroup.save(flush:true, failOnError:true);
//def othersGroup = new SpeciesGroup(name:"Others", parentGroup:allGroup);
//othersGroup.save(flush:true, failOnError:true);
//groupHandlerService.loadGroups(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Groups.xlsx", 0, 0);
