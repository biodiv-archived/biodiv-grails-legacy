package speciespage

import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;

import species.Classification;
import species.Species;
import species.TaxonomyDefinition;
import species.ScientificName.TaxonomyRank;
import species.TaxonomyRegistry;
import species.formatReader.SpreadsheetReader;
import species.groups.SpeciesGroup;
import species.groups.SpeciesGroupMapping;
import species.sourcehandler.XMLConverter;
import species.SynonymsMerged;
import groovy.sql.Sql;
import species.NamesMetadata.NameStatus;

class GroupHandlerService {

	static transactional = false

	def grailsApplication
	def sessionFactory
	def utilsService

    def dataSource

	static int BATCH_SIZE = 50;

	def speciesGroupMappings;


	/**
	 * 
	 * @param file
	 * @param contentSheetNo
	 * @param contentHeaderRowNo
	 * @return
	 */
	def loadGroups(String file, contentSheetNo, contentHeaderRowNo) {
		log.info "Loading groups and their association with species";
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		//TODO:sort groups in name and rank order
		content.each { row ->
			String name = row.get("group");
			String canonicalName = row.get("name");
			String rank = row.get("rank");
			String parentGroupName = row.get("parent group");

			int taxonRank = XMLConverter.getTaxonRank(rank);
			addGroup(name, parentGroupName, canonicalName, taxonRank);
		}
		updateGroups();
	}

	/**
	 * 
	 * @param name
	 * @param parentGroupName
	 * @param taxonName
	 * @param taxonRank
	 * @return
	 */
	SpeciesGroup addGroup(String name, String parentGroupName, String taxonName, int taxonRank) {
		SpeciesGroup parentGroup = SpeciesGroup.findByName(parentGroupName);
		TaxonomyDefinition taxonConcept = TaxonomyDefinition.findByCanonicalFormAndRank(taxonName, taxonRank);
		SpeciesGroupMapping speciesGroupMapping = new SpeciesGroupMapping(taxonName:taxonName, rank:taxonRank, taxonConcept:taxonConcept);
		return addGroup(name, parentGroup, speciesGroupMapping);
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
	 * Updates group for all species by going along its hierarchy and checking 
	 * parent if at any level has a corresponding group mapping.
	 * A species should not have multiple paths in the same classification 
	 * @return
     */
    int updateGroups(boolean runForSynonyms=false) {
        int noOfUpdations = 0;
        int offset = 0;
        int limit = BATCH_SIZE;

        def taxonConcepts;

        long startTime = System.currentTimeMillis();
        int count = 0;

        //        int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
        //        dataSource.setUnreturnedConnectionTimeout(500);

        def conn;
        while(true) {
            try {

                conn = new Sql(dataSource)

                if(runForSynonyms) {
                    taxonConcepts = conn.rows("select id from taxonomy_definition as t where t.status = '"+NameStatus.SYNONYM.value().toUpperCase()+"' and t.rank >= "+TaxonomyRank.SPECIES.ordinal()+" order by t.id asc limit "+limit+" offset "+offset);
                } else {
                    taxonConcepts = conn.rows("select id from taxonomy_definition as t where t.rank >= "+TaxonomyRank.SPECIES.ordinal()+" order by t.id asc limit "+limit+" offset "+offset);
                }
                println taxonConcepts
                TaxonomyDefinition.withNewTransaction {
                    taxonConcepts.each { taxonConceptRow ->
                        def taxonConcept = TaxonomyDefinition.get(taxonConceptRow.id);
                        //if(!taxonConcept.group && 
                        if(updateGroup(taxonConcept)) {
                            count ++;
                        }
                    }


                    if(count && count == BATCH_SIZE) {
                        cleanUpGorm();
                        noOfUpdations += count;
                        count = 0;
                        log.info "Updated group for taxonConcepts ${noOfUpdations}"
                    }
                }

            } catch(Exception e) {
                println e.printStackTrace();
            } finally {
                //            log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
                //            dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
                conn.close()
            }
            if(!taxonConcepts) break;
            taxonConcepts.clear();
            offset += limit;
        }

		if(count) {
			cleanUpGorm();
			noOfUpdations += count;
		}
		
		log.info "Updated group for taxonConcepts ${noOfUpdations} in total"
		log.info "Time taken to update groups for taxonConcepts ${noOfUpdations} is ${System.currentTimeMillis()-startTime}(msec)";
		return noOfUpdations;
	}

	/**
	 * Updates group for all species by going along its hierarchy and checking
	 * parent if at any level has a corresponding group mapping.
	 * A species should not have multiple paths in the same classification
	 * @return
	 */
	int updateGroups(List<Species> species, boolean flush) {
		int noOfUpdations = 0;

		species.each { s ->
			if(updateGroup(s.taxonConcept)) {
				noOfUpdations ++;
			}
			if(noOfUpdations % BATCH_SIZE == 0 && flush) {
				cleanUpGorm();
			}
		}

		if(noOfUpdations && flush) {
			cleanUpGorm();
		}
		return noOfUpdations;
	}
	
	/**
	 * Tries to deduce group for the taxon concept based on its hierarchy 
	 * and updates group for itself and all of its child concepts   
	 */
	boolean updateGroup(TaxonomyDefinition taxonConcept) {
		//parentTaxon has hierarchies from all classifications
        def classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);
        def ibpParentTaxon;
        if(taxonConcept instanceof SynonymsMerged) {
            def acceptedTaxonConcept = taxonConcept.fetchAcceptedNames()[0];
			acceptedTaxonConcept.postProcess()
            ibpParentTaxon = acceptedTaxonConcept.parentTaxonRegistry(classification).values()[0];
        } else {
			taxonConcept.postProcess()
            ibpParentTaxon = taxonConcept.parentTaxonRegistry(classification).values()[0];
        }
		
		if(ibpParentTaxon) {
            return updateGroup(taxonConcept, getGroupByHierarchy(taxonConcept, ibpParentTaxon));
        } else {
            log.error "No IBP parent taxon for  ${taxonConcept}"
            return false;
        }
	}

