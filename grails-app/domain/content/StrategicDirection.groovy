package content

/**
 * Domain class of rStrategic Direction
 * Strategic Direction contains the message regarding the strategy of the CPEF grantee project
 */
class StrategicDirection {
	
	
    String title;
    String strategy;

    static constraints = {
    }
	
	static mapping = {
		strategy type:"text"
	}
	
	public String toString ()  {
		return title;
	}

}
