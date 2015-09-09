package species.participation

class TestA {
    
    String name;
    
	//static hasMany = [testbs: TestB];
    
    static constraints = {
        name nullable:false;
    }
	
    static mapping = {
		tablePerHierarchy true
	 }
}
