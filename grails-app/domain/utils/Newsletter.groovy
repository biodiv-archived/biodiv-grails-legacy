package utils

import species.groups.UserGroup;

class Newsletter {
    String title
    Date date    
    String newsitem

	static belongsTo = [userGroup: UserGroup]
    static constraints = {
        newsitem size: 1..5000
		userGroup nullable:true;
    }
	
	static mappings = {
		sort date:"desc"
	}
}
