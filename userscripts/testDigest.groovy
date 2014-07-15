import species.participation.Observation
import species.*;
import species.auth.SUser
import content.eml.Document
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

def digestContent(){
    def obvList = Observation.withCriteria(){
        maxResults(5)
        order("lastRevised", "desc")        
    }
    def spList = Species.withCriteria(){
        maxResults(5)
        order("lastUpdated", "desc")
    }

    def docList = Document.withCriteria(){
        maxResults(5)
        order("lastRevised", "desc")
    }
    
    def userList = SUser.withCriteria(){
        maxResults(5)
        order("dateCreated", "desc")
    }

    def digestContent =[:]
    digestContent['observations'] = obvList
    digestContent['species'] = spList
    digestContent['documents'] = docList
    digestContent['users'] = userList
    return digestContent
}

def sendDigestMail(){
    def digService = ctx.getBean("digestService");
    digService.sendDigest(Digest.get(1L))
    println "========== DONE ============="
}

//sendDigestMail()


//creating a new digest instance
def createDigestIns(){
    def ug = UserGroup.get(18L)
    def dig = new Digest(userGroup:ug, lastSent:new Date() - 20, forObv:true, forSp:true, forDoc:true, forUsers:true);
    dig.save(flush:true)
    println "========== CREATED Digest instance ============="
}

//createDigestIns()

def tryDeleteUsersRes() {
    def resService = ctx.getBean("resourcesService");
    resService.deleteUsersResources()
}

//tryDeleteUsersRes()

