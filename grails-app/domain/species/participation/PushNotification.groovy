package species.participation

class PushNotification {

	String title
	String description
	int userId
	Date dateCreated

    static constraints = {
    	description(nullable:false);
    	title(nullable:false);
    	userId(nullable:false);
    }

    static mapping = {
		description type:'text';
	}

}


