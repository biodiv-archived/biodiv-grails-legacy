package species.participation
import species.groups.UserGroup;

class Digest {

    UserGroup userGroup
    Date lastSent = new Date() - 20
    int threshold = 0
    boolean forObv = true
    boolean forSp = true
    boolean forDoc = true
    boolean forUsers = true
    boolean sendTopContributors = true
    boolean sendTopIDProviders = true
	
    //Date startDateStats = new Date()
	
    static constraints = {
    }
	
	public static Digest updateDigest(UserGroup ug){
		Digest d = Digest.findByUserGroup(ug)
		
		if(d) 
			return
		
		d  = new Digest(userGroup:ug)
		if(!d.save(flush:true)){
			d.errors.allErrors.each { println it }
			return
		}
		
		return d
	}

}
