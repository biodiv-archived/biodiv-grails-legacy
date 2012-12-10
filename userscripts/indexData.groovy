import speciespage.search.SpeciesSearchService;
import speciespage.search.NewsletterSearchService;
import species.SpeciesField.AudienceType;

//def speciesSearchService = ctx.getBean("speciesSearchService");
//speciesSearchService.publishSearchIndex(Species.findByIdInList([238535,257346,238520,238583,262370,262371,238544,238518,238592,238560,238536,238524,262362,238566,238550,227723,262364,253966,238565,227185,228494,226575,262368,238458,238585,238540,227666,262369,238521,257364,257353,227975,238503,238439,238541,228027,238459,228235,238567,238475,262367,262363,262365,238548,262366,257362]));

def newsletterSearchService = ctx.getBean("newsletterSearchService");
newsletterSearchService.publishSearchIndex();