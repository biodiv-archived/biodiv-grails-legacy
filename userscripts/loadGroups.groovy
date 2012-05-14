import species.groups.SpeciesGroup;
import species.sourcehandler.XMLConverter;
import speciespage.GroupHandlerService;
import species.groups.SpeciesGroup;
import species.groups.SpeciesGroupMapping;

import species.Habitat;
import species.Habitat.HabitatType;

//def s = ctx.getBean("groupHandlerService");
//s.updateGroups();

def habitat = Habitat.findByName(HabitatType.OTHERS.toString())
habitat.habitatOrder =  HabitatType.getOrdering(HabitatType.OTHERS);
if(!habitat.save(flush:true)) {
	habitat.errors.allErrors.each { println it }
}

habitat = (new Habitat(name:HabitatType.URBAN.toString(), habitatOrder:HabitatType.getOrdering(HabitatType.URBAN)))
if(!habitat.save(flush:true)) {
	habitat.errors.allErrors.each { println it }
}