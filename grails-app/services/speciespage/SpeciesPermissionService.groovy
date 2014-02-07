package speciespage

import java.util.Date;
import java.util.List;
import java.util.Map;

import species.TaxonomyDefinition;
import species.auth.SUser;
import species.SpeciesPermission;
import species.auth.Role;
import species.auth.SUserRole;


class SpeciesPermissionService {

    static transactional = true


    List<SUser> getCurators(speciesInstance) {
        def result = getUsers(speciesInstance, PermissionType.ROLE_CURATOR) 
        return result
    }

    List<SUser> getContributors(speciesInstance) { 
        def result = getUsers(speciesInstance, PermissionType.ROLE_CONTRIBUTOR)
        return result
    }

    private List getUsers(speciesInstance, permissionType) {
        def taxonConcept = species.taxonConcept;
        List parentTaxons = taxonConcept.parentTaxon()
        def res = SpeciesPermission.findAllByPermissionTypeAndTaxonConceptInList(permissionType, parentTaxons)
        def result = []
        res.each { r->
            result.add(r.author)            
        }
        return result
    }

    boolean addCurator(SUser author, List species) {
        def result = addUsers(author, species, PermissionType.ROLE_CURATOR)
        return result
    }

    boolean addContributor(SUser author, List species) {
        def result = addUsers(author, species, PermissionType.ROLE_CONTRIBUTOR)
        return result        
    }

    private boolean addUsers(SUser author, List species, permissionType) {
        species.each { spec ->
            def taxon = spec.taxonConcept
            try{
                def sp = new SpeciesPermission(author: author, taxonConcept: taxon, permissionType: permissionType);
                if(!sp.save(flush:true)){
                    sp.errors.allErrors.each { log.error it }
                    return false
                }
            } catch (Exception e) {
                status = false;
                msg = "Error: ${e.getMessage()}";
                e.printStackTrace()
                log.error e.getMessage();
            } 
        }
        return true
    }

    List<SUser> getSpeciesAdmin (){
        def role = Role.findByAuthority("ROLE_SPECIES_ADMIN")
        def suserRole = SUserRole.findAllByRole(role)
        def speciesAdmins = []
        suserRole.each{
            speciesAdmins.add(it.sUser)
        }
        return speciesAdmins
    }

}
