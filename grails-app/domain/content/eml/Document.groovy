package content.eml

import java.util.Date;

import species.auth.SUser;
import species.groups.UserGroup;
import org.grails.taggable.Taggable;

/**
 * eml-literature module
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/eml-literature.html
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html
 *
 */
class Document implements Taggable {


	public enum DocumentType {
		REPORT("Report"),
		POSTER("Poster"),
		PROPOSAL("Proposal"),
		MISCELLANEOUS("Miscellaneous"),

		private String value;
		

		DocumentType(String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}

	DocumentType type


	String title
	SUser author;
	
	UFile uFile   //covers physical file formats
	
	String description
	String contributors;
	String attribution;

	//source holder(i.e project, group)
	Long sourceHolderId;
	String sourceHolderType;

	Coverage coverage 	//Coverage Information

	Date dateCreated
	Date lastUpdated
	
	boolean deleted
	
	static transients = [ 'deleted' ]


	static constraints = {
		title nullable:false
		uFile nullable:false
		contributors nullable:true
		attribution nullable:true	
		sourceHolderId nullable:true
		sourceHolderType nullable:true
	}
	
	static hasMany ={userGroups:UserGroup}
	
	static belongsTo = [SUser, UserGroup]
	
	
	static mapping = {
		description type:"text"
		
		coverage cascade: "all-delete-orphan"
		uFile cascade: "all-delete-orphan"
		
	}
	
	
	def getOwner() {
		return author;
	}
	
	String toString() {
		return title;
	}
	
	def setSource(parent) {
		this.sourceHolderId = parent.id
		this.sourceHolderType = parent.class.getCanonicalName()
	}
}
