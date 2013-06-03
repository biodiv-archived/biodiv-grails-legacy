package content.eml

import java.util.Date;

import species.License;
import species.auth.SUser;
import species.groups.UserGroup;
import org.grails.taggable.Taggable;
import org.grails.rateable.*
/**
 * eml-literature module
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/eml-literature.html
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/index.html
 *
 */
class Document implements Taggable, Rateable {
	
	def grailsApplication
	def activityFeedService
	
	public enum DocumentType {
		Report("Report"),
		Poster("Poster"),
		Proposal("Proposal"),
		Miscellaneous("Miscellaneous"),

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
	
	String uri
	
	String description
	String contributors;
	String attribution;
	
	License license
	

	//source holder(i.e project, group)
	Long sourceHolderId;
	String sourceHolderType;

	Coverage coverage 	//Coverage Information

	String doi
	
	Date dateCreated
	Date lastUpdated
	
	boolean deleted
	
	boolean agreeTerms = false	
	
	static transients = [ 'deleted' ]


	static constraints = {
		title nullable:false, blank:false
		uFile validator : {val, obj -> 
			if(!(val || obj.uri))
				return 'fileOrUrl.validator.invalid' 
		},nullable:true
		uri validator : {val, obj -> 
			val || obj.uFile
		},nullable:true
		contributors nullable:true
		attribution nullable:true	
		sourceHolderId nullable:true
		sourceHolderType nullable:true
		author nullable:true
		coverage nullable:true	
		description nullable:true
		doi nullable:true
		license nullable:true
		
		agreeTerms nullable:true
		
	}
	
	static hasMany =[userGroups:UserGroup]
	
	static belongsTo = [SUser, UserGroup]
	
	
	static mapping = {
		description type:"text"
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
	
	def fetchSource(){
		if(sourceHolderId && sourceHolderType){
			return grailsApplication.getArtefact("Domain",sourceHolderType)?.getClazz()?.read(sourceHolderId)
		}
	}
	
	def afterDelete(){
		activityFeedService.deleteFeed(this)
	}
	
}
