package species.participation

import java.util.Collections;
import species.UtilsService
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
	
	public static Map getStatResult(UserGroup ug=null){
		update(ug)
		long id = ug ? ug.id : 0
		def rr = idToResultMap.get(id)
		return idToResultMap.get(id).result
	}

	//tree group obv count
	private static int TREE_OBV_COUNT = 0
	private static Date TREE_OBV_COUNT_UPDATED = new Date();
	
	public static int getTreeGroupObvCount(){
		Date currTime = new Date()
		if((TREE_OBV_COUNT == 0) || (currTime.getTime() - TREE_OBV_COUNT_UPDATED.getTime())/(1000*60) > INTERVAL){
			TREE_OBV_COUNT = ActivityFeed.dailyStats(UserGroup.get(18), UtilsService.parseDate("22/04/2015")).Observation
			TREE_OBV_COUNT_UPDATED = currTime
		}
		return TREE_OBV_COUNT
	}
	
}
