
import speciespage.SetupService;
import species.Language

//Language.list().each{ ll ->
//	ll.isDirty = false;
//	if(ll.save(flush:true)){
//		ll.errors..each { println it; }
//	}
//}

def s = ctx.getBean("setupService");
println "=============== started == "
s.updateLanguageRegion("/home/sandeept/dbmig/pambaMig/Language_iso639-2_withRegion.csv");
println "=================== done =="
