package species

class VisitCounter {
	String name;
	int visitCount = 1;
    
	static constraints = {
		name(blank:false, unique : true);
    }
	
	static mapping = {
		version false;
	}
	
	public static incrementPageVisit(name){
		VisitCounter vc = VisitCounter.findByName(name)
		if(vc){
			vc.visitCount++;
		}else{
			vc = new VisitCounter(name:name)
		}
		if(!vc.save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}	
	}
	
	public static int getPageVisitCount(name){
		VisitCounter vc = VisitCounter.findByName(name);
		if(vc){
			return vc.visitCount;
		}
		//denoting error condition
		return 0;
	}
}
