package species.auth

class PushNotificationToken {

	int userId
    String deviceToken
    Date dateCreated

    static constraints = {
    	deviceToken(nullable:false);
    	userId(nullable:true);
    }
}
