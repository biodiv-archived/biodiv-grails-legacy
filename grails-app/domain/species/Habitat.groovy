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
		ROCKY_OUTCROPS("Rocky Outcrops"),
		AGRICULTURE("Agriculture"),
		URBAN("Urban"),
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
				 SWAMP, AQUATIC, DESERT, ROCKY_OUTCROPS, AGRICULTURE, URBAN, \
				  OTHERS]
		}
		
		private static def orderMap =  [(ALL):1, (FOREST):2, (THICKET):3, (SAVANNA):4, (GRASSLAND):5,   \
			(SWAMP):6, (AQUATIC):7, (DESERT):9, (ROCKY_OUTCROPS):8, (AGRICULTURE):10, (URBAN):11, (OTHERS):12]; 
		
		
		public String toString() {
			return this.value();
		}
		
		public static int getOrdering(HabitatType ht){
			return orderMap[ht];
		}
		
	};
	
	String name;
	int habitatOrder;
	
    static constraints = {
		name(blank:false, unique : true);
    }
	
	static mapping = {
		version false;
		sort habitatOrder:"asc"
	}
	
	Resource icon(ImageType type) {
		String name = this.name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
		name = ImageUtils.getFileName(name, type, '.png');

		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.resources.rootDir+"/group_icons/habitat/${name}")).exists()
		if(!iconPresent) {
			name = Habitat.findByName(HabitatType.OTHERS.value()).name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
			name = ImageUtils.getFileName(name, type, '.png');
		}
		return new Resource(fileName:"group_icons/habitat/${name}", type:ResourceType.ICON, title:"You can contribute!!!");
	}
	
	String iconClass() {
		return this.name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')+'_gall_th';
	}
}
