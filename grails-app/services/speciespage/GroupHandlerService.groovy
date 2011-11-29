package speciespage

import org.apache.commons.logging.LogFactory;

import species.Classification;
import species.SpeciesGroup;
import species.TaxonomyDefinition;
import species.TaxonomyRegistry;
import species.formatReader.SpreadsheetReader;
import species.sourcehandler.XMLConverter;

class GroupHandlerService {

	static transactional = false

	private static final log = LogFactory.getLog(this);

	def grailsApplication
	def sessionFactory

	/**
	 * 
	 * @param name
	 * @param taxonConcept
	 * @param classification
	 * @param parentGroup
	 * @return
	 */
	SpeciesGroup addGroup(String name, SpeciesGroup parentGroup) {

		if(name) {
			SpeciesGroup group = SpeciesGroup.findByName(name);

			if(!group) {
				if(parentGroup) {
					group = new SpeciesGroup(name:name, parentGroup:parentGroup);
					if(group.save(flush:true)) {
						//success = true;
					} else {
						log.error "Unable to save group : "+name;
					}
				} else {
					log.error "Coudn't save the group as parentGroupName is inappropriate"
				}
			}
			return group;
		}
		else {
			log.error "Group is not defined : " + name;
		}
	}

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
			SpeciesGroup group = addGroup(name, parentGroup);
			if(group && taxonConcept) {
				updateGroup(taxonConcept, group);
			} 
		}
	}

	int updateGroup(TaxonomyDefinition taxonConcept, SpeciesGroup group) {
		log.debug "Updating group associations for taxon concept : "+taxonConcept;
		int noOfUpdations = 0;

		if(taxonConcept && group) {
			def startTime = System.currentTimeMillis();
			
			taxonConcept.group = group;
			if(taxonConcept.save()) {
				noOfUpdations++;
			} else {
				taxonConcept.errors.allErrors.each { log.error it }
			}

			List childConcepts = getChildConcepts(taxonConcept);
			childConcepts.eachWithIndex { TaxonomyRegistry registry, index ->
				TaxonomyDefinition concept = registry.taxonDefinition;
				if(concept.group != group) {
					concept.group = group;
					if(concept.save()) {
						noOfUpdations++;
					} else {
						concept.errors.allErrors.each { log.error it }
						log.error "Coundn't update group for concept : "+concept;
					}
				}
				if (index % 20 == 0) {
					log.debug "Saved group for ${index} taxonConcepts"
					cleanUpGorm();
				}
			}
			cleanUpGorm();
			log.debug "Time taken to save : "+((System.currentTimeMillis() - startTime)/1000) + "(sec)"
			log.debug "Updated group for ${noOfUpdations} taxonConcentps in total"
		}
		return noOfUpdations;
	}

	/**
	 * 
	 * @param taxonConcept
	 * @return
	 */
	List<TaxonomyDefinition> getChildConcepts(TaxonomyDefinition taxonConcept) {
		List<TaxonomyDefinition> result = [];
		if(taxonConcept) {
			TaxonomyRegistry.findAllByTaxonDefinition(taxonConcept).each { reg ->
				//TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
				def c = TaxonomyRegistry.createCriteria();
				def r = c.list {
					sqlRestriction ("path like '"+reg.path.replaceAll("_", "!_")+"!_%' escape '!'");
				}
				result.addAll(r);
			}
		}
		return result;
	}

	/**
	 * Looking at taxonConcept hierarchy a group is determined and returned 
	 * @param taxonConcept
	 * @return
	 */
	SpeciesGroup getGroup(TaxonomyDefinition taxonConcept) {
		if(taxonConcept.group) {
			return taxonConcept.group;
		} else {
			//get hierarchy & chk for it in groups
		}
		return null;
	}

	/**
	 * 
	 */
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
