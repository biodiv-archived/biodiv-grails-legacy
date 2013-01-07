import speciespage.SpeciesService;

def speciesservice = ctx.getBean("speciesService");
def species = speciesservice.search(["query":"contributor:chitra OR contributor:Seena"]).speciesInstanceList;
speciesservice.exportSpeciesData("/tmp/dwcadata", species);
