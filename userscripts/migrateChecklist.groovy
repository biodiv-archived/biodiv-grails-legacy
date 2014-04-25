import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;

import com.vividsolutions.jts.geom.PrecisionModel;
import species.CommonNames;
import species.Language;
//import species.participation.checklistUtilService
import species.participation.curation.*
import species.participation.*
import species.formatReader.SpreadsheetReader;
import species.utils.*;
import speciespage.*
import com.vividsolutions.jts.geom.*
import content.eml.Coverage
import content.eml.Document
import species.groups.UserGroup
import grails.converters.JSON
import species.auth.*;
import groovy.util.Eval;

def addDrupalId(){
	def checklistUtilService = ctx.getBean("checklistUtilService");
	checklistUtilService.setDrupalRef()
	println " =========== done "
}



//addFeedToChecklist()
//serializeChecklist()
//corChecklist()
//addDrupalId()


def batchUpload(){
	def s = ctx.getBean("obvUtilService");
	s.batchUpload(null, [imageDir:'/home/sandeept/bulk/images', batchFileName:'/home/sandeept/bulk/observation_bulk_upload_sheet_KANS1.xls'])
}


def updateGeoPrivacy(){
	Observation.withTransaction(){
		Observation.findAllByAuthor(species.auth.SUser.read(3881)).each { obv ->
			obv.geoPrivacy = true
			obv.save(flush:true)
		}
	}
}


def fixSpeciesCount() {
    Checklists.withTransaction() {
        Checklists.findAllBySpeciesCountAndIsDeleted(0,false).each { checklistInstance ->
            println checklistInstance.title
            println checklistInstance.observations.size()

//		    checklistInstance.speciesCount = (checklistInstance.observations) ? checklistInstance.observations.size() : 0
  //          if(!checklistInstance.save(flush:true)) {
    //            checklistInstance.errors.each { println it }
     //       }
       }
    }
}

fixSpeciesCount();
