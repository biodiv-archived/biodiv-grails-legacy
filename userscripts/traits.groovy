import species.traits.SpeciesTraitsService;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.util.WebUtils;

def speciesTraitsService = ctx.getBean("speciesTraitsService");
def utilsService = ctx.getBean("utilsService");

GrailsWebRequest webUtils = WebUtils.retrieveGrailsWebRequest();
def request = webUtils.getCurrentRequest();

def languageInstance = utilsService.getCurrentLanguage(request);
speciesTraitsService.loadTraitDefinitions('/home/sravanthi/git/biodiv/app-conf/TraitsDefinition.tsv');

