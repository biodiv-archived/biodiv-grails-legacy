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
import species.participation.UserToken;
import species.SpeciesPermission;

class SpeciesPermissionService {

    static transactional = true

    def springSecurityService;
    def emailConfirmationService;

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
            parentTaxons.add(taxonConcept);
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
            if(c.id == user.id) {
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

    List<TaxonomyDefinition> curatorFor(SUser user){
        def result = SpeciesPermission.findAllWhere(author: user, permissionType: SpeciesPermission.PermissionType.ROLE_CURATOR.toString())
        def res = []
        result.each{
            res << it.taxonConcept
        }
        return res
    }
    
    List<TaxonomyDefinition> contributorFor(SUser user){
        def result = SpeciesPermission.findAllByAuthorAndPermissionType(user, SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR)
        def res = []
        result.each{
            res << it.taxonConcept
        }
        return res
    }

    def sendSpeciesCuratorInvitation(selectedNodes, members, domain, message=null) {
        String msg = ""
        String usernameFieldName = 'name'
        def selNodes = selectedNodes.split(",")
        members.each { mem ->
            def curatorFor = curatorFor(mem)
            selNodes.each { snid ->
                def sn = TaxonomyDefinition.get(snid.toLong())
                def allParents = sn.parentTaxon()
                if(curatorFor) {
                    if(curatorFor.intersect(allParents)) {
                        //he is already curator of a parent node, no need to add for child node
                        msg += " ${mem.name} is already a curator for ${sn.name} ";
                        return msg
                    }
                    else {
                        def userToken = new UserToken(username: mem."$usernameFieldName", controller:'species', action:'confirmCuratorInviteRequest', params:['userId':mem.id.toString(), 'taxonConcept':sn.id.toString()]);
                        userToken.save(flush: true)
                        emailConfirmationService.sendConfirmation(mem.email,"Invite for Curatorship",  [curator: mem, domain:domain, view:'/emailtemplates/requestPermission'], userToken.token);
                        msg += " Successfully sent invitation to ${mem.name} for curatorship of ${sn.name} "                        
                    }

                }
                else{
                    def userToken = new UserToken(username: mem."$usernameFieldName", controller:'species', action:'confirmCuratorInviteRequest', params:['userId':mem.id.toString(), 'taxonConcept':sn.id.toString()]);
                    userToken.save(flush: true)
                    emailConfirmationService.sendConfirmation(mem.email,"Invite for Curatorship",  [curator: mem, domain: domain, view:'/emailtemplates/requestPermission'], userToken.token);
                    msg += " Successfully sent invitation to ${mem.name} for curatorship of ${sn.name} "
                }

            }
        }
        return msg
    }
    
    void addCurator(SUser user, TaxonomyDefinition taxonConcept){
        def cu = SpeciesPermission.findWhere(author:user, taxonConcept : taxonConcept, permissionType: SpeciesPermission.PermissionType.ROLE_CURATOR.toString())
        if(!cu){
            try{
                def newCu = new SpeciesPermission(author:user, taxonConcept : taxonConcept, permissionType: SpeciesPermission.PermissionType.ROLE_CURATOR.toString())
                if(!newCu.save(flush:true)){
                    newCu.errors.allErrors.each { println it }
                    return null
                }

            }catch (org.springframework.dao.DataIntegrityViolationException e) {
                println "error adding new CURATOR " + e
            }
        }
    }

    void addContributor(SUser user, TaxonomyDefinition taxonConcept){
        def con = SpeciesPermission.findWhere(author:user, taxonConcept : taxonConcept, permissionType: SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR.toString())
        if(!con){
            try{
                def newCon = new SpeciesPermission(author:user, taxonConcept : taxonConcept, permissionType: SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR.toString())
                if(!newCon.save(flush:true)){
                    newCon.errors.allErrors.each { println it }
                    return null
                }

            }catch (org.springframework.dao.DataIntegrityViolationException e) {
                println "error adding new CONTRIBUTOR " + e
            }
        }
    }


}
