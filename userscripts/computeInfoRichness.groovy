import speciespage.SpeciesService;

def speciesservice = ctx.getbean("speciesService");
speciesservice.computeinforichness();