	/**
	 * Updates group for taxonConcept and all concepts below this under any of the hierarchies
	 * @param taxonConcept
	 * @param group
	 * @return
	 */
	boolean updateGroup(TaxonomyDefinition taxonConcept, SpeciesGroup group) {
		log.info "Updating group associations for taxon concept : "+taxonConcept + " to ${group}";
		int noOfUpdations = 0;

		if(taxonConcept) {// && group) {

//			if(!group.equals(taxonConcept.group)) {
				taxonConcept.group = group;
				if(taxonConcept.save(flush:true)) {
					log.info "Setting group '${group?.name}' for taxonConcept '${taxonConcept?.name}'"
					noOfUpdations++;
				} else {
					taxonConcept.errors.allErrors.each { log.error it }
				}
//			}
		}
		return noOfUpdations ?: false;
	}

	/**
	 * returns the groups if there is a match with mappings defined 
	 */
	private SpeciesGroup getGroupByMapping(TaxonomyDefinition taxonConcept) {
		SpeciesGroup group;
		if(!speciesGroupMappings) {
			speciesGroupMappings = SpeciesGroupMapping.listOrderByRank('desc');
		}
		
		speciesGroupMappings.each { mapping ->
			if((taxonConcept.name.trim().equals(mapping.taxonName)) && taxonConcept.rank == mapping.rank) {
				group = SpeciesGroup.read(mapping.speciesGroup.id);
				if(!group.isAttached()) {
					group.attach();
				}
			}
		}
		return group;
	}

	/**
	 * returns the group for the closest ancestor.
	 * 
	 */
	public SpeciesGroup getGroupByHierarchy(TaxonomyDefinition taxonConcept, List<TaxonomyDefinition> parentTaxon) {
		int rank = TaxonomyRank.KINGDOM.ordinal();

		SpeciesGroup group;
		parentTaxon.sort { it.rank };

		log.debug "Parent Taxon : "+parentTaxon 
		for(int i=parentTaxon.size() -1; i>=0; i--) {
			group = getGroupByMapping(parentTaxon.get(i))
			if(group) {
				break;
			}
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
			try {
				hibSession.flush()
			} catch(ConstraintViolationException e) {
				e.printStackTrace()
			}
			hibSession.clear()
			speciesGroupMappings.each { mapping ->
				if(!mapping.isAttached()) {
					mapping.attach();
				}
			}			
		}
	}
}
