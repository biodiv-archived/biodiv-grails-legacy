import speciespage.TaxonService;

def taxonService = ctx.getBean("taxonService");
taxonService.loadTaxon(true);
