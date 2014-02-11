import species.Field

def addNewField(){
	def f = new Field(concept:'Information Listing',category:'Images', description:'Place holder for images', displayOrder:83)
	f.save(flush:true)
}

addNewField()