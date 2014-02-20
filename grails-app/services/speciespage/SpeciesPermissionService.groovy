package speciespage

import java.util.Date;
import java.util.List;
import java.util.Map;

import species.TaxonomyDefinition;
import species.auth.SUser;
import species.SpeciesPermission;
import species.Field;
import species.SpeciesField;
import species.Species;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import species.auth.Role;
import species.auth.SUserRole;

class SpeciesPermissionService {

    static transactional = true

    def springSecurityService;

    List<SUser> getCurators(Species speciesInstance) {
        def result = getUsers(speciesInstance, PermissionType.ROLE_CURATOR) 
        return result
    }

    List<SUser> getContributors(Species speciesInstance) { 
        def result = getUsers(speciesInstance, PermissionType.ROLE_CONTRIBUTOR)
        return result
    }

    private List getUsers(Species speciesInstance, permissionType) {
        def taxonConcept = species.taxonConcept;
        List parentTaxons = taxonConcept.parentTaxon()
        def res = SpeciesPermission.findAllByPermissionTypeAndTaxonConceptInList(permissionType, parentTaxons)
        def result = []
        res.each { r-> 
            result.add(r.author)            
        }
        return result
    }

    boolean addCurator(SUser author, List<Species> species) {
        def result = addUsers(author, species, PermissionType.ROLE_CURATOR)
        return result
    }

    boolean addContributor(SUser author, List<Species> species) {
        def result = addUsers(author, species, PermissionType.ROLE_CONTRIBUTOR)
        return result        
    }

    private boolean addUsers(SUser author, List<Species> species, permissionType) {
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

    boolean isSpeciesContributor(Species speciesInstance, SUser user) {
        if(!user) return false;
        if(SpringSecurityUtils.ifAllGranted('ROLE_SPECIES_ADMIN')) 
            return true;
        else {
            def taxonConcept = speciesInstance.taxonConcept;
            List parentTaxons = taxonConcept.parentTaxon()
            def res = SpeciesPermission.withCriteria {
                eq('author', user)
                inList('permissionType', [SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR.value(),SpeciesPermission.PermissionType.ROLE_CURATOR.value()])
                inList('taxonConcept',  parentTaxons)
            }
            if(res && res.size() > 0)
                return true
            else
                return false
        }
        return false;
    }

    boolean isSpeciesFieldContributor(SpeciesField speciesFieldInstance, SUser user) {
        if(!user) return false;
        boolean flag = false;
        speciesFieldInstance.contributors.each { c ->
            if(c.name == user.username) {
                flag = true;
                return
            }
        }
        return flag;
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
