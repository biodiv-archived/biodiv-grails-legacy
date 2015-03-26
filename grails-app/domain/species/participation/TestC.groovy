package species.participation

class TestC {
    
    TestA a;
    TestB b;

    static constraints = {
        a(unique: ['b'])
    }
    
    static mapping = {
        version false
    }

    static getBs(a) {
        println "called"
        def x = TestC.findAllByA(a);
        println "=========Bs == " + x;
    }

    static getAs(b) {
        def x = TestC.findAllByB(b);
        println "=========As == " + x;
    }
}
