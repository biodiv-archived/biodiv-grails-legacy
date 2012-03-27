import species.NamesParser;
import species.Synonyms;
import speciespage.TaxonService;

def namesLoaderService = ctx.getBean("namesLoaderService");
namesLoaderService.syncNamesAndRecos(true);

/*
NamesParser namesParser = new NamesParser();
Synonyms.withTransaction {
	Synonyms.list().eachWithIndex { syn, index ->
		def parsedNames = namesParser.parse([syn.name]);
		if(parsedNames[0]?.canonicalForm) {
			syn.canonicalForm = parsedNames[0].canonicalForm;
			syn.normalizedForm = parsedNames[0].normalizedForm;;
			syn.italicisedForm = parsedNames[0].italicisedForm;;
			syn.binomialForm = parsedNames[0].binomialForm;;
			if(!syn.save(flush:true, insert:true)) {
				syn.errors.each {println it}
			}
		}
	};
}
*/

//def taxonService = ctx.getBean("taxonService");
//taxonService.loadTaxon(true);

