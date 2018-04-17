package utils

import species.groups.UserGroup;
import species.Language;

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

//@Cache(region="Newsletter", include = "non-lazy")
//@JsonIgnoreProperties([])

class Newsletter {
    String title
    Date date    
    String newsitem
	boolean sticky = false;
    int displayOrder;
    int parentId;
    boolean showInFooter = false;
    Language language;

	static belongsTo = [userGroup: UserGroup]
    static constraints = {
		title nullable: false, blank:false
		title nullable: false
        newsitem type:'text'
		userGroup nullable:true;
        displayOrder nullable:false;
        parentId nullable:false;
        showInFooter nullable:false;
        language nullable:false;
    }
	
	static mappings = {
		sort displayOrder:"desc"

        cache include: 'non-lazy'
	} 
	
	def boolean fetchIsHomePage(){
		if(userGroup?.homePage){
			return ("newsletter".equalsIgnoreCase(userGroup.homePage.tokenize('/').first().trim())) && (id == userGroup.homePage.tokenize('/').last().trim().toLong())
		}
		
		return false
	}
}
