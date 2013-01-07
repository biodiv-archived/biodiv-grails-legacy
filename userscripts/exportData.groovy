import speciespage.SpeciesService;

def speciesservice = ctx.getBean("speciesService");
def speciesSearchservice = ctx.getBean("speciesSearchService");
//def species = speciessearchService.search(["query":"contributor:chitra OR contributor:Seena", ]);
speciesservice.exportSpeciesData("/tmp/dwcadata");
