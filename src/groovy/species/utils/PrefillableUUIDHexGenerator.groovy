package species.utils

import org.hibernate.engine.SessionImplementor


public class PrefillableUUIDHexGenerator extends org.hibernate.id.IncrementGenerator {
	public Serializable generate(SessionImplementor session, Object obj) {
		if(obj.id && obj.id instanceof Long){
			return obj.id
		} else {
		def cid = super.generate(session, obj)
			return cid 
		}
	}
}
