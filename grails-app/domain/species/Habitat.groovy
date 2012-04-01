package species

import species.Resource.ResourceType;
import species.utils.ImageType;
import species.utils.ImageUtils;

class Habitat {
	
	def grailsApplication;
	
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
		sort name:"asc"
	}
	
	Resource icon(ImageType type) {
		String name = this.name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
		name = ImageUtils.getFileName(name, type, '.png');

		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.resources.rootDir+"/group_icons/habitat/${name}")).exists()
		if(!iconPresent) {
			name = Habitat.findByName("Other").name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
			name = ImageUtils.getFileName(name, type, '.png');
		}
		return new Resource(fileName:"group_icons/habitat/${name}", type:ResourceType.ICON, title:"You can contribute!!!");
	}
}
