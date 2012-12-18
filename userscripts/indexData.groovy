import speciespage.search.SpeciesSearchService;
import speciespage.search.ObservationsSearchService;
import speciespage.search.NewsletterSearchService;
import species.SpeciesField.AudienceType;

//def speciesSearchService = ctx.getBean("speciesSearchService");
//speciesSearchService.deleteIndex();
//speciesSearchService.publishSearchIndex();

//def newsletterSearchService = ctx.getBean("newsletterSearchService");
//newsletterSearchService.deleteIndex();
//newsletterSearchService.publishSearchIndex();

def observationsSearchService = ctx.getBean("observationsSearchService");
observationsSearchService.deleteIndex();
observationsSearchService.publishSearchIndex();
