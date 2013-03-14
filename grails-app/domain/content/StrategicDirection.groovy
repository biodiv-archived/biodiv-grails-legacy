package content

/**
 * Domain class of rStrategic Direction
 * Strategic Direction contains the message regarding the strategy of the CPEF grantee project
 */
class StrategicDirection {
    
    String strategy;

    static constraints = {
    }
	
	public String toString() {
		return strategy;
	}
}
