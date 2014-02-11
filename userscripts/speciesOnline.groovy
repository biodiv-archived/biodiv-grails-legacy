import species.Field;
import species.auth.SUser;
import species.Contributor;

def addNewField(){
	def f = new Field(concept:'Information Listing',category:'Images', description:'Place holder for images', displayOrder:83)
	f.save(flush:true)
}

def getContNotPresent(){
    def allCont = []
    def contList = Contributor.list()
    contList.each { cl->
        allCont.add(cl.name)
    }
    
    println "==ALL CONT ======= " + allCont

    def suList = SUser.findAllByUsernameInList(allCont)
    def contPresent = []
    suList.each{ su-> 
        contPresent.add(su.username)
    }
    println "======CONT PRESENT ==== " + contPresent
    def contNotPresent = allCont - contPresent

    println "======CONT NOT PRESNET ====== " + contNotPresent
    File file = new File("/home/rahulk/Documents/new.tsv")
    contNotPresent.each {
        file << ("${it}\n")
    } 
}

getContNotPresent()

addNewField()
