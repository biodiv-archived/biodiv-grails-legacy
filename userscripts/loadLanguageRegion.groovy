
import speciespage.SetupService;

def s = ctx.getBean("setupService");
println "=============== started == "
s.updateLanguageRegion(grailsApplication.config.speciesPortal.data.rootDir+"/templates/Language_iso639-2_withRegion.csv");
println "=================== done =="
