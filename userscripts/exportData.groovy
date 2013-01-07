import speciespage.SpeciesService;

def speciesservice = ctx.getBean("speciesService");
speciesservice.exportSpeciesData("/tmp/dwcadata");
