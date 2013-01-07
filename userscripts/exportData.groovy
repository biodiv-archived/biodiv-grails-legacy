import speciespage.SpeciesService;

def speciesservice = ctx.getBean("speciesService");
speciesservice.exportSpeciesData(grailsApplication.config.speciesPortal.app.rootDir + "/dwcadata");
