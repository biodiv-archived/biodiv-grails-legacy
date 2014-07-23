package species.participation
import species.groups.UserGroup;

class Digest {

    UserGroup userGroup
    Date lastSent
    int threshold = 0
    boolean forObv
    boolean forSp
    boolean forDoc
    boolean forUsers
    boolean sendTopContributors
    boolean sendTopIDProviders
    Date startDateStats
    static constraints = {
    }
}
