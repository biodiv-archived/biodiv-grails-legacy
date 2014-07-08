import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import species.Field;
import species.UserGroupTagLib;
import species.Synonyms;
import species.CommonNames;
import species.TaxonomyDefinition;
import species.auth.Role
import species.auth.SUser
import species.auth.SUserRole
import species.groups.SpeciesGroup;
import species.Habitat;
import species.License;
import species.groups.UserGroupController;
import species.groups.UserGroup;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import species.participation.UserToken;
import species.participation.Recommendation;
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter;
import grails.converters.JSON;
import species.participation.Featured;
import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;
import species.TaxonomyRegistry;
import species.Classification;

class BootStrap {

	private static final log = LogFactory.getLog(this);
	
	def grailsApplication
	def setupService;
	def namesIndexerService
	def navigationService
	def springSecurityService
	def emailConfirmationService
	def userGroupService
	/**
	 * 
	 */
	def init = { servletContext ->
		//grailsApplication.config.'grails.web.disable.multipart' = true
		initDefs();
		initUsers();
		//initGroups();
		initNames();
		initFilters();
		initEmailConfirmationService();
        initJSONMarshallers();
       	}

	def initDefs() {
		if(Field.count() == 0) {
			log.debug ("Initializing db.")
			setupService.setupDefs()
		}
	}
	
