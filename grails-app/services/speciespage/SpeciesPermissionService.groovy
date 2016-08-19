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
import grails.plugin.springsecurity.SpringSecurityUtils;
import species.auth.Role;
import species.auth.SUserRole;
import species.participation.UserToken;
import species.SpeciesPermission;
import species.SpeciesPermission.PermissionType;
import grails.util.Environment;
import species.ScientificName.TaxonomyRank;
import species.sourcehandler.XMLConverter

class SpeciesPermissionService {

    static transactional = true

    def emailConfirmationService;
    def utilsService;
    def springSecurityService;

    List<SUser> getCurators(Species speciesInstance) {
        def result = getUsers(speciesInstance, PermissionType.ROLE_CURATOR) 
        return result
    }

    List<SUser> getContributors(Species speciesInstance) { 
        def result = getUsers(speciesInstance, PermissionType.ROLE_CONTRIBUTOR)
        return result
    } 

    List<SUser> getUsers(Species speciesInstance, SpeciesPermission.PermissionType permissionType) {
        def taxonConcept = species.taxonConcept;
        return getUsers(taxonConcept, permissionType);
    }

    List<SUser> getUsers(TaxonomyDefinition taxonConcept, SpeciesPermission.PermissionType permissionType) {
        List parentTaxons = taxonConcept.parentTaxon()
        def res = SpeciesPermission.findAllByPermissionTypeAndTaxonConceptInList(permissionType.toString(), parentTaxons)
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

    boolean addContributorToTaxonConcept(SUser author, TaxonomyDefinition taxonConcept) {
        return addTaxonUser(author, taxonConcept, SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR);
    }

    int addUsers(SUser author, List<Species> species, SpeciesPermission.PermissionType permissionType) {
        int n = 0
        species.each { spec -> 
            addUser(author, spec, permissionType)? n++ :''
        }
        return n
    }

    boolean addUser(SUser author, Species species, SpeciesPermission.PermissionType permissionType) {
        return addTaxonUser(author, species.taxonConcept, permissionType);
    }

    boolean addTaxonUser(SUser user, TaxonomyDefinition taxonConcept, SpeciesPermission.PermissionType permissionType){
        if(!isTaxonContributor(taxonConcept, user, [permissionType])) {
            log.info "adding taxon user ${user} to ${taxonConcept} with permission ${permissionType}"
            try{
                def newCon = new SpeciesPermission(author:user, taxonConcept : taxonConcept, permissionType: permissionType.toString())
                if(!newCon.save(flush:true)){
                    newCon.errors.allErrors.each { log.error it }
                    return false
                }

                return true;
            } catch (Exception e) {
                //e.printStackTrace();
                log.error "error adding ${permissionType} to the user. ${e.getMessage()}"
                return false;
            }
        } else { 
            //log.debug "${user} already has ${permissionType} for the taxon"
            return true;
        }
    }

    boolean isSpeciesContributor(Species speciesInstance, SUser user, List<PermissionType> permissionTypes= [SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR]) {
        return (isTaxonContributor(speciesInstance.taxonConcept, user, permissionTypes) || SpringSecurityUtils.ifAllGranted('ROLE_SPECIES_ADMIN'));
    }

    boolean isSpeciesContributor(List<String> taxonRegistryNames, SUser user, List<PermissionType> permissionTypes= [SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR]) {
        List<TaxonomyDefinition> parentTaxon = [];
        XMLConverter converter = new XMLConverter();
        taxonRegistryNames.eachWithIndex { tr,index ->
            println tr
            if(tr) {
            def t = converter.getTaxonConceptFromName(tr, index);
            if(t) parentTaxon << t            
            }
        }
        return (isTaxonContributor(parentTaxon, user, permissionTypes) || SpringSecurityUtils.ifAllGranted('ROLE_SPECIES_ADMIN'));
    }

    boolean isTaxonContributor(TaxonomyDefinition taxonConcept, SUser user, List<PermissionType> permissionTypes = [SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR]) {
        if(!user) return false;
        if(!taxonConcept) return false;
        List parentTaxons = taxonConcept.parentTaxon()
        parentTaxons.add(taxonConcept);
        return isTaxonContributor(parentTaxons, user, permissionTypes);
    }

    boolean isTaxonContributor(List<TaxonomyDefinition> parentTaxons, SUser user, List<PermissionType> permissionTypes = [SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR]) {
        if(!parentTaxons || !user || !permissionTypes) return false;
        def permissions = permissionTypes.collect {it.value()};
        def res = SpeciesPermission.withCriteria {
            eq('author', user)
            inList('permissionType', permissions)
            inList('taxonConcept',  parentTaxons)
        }
        println "Species Permission Result : ${res}"
        if((res && res.size() > 0)) {
            return true
        } else {
            return false
        }
    }

    boolean isSpeciesFieldContributor(SpeciesField speciesFieldInstance, SUser user) {
        if(!user) return false;
        boolean flag = false;
        speciesFieldInstance.contributors.each { c ->
            if((c.id == user.id) || (utilsService.isAdmin(user))) {
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
        return hasRoleFor(user, SpeciesPermission.PermissionType.ROLE_CURATOR);
    }
    
    List<TaxonomyDefinition> contributorFor(SUser user){
        return hasRoleFor(user, SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR);
    }

    List<TaxonomyDefinition> hasRoleFor(SUser user, SpeciesPermission.PermissionType permissionType){
        def result = SpeciesPermission.findAllByAuthorAndPermissionType(user, permissionType.toString())
        def res = []
        result.each {
            res << it.taxonConcept
        }
        return res
    }
/*
    String sendSpeciesCuratorInvitation(String selectedNodes, List<SUser> members, String domain, String message=null) {
        if(!selectedNodes) return "Please select a node";
        def rankLevel
        def rankArray = [];
        TaxonomyRank.each {
            rankArray << it.value()
        }

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
*/
    String sendPermissionInvitation(String selectedNodes, List<SUser> members, String domain, String invitetype, String message=null) {
        if(!selectedNodes) return "Please select a node";
        def rankLevel
        def rankArray = [];
        TaxonomyRank.each {
            rankArray << it.value()
        }

        String mailSubject = "Invitation for ${invitetype}"
        String msg = ""
        String usernameFieldName = 'name'
        def selNodes = selectedNodes.split(",")

        def selNodeTDs = [];

        selNodes.each { snid ->
            def sn = TaxonomyDefinition.get(snid.toLong())
            if(sn)
                selNodeTDs << sn;
        }

        //order selectedNodes by their rank;
        selNodeTDs = selNodeTDs.sort { a,b -> a.rank <=> b.rank }

        boolean alReq = false;

        members.each { mem ->
            def hadPermissionFor;
            if(invitetype == 'curator')
                hadPermissionFor = curatorFor(mem);
            else if(invitetype == 'contributor')
                hadPermissionFor = contributorFor(mem);
            else if(invitetype == 'taxon_curator')
                hadPermissionFor = hasRoleFor(mem, SpeciesPermission.PermissionType.ROLE_TAXON_CURATOR);
            else if(invitetype == 'taxon_editor')
                hadPermissionFor = hasRoleFor(mem, SpeciesPermission.PermissionType.ROLE_TAXON_EDITOR);

            hadPermissionFor = hadPermissionFor.id;

            selNodeTDs.each { sn ->
                def allParents = sn.parentTaxon()
                
                allParents = allParents - sn;

                boolean hasPermission = false, alreadyRequested = false;
                allParents.each { parent ->
                    if(hadPermissionFor && hadPermissionFor.contains(parent.id)) {
                        hasPermission = true;
                        return;
                    } else if(selNodeTDs.contains(parent)) {
                        alreadyRequested = true;
                        alReq = true;
                        return;
                    }
                }

                if(hasPermission) {
                    //he is already has permission for a parent node, no need to add for child node
                    rankLevel = rankArray[sn.rank]
                    msg += " ${mem.name} is already a ${invitetype} of " + rankLevel + " : ${sn.name}. ";
                    return msg
                }
                if(alreadyRequested) {
                    //invitation was already sent for parent node, no need to add for child node
                    rankLevel = rankArray[sn.rank]
//                    msg += " ${mem.name} has already requested ${invitetype} permission for its parent of " + rankLevel + " : ${sn.name}. ";
                    return msg
                }

                rankLevel = rankArray[sn.rank]
                def userToken = new UserToken(username: mem."$usernameFieldName", controller:'species', action:'confirmPermissionInviteRequest', params:['userId':mem.id.toString(), 'taxonConcept':sn.id.toString(), 'invitetype':invitetype]);
                userToken.save(flush: true)
                def userLanguage = utilsService.getCurrentLanguage();
                emailConfirmationService.sendConfirmation(mem.email,mailSubject,  [curator: mem, invitetype:invitetype, taxon:sn, domain:domain, rankLevel:rankLevel, view:'/emailtemplates/'+userLanguage.threeLetterCode+'/invitePermission', message:message], userToken.token);

                msg += " Successfully sent invitation to ${mem.name} for ${invitetype}ship of " + rankLevel + " : ${sn.name}. "           
            }

        }
        if(alReq) {
            msg += " As an invitation was sent for the parent other selected taxon names are ignored. ";
        }

        return msg
    }

    String sendPermissionRequest(String selectedNodes, List<SUser> members, String domain, String invitetype, String message=null) {
        if(!selectedNodes) return "Please select a node";
        def rankLevel
        def rankArray = [];
        TaxonomyRank.each {
            rankArray << it.value()
        }

        String mailSubject = "Request for ${invitetype}"
        String msg = ""
        String usernameFieldName = 'name'
        def selNodes = selectedNodes.split(",")

        def selNodeTDs = [];

        selNodes.each { snid ->
            def sn = TaxonomyDefinition.get(snid.toLong())
            if(sn)
                selNodeTDs << sn;
        }

        //order selectedNodes by their rank;
        selNodeTDs = selNodeTDs.sort { a,b -> a.rank <=> b.rank }

        boolean alReq = false;

        members.each { mem ->
            def hadPermissionFor;
            if(invitetype == 'curator')
                hadPermissionFor = curatorFor(mem);
            else if(invitetype == 'contributor')
                hadPermissionFor = contributorFor(mem);
            else if(invitetype == 'taxon_curator')
                hadPermissionFor = hasRoleFor(mem, SpeciesPermission.PermissionType.ROLE_TAXON_CURATOR);
            else if(invitetype == 'taxon_editor')
                hadPermissionFor = hasRoleFor(mem, SpeciesPermission.PermissionType.ROLE_TAXON_EDITOR);

            selNodeTDs.each { sn ->
                def allParents = sn.parentTaxon()

                allParents = allParents - sn;

                boolean hasPermission = false, alreadyRequested = false;
                allParents.each { parent ->
                    if(hadPermissionFor && hadPermissionFor.contains(parent)) {
                        hasPermission = true;
                        return;
                    } else if(selNodeTDs.contains(parent)) {
                        alreadyRequested = true;
                        alReq = true;
                        return;
                    }
                }

                    if(hasPermission) {
                        //he is already has permission for a parent node, no need to add for child node
                        rankLevel = rankArray[sn.rank]
                        msg += " ${mem.name} is already a ${invitetype} of " + rankLevel + " : ${sn.name} ";
                        return msg
                    }
                    if(alreadyRequested) {
                        rankLevel = rankArray[sn.rank]
                        return msg
                    }


                    rankLevel = rankArray[sn.rank]
                    def userToken = new UserToken(username: mem."$usernameFieldName", controller:'species', action:'confirmPermissionRequest', params:['userId':mem.id.toString(), 'taxonConcept':sn.id.toString(), 'invitetype':invitetype]);
                    userToken.save(flush: true)

                    List<SUser> speciesAdmins;
                    if (Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
                        speciesAdmins = SUserRole.findAllByRole(Role.findByAuthority("ROLE_SPECIES_ADMIN")).sUser
                    } else {
                        speciesAdmins = [springSecurityService.currentUser];
                    }
                    speciesAdmins.each {
                        def userLanguage = utilsService.getCurrentLanguage();
                        emailConfirmationService.sendConfirmation(it.email, mailSubject,  [admin: it, requester:mem, requesterUrl:utilsService.generateLink("SUser", "show", ["id": mem.id], null), invitetype:invitetype, taxon:sn, domain:domain, rankLevel:rankLevel, view:'/emailtemplates/'+userLanguage.threeLetterCode+'/requestPermission', 'message':message], userToken.token);
                    }

                    msg += " Successfully sent request for ${invitetype}ship of " + rankLevel + " : ${sn.name} "                        
                }
        }

        if(alReq) {
            msg += " As a request was sent for the parent, other selected taxon names are ignored. ";
        }

        return msg
    }

    boolean addCurator(SUser user, TaxonomyDefinition taxonConcept){
        def cu = SpeciesPermission.findWhere(author:user, taxonConcept : taxonConcept, permissionType: SpeciesPermission.PermissionType.ROLE_CURATOR.toString())
        if(!cu){
            try{
                def newCu = new SpeciesPermission(author:user, taxonConcept : taxonConcept, permissionType: SpeciesPermission.PermissionType.ROLE_CURATOR.toString())
                if(!newCu.save(flush:true)){
                    newCu.errors.allErrors.each { log.error it }
                    return false;
                } 

                return true;

            }catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error "error adding new CURATOR " + e.getMessage()
                return false
            }
        } else { 
            //log.debug "${user} is already a curator for ${taxonConcept}"
            return true;
        }

    }
}
