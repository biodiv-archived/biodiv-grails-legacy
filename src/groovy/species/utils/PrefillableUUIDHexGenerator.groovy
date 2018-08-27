package species.utils

import org.hibernate.engine.spi.SessionImplementor;


public class PrefillableUUIDHexGenerator extends org.hibernate.id.enhanced.SequenceStyleGenerator {
	public Serializable generate(SessionImplementor session, Object obj) {
		if(obj.id && obj.id instanceof Long){
			return obj.id
		} else {
		def cid = super.generate(session, obj)
			return cid 
		}
	}
}
