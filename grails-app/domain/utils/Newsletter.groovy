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
}
