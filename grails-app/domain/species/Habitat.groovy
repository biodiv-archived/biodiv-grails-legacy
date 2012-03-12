package species

class Habitat {
	
	public enum HabitatType {
		ALL("All"),
		FOREST("Forest"),
		SAVANNA("Savanna"),
		SHRUBLAND("Shrubland"),
		GRASSLAND("Grassland"),
		WETLANDS("Wetlands"),
		ROCKY_AREAS("Rocky Areas"),
		CAVES_AND_SUBTERRANEAN_HABITATS("Caves and Subterranean Habitats"),
		DESERT("Desert"),
		MARINE("Marine"),
		ARTIFICIAL_TERRESTRIAL("Artificial - Terrestrial"),
		ARTIFICIAL_AQUATIC("Artificial - Aquatic"),
		INTRODUCED_VEGETATION("Introduced Vegetation"),
		OTHER("Other"),
		UNKNOWN("Unknown");
		
		private String value;
		
		HabitatType(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
		
		static def toList() {
			return [ALL, FOREST, SAVANNA, SHRUBLAND, GRASSLAND, WETLANDS, ROCKY_AREAS, \
				CAVES_AND_SUBTERRANEAN_HABITATS, DESERT, MARINE, ARTIFICIAL_TERRESTRIAL, \
				ARTIFICIAL_AQUATIC, INTRODUCED_VEGETATION, OTHER, UNKNOWN ]
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
