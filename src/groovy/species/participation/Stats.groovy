package species.participation

import java.util.Collections;
import species.groups.UserGroup

class Stats {
	private static int INTERVAL = 10
	private static Map idToResultMap =  Collections.synchronizedMap([:])
	
	private long id
	private Date lastUpdated
	private Map result
	
	
	private Stats(long id, Date lastUpdated, Map result){
		this.id = id
		this.lastUpdated = lastUpdated
		this.result = result
	}
	
	public static Map getStatResult(UserGroup ug=null){
		update(ug)
		long id = ug ? ug.id : 0
		def rr = idToResultMap.get(id)
		return idToResultMap.get(id).result
	}
	
	private static update(UserGroup ug){
		long id = ug ? ug.id : 0
		
		Stats stats = idToResultMap.get(id)
		Date currTime = new Date()
		Date lastUpdated = stats ? stats.lastUpdated : null
		
		if(!stats || ((currTime.getTime() - lastUpdated.getTime())/(1000*60)) > INTERVAL){
			Map res = ActivityFeed.dailyStats(ug)
			idToResultMap.put(id, new Stats(id, currTime, res))
		}
	}
	
}
