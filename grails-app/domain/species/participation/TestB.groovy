package species.participation

class TestB extends TestA {
    
    String address;

	//static hasMany = [testas: TestA];
	//static belongsTo = [TestA];

    static constraints = { 
        address nullable:true;
    }
}
