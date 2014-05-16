package utils

import species.groups.UserGroup;

class Newsletter {
    String title
    Date date    
    String newsitem
	boolean sticky = false;
    int displayOrder;

	static belongsTo = [userGroup: UserGroup]
    static constraints = {
		title nullable: false, blank:false
		title nullable: false
        newsitem type:'text'
		userGroup nullable:true;
        displayOrder nullable:false;
    }
	
	static mappings = {
		sort displayOrder:"desc"
	}
	
	def boolean fetchIsHomePage(){
		if(userGroup?.homePage){
			return ("newsletter".equalsIgnoreCase(userGroup.homePage.tokenize('/').first().trim())) && (id == userGroup.homePage.tokenize('/').last().trim().toLong())
		}
		
		return false
	}
}
