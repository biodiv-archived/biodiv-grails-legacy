package species.auth

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

//@Cache(region="role", usage = CacheConcurrencyStrategy.READ_ONLY)  // or @Cacheable(true) for JPA
class Role implements Serializable {

	String authority

	static mapping = {
		version:false;
        cache usage: 'read-only', include: 'non-lazy'
	}

	static constraints = {
		authority blank: false, unique: true
	}

    static Role findByAuthority(String whatever) { 
        println "Role findByAuthority overridden fn for cache"
        return Role.createCriteria().get {
            eq 'authority', whatever
            cache true
        }
    } 
}
