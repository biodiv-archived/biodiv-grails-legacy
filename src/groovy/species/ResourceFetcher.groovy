package species
import org.apache.commons.logging.LogFactory;

import species.participation.Observation;
import species.utils.Utils;
import content.eml.Document;

class ResourceFetcher {
	private static final log = LogFactory.getLog(this);
	
	private static int  BATCH_SIZE = 100
	
	private Observation obv = new Observation();
	private Document doc = new Document()
	private Species species = new Species()
	
	
	private String rType;
	private int offset = 0;
	private URL filterUrl = null;
	
	List nextResult = null
	
	public  ResourceFetcher(String rType, String filterUrl){
		this.rType = rType
		this.filterUrl = new URL(filterUrl)
		init()
	}
	
	public hasNext(){
		return (nextResult &&  !nextResult.isEmpty())? true : false
	}
	
	public next(){
		def retResult = nextResult
		if(hasNext())
			computeNext()
		return retResult
	}
	
	public reset(){
		init()
	}
	
	private init(){
		offset = 0
		computeNext()
	}
	
	private computeNext(){
		switch(rType){
			case Observation.class.canonicalName:
				nextResult = obv.fetchList(filterUrl, BATCH_SIZE, offset)
				break
			case Document.class.canonicalName:
				def paramsMap = Utils.getQueryMap(filterUrl)
				paramsMap.offset = offset
				paramsMap.max = BATCH_SIZE
				nextResult = doc.fetchList(paramsMap, BATCH_SIZE, offset).documentInstanceList
				break
			case Species.class.canonicalName:
				def paramsMap = Utils.getQueryMap(filterUrl)
				paramsMap.offset = offset
				paramsMap.max = BATCH_SIZE
				String action = filterUrl.getPath().split("/")[2]
				nextResult = species.fetchList(paramsMap, action).speciesInstanceList
				break
			default:
				log.debug "invalid object type " + rType
		}
		offset += BATCH_SIZE
	}
	
	public List getAllResult(){
		reset()
		List res = []
		while(hasNext()){
			res.addAll(next())
		}
		return res
	}		

}
