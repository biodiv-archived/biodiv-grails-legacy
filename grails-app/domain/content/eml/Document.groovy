package content.eml

import content.fileManager.UFile
import species.auth.SUser;
import species.groups.UserGroup;


/**
 * eml-literature module
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/eml-literature.html
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html
 *
 */
class Document {

	String title
	SUser author;
	
	UFile uFile   //covers physical file formats, party info, access info

	Coverage coverage 	//Coverage Information


	static constraints = {
		
	}
	
	static hasMany ={userGroups:UserGroup}
	
	static belongsTo = [SUser, UserGroup]
	
	
	static mapping = {
		coverage cascade: "all-delete-orphan"
		uFile cascade: "all-delete-orphan"
		
	}
	
	
	def getOwner() {
		return author;
	}
	
	String toString() {
		return title;
	}
}
