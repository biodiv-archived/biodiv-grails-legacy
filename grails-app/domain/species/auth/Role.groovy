package species.auth

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Cache(region="role", usage = CacheConcurrencyStrategy.READ_ONLY)  // or @Cacheable(true) for JPA
class Role implements Serializable {

	String authority

	static mapping = {
		version:false;
        cache:true;
	}

	static constraints = {
		authority blank: false, unique: true
	}
}