	/**
	 * 
	 * @return
	 */
	def initUsers() {
		createOrUpdateUser('admin@strandls.com', 'admin', true);
//		createOrUpdateUser('sravanthi', 'sra123', true);
//		createOrUpdateUser('janaki', 'janaki', false);
//		createOrUpdateUser('prabha', 'prabha', false);
//		createOrUpdateUser('rahool', 'rahool', false);
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param isAdmin
	 */
	private void createOrUpdateUser(email, password, boolean isAdmin) {
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(flush:true, failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(flush:true, failOnError: true)
		def cepfAdminRole = Role.findByAuthority('ROLE_CEPF_ADMIN') ?: new Role(authority: 'ROLE_CEPF_ADMIN').save(flush:true, failOnError: true)
		def speciesAdminRole = Role.findByAuthority('ROLE_SPECIES_ADMIN') ?: new Role(authority: 'ROLE_SPECIES_ADMIN').save(flush:true, failOnError: true)
		
		
		def user = SUser.findByEmail(email) ?: new SUser(
				email: email,
				password: password,
				enabled: true).save(failOnError: true)

		if (!user.authorities.contains(userRole)) {
			SUserRole.create user, userRole
		}

		if(isAdmin) {
			if (!user.authorities.contains(adminRole)) {
				SUserRole.create user, adminRole
			}
		}
		
		UserGroupMemberRoleType.each { it ->
			Role.findByAuthority(it.value()) ?: new Role(authority: it.value()).save(flush:true, failOnError: true)
		}
	}

	/**
	 * 	
	 * @return
	 */
	def initGroups() {
		def groups = SpeciesGroup.list();
		def subItems = [];
		def allGroup;
		groups.eachWithIndex { SpeciesGroup group, index ->
			subItems.add([controller:'speciesGroup', title:group.name, order:index, action:'show', params:[id:group.id], path:'show/'+group.id]);
			if(group.name.equalsIgnoreCase("All")) {
				allGroup = group;
			}
		}
		navigationService.registerItem('dashboard', [controller:'speciesGroup', order:30, title:'Groups', action:'list', path:(allGroup)?'show/'+allGroup.id:'list', subItems:subItems])
		navigationService.updated()
	}

	/**
	 * 
	 * @return
	 */
	def initNames() {
		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		namesIndexerService.load(indexStoreDir);
	}

	/**
	 * 
	 */
	def initFilters() {
		if(grailsApplication.config.checkin.drupal) {
			SpringSecurityUtils.clientRegisterFilter('drupalAuthCookieFilter', SecurityFilterPosition.CAS_FILTER.order + 1);
		}
	}

	def initEmailConfirmationService() {
		emailConfirmationService.onConfirmation = { email, uid, confirmationToken ->
			log.info("User with id $uid has confirmed their email address $email")
            def userToken = UserToken.findByToken(uid);
			if(userToken) {
				userToken.params.tokenId = userToken.id.toString();
				userToken.params.confirmationToken = confirmationToken;
				def userGroupController = new UserGroupController();
				def userGroup = null
                if(userToken.params.userGroupInstanceId){
                    userGroup = userGroupController.findInstance(Long.parseLong(userToken.params.userGroupInstanceId), null, false);
                }
                def url
                if(userToken.controller == "userGroup" || userToken.controller == "userGroupGeneric"){
                    url = userGroupService.userGroupBasedLink(mapping: 'userGroupGeneric', controller:userToken.controller, action:userToken.action, userGroup:userGroup, params:userToken.params)
                }else{
                    url = userGroupService.userGroupBasedLink(controller:userToken.controller, action:userToken.action, userGroup:userGroup, params:userToken.params)

                }
                return [url: url]
			} else {
				//TODO
			}
		  }
		  emailConfirmationService.onInvalid = { uid ->
//        	return [url: userGroupService.userGroupBasedLink('controller':'userGroup', action:'members', userGroup:userGroup, params:userToken.params)]

			log.warn("User with id $uid failed to confirm email address after 30 days")
		  }
		  emailConfirmationService.onTimeout = { email, uid ->
			 log.warn("User with id $uid failed to confirm email address after 30 days")
		  }
	}
	
    def initJSONMarshallers() {
        JSON.registerObjectMarshaller(Geometry) {
            String geomStr = "error"
            WKTWriter wkt = new WKTWriter();
            try {
                geomStr = wkt.write(it);
            } catch(Exception e) {
                log.error "Error writing polygon wkt : ${it}"
            }
            return geomStr;
        }

        JSON.registerObjectMarshaller(SpeciesGroup) {
            return ['id':it.id, 'name': it.name, 'groupOrder':it.groupOrder]
        }

        JSON.registerObjectMarshaller(Habitat) {
            return ['id':it.id, 'name': it.name, 'habitatOrder':it.habitatOrder]
        }

        JSON.registerObjectMarshaller(License) {
            return ['name': it.name.value(), 'url':it.name]
        }

        JSON.registerObjectMarshaller(Featured) {
            if(it.userGroup) 
                return ['createdOn':it.createdOn, 'notes': it.notes, 'userGroupId':it.userGroup.id, 'userGroupName':it.userGroup.name, 'userGroupUrl':userGroupService.userGroupBasedLink(['mapping':'userGroup', 'controller':'userGroup', 'action':'show', 'userGroup':it.userGroup])]
            else
                return ['createdOn':it.createdOn, 'notes': it.notes]
        }

        JSON.registerObjectMarshaller(Synonyms) {
            def syn =  ['id':it.id, 'name':it.name,  'canonicalForm': it.canonicalForm, 'italicisedForm':it.italicisedForm, 'taxonConcept':['id':it.taxonConcept.id], 'isContributor':it.isContributor()]
            if(it.relationship) {
                syn['relationship'] = ['name':it.relationship.value()] 
            }
            return syn;
        }

        JSON.registerObjectMarshaller(CommonNames) {
            def commonname = ['id':it.id, 'name':it.name, 'taxonConcept':['id':it.taxonConcept.id], 'isContributor':it.isContributor() ];
            if(it.language) {
                commonname ['language'] =  ['id':it.language.id, 'name':it.language.name]
            }
            return commonname;
        }
        
        JSON.registerObjectMarshaller(TaxonomyDefinition) {
            return ['id':it.id, 'name':it.name, 'canonicalForm': it.canonicalForm, 'italicisedForm':it.italicisedForm, 'rank':TaxonomyRank.list()[it.rank].value()]
        }

        JSON.registerObjectMarshaller(Classification) {
            return ['id':it.classification.id, name : it.classification.name]
        }

        JSON.registerObjectMarshaller(TaxonomyRegistry) {
            return ['id':it.id, 'classification': ['id':it.classification.id, name : it.classification.name + it.contributors], 'parentTaxon':it.parentTaxon, 'taxonConcept':it.taxonDefinition]
        }
        
        JSON.registerObjectMarshaller(SUser) {
            return ['id':it.id, 'name':it.name, 'email': it.email, 'icon':it.profilePicture()]
        }
 
        JSON.registerObjectMarshaller(Recommendation) {
            return ['name':it.name, 'taxonomyDefinition' : it.taxonConcept];
        }

        JSON.registerObjectMarshaller(UserGroup) {
            return ['id':it.id, 'name':it.name, 'description' : it.description, 'domainName':it.domainName, 'webaddress':it.webaddress, 'foundedOn':it.foundedOn, 'icon':it.icon, ];
        }

    }

	/**
	 * 
	 */
	def destroy = {
		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		//namesIndexerService.store(indexStoreDir);
	}
	
}
