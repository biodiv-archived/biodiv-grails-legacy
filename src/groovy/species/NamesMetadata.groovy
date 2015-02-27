package species

import species.auth.SUser

abstract class NamesMetadata extends NamesSorucedata {
	
	public enum NamePosition {
		CLEAN("Clean"),
		WORKING("Working"),
		DIRTY("Dirty");

		private String value;

		NamePosition(String value) {
			this.value = value;
		}

		static list() {
			[
				CLEAN,
				WORKING,
				DIRTY
			]
		}

		String value() {
			return this.value;
		}

		String toString() {
			return this.value();
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

		String toString() {
			return this.value();
		}
	}

	public enum COLNameStatus {
        ACCEPTED("accepted name"),
        COMMON("common name"),
        SYNONYM("synonym"),
        MISAPPLIED("misapplied name"),
        AMBIGUOUS_SYNONYM("ambiguous synonym"),
        PROV_ACCEPTED("provisionally accepted name");

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
	NamePosition position = NamePosition.DIRTY
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
		//name blank:false
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
	}
	
	def beforeUpdate(){
	}

}
