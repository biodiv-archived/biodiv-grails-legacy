import speciespage.search.SpeciesSearchService;
import speciespage.search.ObservationsSearchService;
import speciespage.search.NewsletterSearchService;
import species.SpeciesField.AudienceType;
import speciespage.search.ProjectSearchService
import speciespage.search.DocumentSearchService
import speciespage.search.SUserSearchService
import species.Species

def speciesSearchService = ctx.getBean("speciesSearchService");
speciesSearchService.deleteIndex();
speciesSearchService.publishSearchIndex([Species.read(33875)]);
speciesSearchService.optimize();
/*
def newsletterSearchService = ctx.getBean("newsletterSearchService");
newsletterSearchService.deleteIndex();
newsletterSearchService.publishSearchIndex();

def observationsSearchService = ctx.getBean("observationsSearchService");
observationsSearchService.deleteIndex();
observationsSearchService.publishSearchIndex();
observationsSearchService.optimize();
*/
/*def cSearchService = ctx.getBean("checklistSearchService");
cSearchService.deleteIndex();
cSearchService.publishSearchIndex();
cSearchService.optimize();


def projectSearchService = ctx.getBean("projectSearchService");
projectSearchService.deleteIndex();
projectSearchService.publishSearchIndex();
*/

/*def documentSearchService = ctx.getBean("documentSearchService");
documentSearchService.deleteIndex();
documentSearchService.publishSearchIndex();
*/
/*def usersSearchService = ctx.getBean("usersSearchService");
usersSearchService.deleteIndex();
usersSearchService.publishSearchIndex();
//usersSearchService.optimize();
*/
