package species
import org.apache.commons.logging.LogFactory;

import species.participation.Observation;
import species.participation.Discussion;
import species.participation.DownloadLog;
import species.TaxonomyDefinition;
import species.utils.Utils;
import content.eml.Document;

import grails.converters.JSON;

class ResourceFetcher {
	private static final log = LogFactory.getLog(this);
	
	private static final int  BATCH_SIZE = 100
	
	private Observation obv = new Observation();
	private Document doc = new Document()
	private Species species = new Species()
	private Discussion discussion = new Discussion()
	private TaxonomyDefinition taxonomyDefinition = new TaxonomyDefinition();
	
	
	private String rType;
	private int offset = 0;
	private URL filterUrl = null;
	private String userGroupWebAddress = null;
    private Map paramsMap;
	
	List nextResult = null

	public ResourceFetcher(String rType, String filterUrl){
		this(rType, filterUrl, null)
	}
	
	public ResourceFetcher(String rType, String filterUrl, String userGroupWebAddress){
        this(rType, filterUrl, userGroupWebAddress, 0);    
    }

	public ResourceFetcher(String rType, String filterUrl, String userGroupWebAddress, int offset){
		this.rType = rType
		this.filterUrl = new URL(filterUrl)
		this.userGroupWebAddress = userGroupWebAddress
		init(offset)
	}
	
    public ResourceFetcher(String rType, DownloadLog dl, String userGroupWebAddress, int offset){
		this.rType = rType
		this.filterUrl = new URL(dl.filterUrl)
		this.userGroupWebAddress = userGroupWebAddress
        this.paramsMap = dl.fetchMapFromText(); 
        init(offset)
	}

	public hasNext(){
		return (nextResult &&  !nextResult.isEmpty())? true : false
	}
	
	public next(){
        log.debug "Getting new batch of results"
		def retResult = nextResult
        log.debug retResult;
		if(hasNext())
			computeNext()
		return retResult
	}
	
	public reset(){
		init(0)
	}

    public resetFrom(int offset){
		init(offset)
	}

	private init(int offset){
		this.offset = offset
		computeNext()
	}
	
	private computeNext(){
		def paramsMap = Utils.getQueryMap(filterUrl)
		//adding group filter parameter for list/search query
		paramsMap["webaddress"] = this.userGroupWebAddress?:paramsMap["webaddress"]
        String[] parts = filterUrl.getPath().split("/");
		String action = parts.length >=3 ? parts[2]:'index';
		if(this.paramsMap)
	    	paramsMap.putAll(this.paramsMap);

		//XXX:withnewtransaction creates new jdbc connection everytime. this is done to avoid connection time out problem for log running job.
		Observation.withNewTransaction {  status ->
			switch(rType){
				case Observation.class.canonicalName:
					nextResult = obv.fetchList(paramsMap, BATCH_SIZE, offset, action)
					break
				case Document.class.canonicalName:
					paramsMap.offset = offset
					paramsMap.max = BATCH_SIZE
					nextResult = doc.fetchList(paramsMap, BATCH_SIZE, offset).documentInstanceList
					break
				case Discussion.class.canonicalName:
					paramsMap.offset = offset
					paramsMap.max = BATCH_SIZE
					nextResult = discussion.fetchList(paramsMap, BATCH_SIZE, offset).discussionInstanceList
					break
				case Species.class.canonicalName:
					paramsMap.offset = offset
					paramsMap.max = BATCH_SIZE
					nextResult = species.fetchList(paramsMap, action).speciesInstanceList
					break
				case TaxonomyDefinition.class.canonicalName:
					paramsMap.offset = offset
					paramsMap.max = BATCH_SIZE
					nextResult = taxonomyDefinition.fetchList(paramsMap).namesList
                    log.debug nextResult
					break
				default:
					log.debug "invalid object type " + rType
			}
		}
		offset += BATCH_SIZE
	} 
	
	public List getAllResult(){
		resetFrom(0)
		List res = []
		while(hasNext()){
			res.addAll(next())
		}
		return res
	}		

}
