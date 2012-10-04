package utils

import species.groups.UserGroup;

class Newsletter {
    String title
    Date date    
    String newsitem
	boolean sticky = false;

	static belongsTo = [userGroup: UserGroup]
    static constraints = {
        newsitem type:'text'
		userGroup nullable:true;
    }
	
	static mappings = {
		sort date:"desc"
	}
}
