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
import species.SpeciesPermission.PermissionType;

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

    int addContributors(Species speciesInstance, List<SUser> contributors) {
        if(!speciesInstance || !contributors) return 0;
        int n = 0;
        contributors.each { c ->
            addContributorToSpecies(c, speciesInstance) ? n++ : '';
        }
        return n;
    }

    int addContributor(SUser author, List<Species> species) {
        return addUsers(author, species, SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR)
    }

    boolean addContributorToSpecies(SUser author, Species species) {
        return addUser(author, species, SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR)
    }

    private int addUsers(SUser author, List<Species> species, SpeciesPermission.PermissionType permissionType) {
        int n = 0
        species.each { spec -> 
            addUser(author, spec, permissionType)? n++ :''
        }
        return n
    }

    private boolean addUser(SUser author, Species species, SpeciesPermission.PermissionType permissionType) {
        return addTaxonUser(author, species.taxonConcept, permissionType);
    }

    private boolean addTaxonUser(SUser user, TaxonomyDefinition taxonConcept, SpeciesPermission.PermissionType permissionType){
        if(!isTaxonContributor(taxonConcept, user, [permissionType])) {
            try{
                def newCon = new SpeciesPermission(author:user, taxonConcept : taxonConcept, permissionType: SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR.toString())
                if(!newCon.save(flush:true)){
                    newCon.errors.allErrors.each { log.error it }
                    return false
                }
                return true;
            } catch (Exception e) {
                log.error "error adding new contributor ${e.getMessage()}"
                return false;
            }
        } else 
            return true;
    }

    boolean isSpeciesContributor(Species speciesInstance, SUser user, List<PermissionType> permissionTypes= [SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR]) {
        return isTaxonContributor(speciesInstance.taxonConcept, user, permissionTypes);
    }

    boolean isTaxonContributor(TaxonomyDefinition taxonConcept, SUser user, List<PermissionType> permissionTypes) {
        if(!user) return false;
        if(SpringSecurityUtils.ifAllGranted('ROLE_SPECIES_ADMIN')) 
            return true;
        else {
            List parentTaxons = taxonConcept.parentTaxon()
            parentTaxons.add(taxonConcept);
            def permissions = permissionTypes.collect {it.value()};
            def res = SpeciesPermission.withCriteria {
                eq('author', user)
                inList('permissionType', permissions)
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
        def rankLevel
        def rankArray = ["Kingdom", "Phylum", "Class", "Order", "Family", "Sub Family", "Genus", "Sub Genus", "Species"]
        String mailSubject = "Invitation for curatorship"
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
                        rankLevel = rankArray[sn.rank]
                        msg += " ${mem.name} is already a curator of " + rankLevel + " : ${sn.name} ";
                        return msg
                    }
                    else {
                        rankLevel = rankArray[sn.rank]
                        def userToken = new UserToken(username: mem."$usernameFieldName", controller:'species', action:'confirmCuratorInviteRequest', params:['userId':mem.id.toString(), 'taxonConcept':sn.id.toString()]);
                        userToken.save(flush: true)
                        emailConfirmationService.sendConfirmation(mem.email,mailSubject,  [curator: mem,taxon:sn, domain:domain, rankLevel:rankLevel, view:'/emailtemplates/requestPermission'], userToken.token);
                        
                        msg += " Successfully sent invitation to ${mem.name} for curatorship of " + rankLevel + " : ${sn.name} "                        
                    }

                }
                else{
                    rankLevel = rankArray[sn.rank]
                    def userToken = new UserToken(username: mem."$usernameFieldName", controller:'species', action:'confirmCuratorInviteRequest', params:['userId':mem.id.toString(), 'taxonConcept':sn.id.toString()]);
                    userToken.save(flush: true)
                    emailConfirmationService.sendConfirmation(mem.email, mailSubject ,  [curator: mem, ,taxon:sn , domain: domain, rankLevel:rankLevel, view:'/emailtemplates/requestPermission'], userToken.token);
                    msg += " Successfully sent invitation to ${mem.name} for curatorship of " + rankLevel + " : ${sn.name} "                
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
                    newCu.errors.allErrors.each { log.error it }
                    return null
                }

            }catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error "error adding new CURATOR " + e.getMessage()
            }
        }
    }
}
