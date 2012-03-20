package species

class Habitat {
	
	public enum HabitatType {
		ALL("All"),
		FOREST("Forest"),
		THICKET("Thicket"),
		SAVANNA("Savanna"),
		GRASSLAND("Grassland"),
		SWAMP("Swamp"),
		AQUATIC("Aquatic"),
		DESERT("Desert"),
		ROCKY("Rocky"),
		AGRICULTURE("Agriculture"),
		OTHERS("Others"),
		
		private String value;
		
		HabitatType(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
		
		static def toList() {
			return [ALL, FOREST, THICKET, SAVANNA, GRASSLAND,   \
				 SWAMP, AQUATIC, DESERT, ROCKY, AGRICULTURE,  \
				  OTHERS]
		}
		
		public String toString() {
			return this.value();
		}
		
	};
	
	String name;
	
    static constraints = {
		name(blank:false, unique : true);
    }
	
	static mapping = {
		version false;
	}
}
