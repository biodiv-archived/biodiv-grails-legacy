package species

import species.auth.SUser

abstract class NamesMetadata extends NamesSorucedata {

    public enum NamePosition {
        CLEAN("Clean"),
        WORKING("Working"),
        RAW("Raw");

        private String value;

        NamePosition(String value) {
            this.value = value;
        }

        static list() {
            [
            CLEAN,
            WORKING,
            RAW
            ]
        }

        String value() {
            return this.value;
        }

        String toString() {
            return this.value();
        }
		
		static NamePosition getEnum(String str){
			if(!str) return null
			
			NamePosition retVal = null
			list().each{
				if(!retVal){
					if(str.trim().equalsIgnoreCase(it.value())){
						retVal = it
					}
				}
			}
			return retVal
		}
    }

    public enum NameStatus {
        ACCEPTED("Accepted"),
        SYNONYM("Synonym"),
        PROV_ACCEPTED("ProvAccepted"),
        COMMON("Common");
        private String value;

        NameStatus(String value) {
            this.value = value;
        }

        static list() {
            [
            ACCEPTED,
            SYNONYM,
            PROV_ACCEPTED,
            COMMON
            ]
        }

        String value() {
            return this.value;
        }

        String label() {
            if(this == ACCEPTED) return "Accepted Name" 
            if(this == SYNONYM) return "Synonym" 
            if(this == PROV_ACCEPTED) return "Provisionally Accepted Name" 
            if(this == COMMON) return "Common Name" 
        }

        /*String toString() {
            return this.value();
        }*/
		
		static NameStatus getEnum(String str){
			if(!str) return null
			
			NameStatus retVal = null
			list().each{
				if(!retVal){
					if(str.trim().equalsIgnoreCase(it.value())){
						retVal = it
					}
				}
			}
			return retVal
		}
    }

    public enum COLNameStatus {
        ACCEPTED("accepted"),
        COMMON("common"),
        SYNONYM("synonym"),
        MISAPPLIED("misapplied"),
        AMBIGUOUS_SYNONYM("ambiguous"),
        PROV_ACCEPTED("provisionally");

        private String value;

        COLNameStatus(String value) {
            this.value = value;
        }

        static list() {
            [
            ACCEPTED,
            SYNONYM,
            AMBIGUOUS_SYNONYM,
            PROV_ACCEPTED,
            COMMON
            ]
        }

        String value() {
            return this.value;
        }

        String toString() {
            return this.value();
        }
    }

    public enum IbpSource {
        SPECIES("Species"),
        OBSERVATION("Observation"),
        CURATOR("Curator")

        private String value;

        IbpSource(String value) {
            this.value = value;
        }

        static list() {
            [
            SPECIES,
            OBSERVATION,
            CURATOR
            ]
        }

        String value() {
            return this.value;
        }

        String toString() {
            return this.value();
        }
    }

	
	//String name
	NameStatus status
	NamePosition position = NamePosition.RAW
	String authorYear
	//Classification will be calculated from path
	

	String matchDatabaseName
	String matchId

	IbpSource ibpSource
	String viaDatasource
	
	//to store reference backbone i.e col, butterfly by kunte default is col, ideally it should be enum
	//String referenceTaxonomyBackbone = "COL"
	
	List<SUser> curators;
	static hasMany = [curators: SUser]
	
	
    static constraints = {
		position nullable:true;
		authorYear nullable:true;
		viaDatasource nullable:true;
		ibpSource nullable:true;
		matchDatabaseName nullable:true;
		matchId nullable:true;
	}

    static mapping = {
        tablePerHierarchy false
    }

	def beforeInsert(){
		super.beforeInsert()
	}
	
	def beforeUpdate(){
	}

}
