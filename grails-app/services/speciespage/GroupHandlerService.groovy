package speciespage

import org.apache.commons.logging.LogFactory;

import species.Classification;
import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;
import species.TaxonomyRegistry;
import species.formatReader.SpreadsheetReader;
import species.groups.SpeciesGroup;
import species.groups.SpeciesGroupMapping;
import species.sourcehandler.XMLConverter;

class GroupHandlerService {

	static transactional = false

	private static final log = LogFactory.getLog(this);

	def grailsApplication
	def sessionFactory

	static int GROUP_UPDATION_BATCH = 20;
	
	/**
	 * 
	 * @param file
	 * @param contentSheetNo
	 * @param contentHeaderRowNo
	 * @return
	 */
	def loadGroups(String file, contentSheetNo, contentHeaderRowNo) {
		log.debug "Loading groups and their association with species";
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		//TODO:sort groups in name and rank order
		content.each { row ->
			String name = row.get("group");
			String canonicalName = row.get("name");
			String rank = row.get("rank");
			String parentGroupName = row.get("parent group");

			int taxonRank = XMLConverter.getTaxonRank(rank);
			TaxonomyDefinition taxonConcept = TaxonomyDefinition.findByCanonicalFormAndRank(canonicalName, taxonRank);
			SpeciesGroup parentGroup = SpeciesGroup.findByName(parentGroupName);
			SpeciesGroupMapping speciesGroupMapping = new SpeciesGroupMapping(taxonName:canonicalName, rank:taxonRank, taxonConcept:taxonConcept);
			SpeciesGroup group = addGroup(name, parentGroup, speciesGroupMapping);
			if(group && taxonConcept) {
				updateGroup(taxonConcept, group);
			}
		}
	}

	/**
	 * Adds a group with given name and associates given mappings
	 * @param name
	 * @param parentGroup
	 * @return
	 */
	SpeciesGroup addGroup(String name, SpeciesGroup parentGroup, SpeciesGroupMapping speciesGroupMapping) {
		if(name) {
			SpeciesGroup group = SpeciesGroup.findByName(name);

			if(!group) {
				group = new SpeciesGroup(name:name, parentGroup:parentGroup);
			}

			def mapping = SpeciesGroupMapping.findByTaxonNameAndRank(speciesGroupMapping.taxonName, speciesGroupMapping.rank);
			if(!mapping) {
				group.addToSpeciesGroupMapping(speciesGroupMapping);
				if(!group.save(flush:true)) {
					log.error "Unable to save group : "+name;
					group.errors.allErrors.each { log.error it }
				}
			}
			return group;
		}
		else {
			log.error "Group name cannot be empty";
		}
	}

	/**
	 * Tries to deduce group for the taxon concept based on its hierarchy 
	 * and updates group for itself and all of its child concepts   
	 */
	int updateGroup(TaxonomyDefinition taxonConcept) {
		return updateGroup(taxonConcept, getGroup(taxonConcept));
	}
	
	/**
	 * Updates group for taxonConcept and all concepts below this under any of the hierarchies
	 * @param taxonConcept
	 * @param group
	 * @return
	 */
	int updateGroup(TaxonomyDefinition taxonConcept, SpeciesGroup group) {
		log.debug "Updating group associations for taxon concept : "+taxonConcept;
		int noOfUpdations = 0;

		if(taxonConcept && group) {
			def startTime = System.currentTimeMillis();

			if(!group.equals(taxonConcept.group)) {
				taxonConcept.group = group;
				if(taxonConcept.save()) {
					log.debug "Setting group '${group.name}' for taxonConcept '${taxonConcept.name}'"
					noOfUpdations++;
				} else {
					taxonConcept.errors.allErrors.each { log.error it }
				}
			}

			List batch = new ArrayList();
			TaxonomyRegistry.findAllByTaxonDefinition(taxonConcept).each { TaxonomyRegistry reg ->
				//TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
				def c = TaxonomyRegistry.createCriteria();
				def r = c.scroll {
					sqlRestriction ("path like '"+reg.path.replaceAll("_", "!_")+"!_%' escape '!'");
				}
				
				//updating group for all child taxon
				boolean flag = false;
				while(r.next()) {
					TaxonomyDefinition concept = r.get(0).taxonDefinition;
					if(!group.equals(concept.group)) {
						concept.group = group;
						if(concept.save()) {
							log.debug "Setting group '${group.name}' for taxonConcept '${concept.name}'"
							noOfUpdations++;
							flag = true;
						} else {
							concept.errors.allErrors.each { log.error it }
							log.error "Coundn't update group for concept : "+concept;
						}
					} 
					if(flag && noOfUpdations % GROUP_UPDATION_BATCH == 0) {
						log.debug "Saved group for ${noOfUpdations} taxonConcepts"
						cleanUpGorm();
						flag = false;
					}
				}
				if(noOfUpdations && flag) {
					log.debug "Saved group for ${noOfUpdations} taxonConcepts"
					cleanUpGorm();
				}
				r.close();
			}
			log.debug "Time taken to save : "+((System.currentTimeMillis() - startTime)/1000) + "(sec)"
			log.debug "Updated group for ${noOfUpdations} taxonConcentps in total"
		}
		return noOfUpdations;
	}

	/**
	 * Looking at taxonConcept hierarchy a group is determined and returned 
	 * @param taxonConcept
	 * @return
	 */
	SpeciesGroup getGroup(TaxonomyDefinition taxonConcept) {
		if(taxonConcept.group) {
			log.debug "Returning already assigned spcies group"
			return taxonConcept.group;
		} else {
			SpeciesGroup group = getGroupByMapping(taxonConcept);
			if(group) {
				return group;
			} else {
				return getGroupByHierarchy(taxonConcept, taxonConcept.parentTaxon());
			}
		}
	}

	/**
	 * returns the groups if there is a match with mappings defined 
	 */
	private SpeciesGroup getGroupByMapping(TaxonomyDefinition taxonConcept) {
		log.debug "Finding if the taxon name has a species_group_mapping"
		def speciesGroupMappings = SpeciesGroupMapping.listOrderByRank('desc');
		SpeciesGroup group;
		speciesGroupMappings.each { mapping ->
			if((taxonConcept == mapping.taxonConcept || taxonConcept.name.equals(mapping.taxonName)) && taxonConcept.rank == mapping.rank) {
				group = mapping.speciesGroup;
			}
		}
		return group;
	}

	/**
	* returns the group for the closest ancestor.
	*/
   private SpeciesGroup getGroupByHierarchy(TaxonomyDefinition taxonConcept, List<TaxonomyDefinition> parentTaxon) {
	   log.debug "Trying to derive species group from hierarchy. Finding group for closest ancestor"
	   int rank = TaxonomyRank.KINGDOM.ordinal();
	   SpeciesGroup group;
	   parentTaxon.each {parentTaxonDefinition ->
		   if(parentTaxonDefinition.group && parentTaxonDefinition.rank < taxonConcept.rank && parentTaxonDefinition.rank > rank) {
			   rank = parentTaxonDefinition.rank;
			   group = parentTaxonDefinition.group;
		   }
		   //TODO: can update group for parents as well
	   }
	   return group;
   }
	/**
	 *
	 */
	private void cleanUpGorm() {
		def hibSession = sessionFactory?.getCurrentSession()
		if(hibSession) {
			log.debug "Flushing and clearing session"
			hibSession.flush()
			hibSession.clear()
		}
	}
}
