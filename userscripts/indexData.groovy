import speciespage.search.SpeciesSearchService;
import speciespage.search.ObservationsSearchService;
import speciespage.search.NewsletterSearchService;
import species.SpeciesField.AudienceType;

//def speciesSearchService = ctx.getBean("speciesSearchService");
//speciesSearchService.deleteIndex();
//speciesSearchService.publishSearchIndex();
//speciesSearchService.optimize();

//def newsletterSearchService = ctx.getBean("newsletterSearchService");
//newsletterSearchService.deleteIndex();
//newsletterSearchService.publishSearchIndex();
//
def observationsSearchService = ctx.getBean("observationsSearchService");
observationsSearchService.deleteIndex();
observationsSearchService.publishSearchIndex();

//def cSearchService = ctx.getBean("checklistSearchService");
//cSearchService.deleteIndex();
//cSearchService.publishSearchIndex();
//cSearchService.optimize();
