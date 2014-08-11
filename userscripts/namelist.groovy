import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import species.TaxonomyRegistry


def testNav(){
	def tSer = ctx.getBean("taxonService")
	GrailsParameterMap m = new GrailsParameterMap([:], null)
	m.taxonId = "3004"
	m.classificationId = "817"
	println "Res === " + tSer.getNodeChildren(m)
}

ctx.getBean("taxonService").addLevelToTaxonReg()

//ALTER TABLE taxonomy_registry ADD COLUMN level integer;