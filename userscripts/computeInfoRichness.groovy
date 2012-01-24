import speciespage.SpeciesService;

def speciesService = ctx.getBean("speciesService");
speciesService.computeInfoRichness();
