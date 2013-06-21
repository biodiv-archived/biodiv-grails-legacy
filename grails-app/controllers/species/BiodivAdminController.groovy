package species

import java.util.Date;
import java.lang.Float;
import species.NamesParser;
import species.Synonyms;

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

import species.auth.SUser;

import grails.plugins.springsecurity.Secured;

@Secured(['ROLE_ADMIN'])
class BiodivAdminController {
	
	def setupService;
	def speciesService;
	def speciesUploadService;
	def taxonService;
	def speciesSearchService;
	def observationsSearchService;
	def namesLoaderService;
	def namesIndexerService;
	def groupHandlerService;
	def sessionFactory;
	def externalLinksService;

	/**
	 * 
	 */
	def index = {
	}

	/**
	 * 
	 */
	def setup = {
		try {
			setupService.setupDefs();
			flash.message = "Successfully loaded all definitions"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def loadData = {
		int noOfInsertions = 0;
		try {
			noOfInsertions = speciesUploadService.loadData();
			flash.message = "Added ${noOfInsertions} records"
		} catch(e) {
			e.printStackTrace();
			flash.message = "Inserted ${noOfInsertions} records. Error while doing so ${e.getMessage()}"
		}
		redirect(action: "index")
	}

	def loadNames = {
		try {
			taxonService.loadTaxon(true);
			flash.message = "Finished loading names"
		} catch(e) {
			e.printStackTrace();
			flash.message = "Error ${e.getMessage()}"
		}
		
		redirect (action:"index");
	}

	def reloadNames = {
		try {
			log.debug "Syncing names into recommendations"
			namesLoaderService.syncNamesAndRecos(false);
			flash.message = "Successfully loaded all names into recommendations"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}

		redirect(action: "index")
	}

	def reloadSpeciesSearchIndex = {
		try {
			speciesSearchService.publishSearchIndex();
			flash.message = "Successfully created species search index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def reloadObservationsSearchIndex = {
		try {
			observationsSearchService.publishSearchIndex();
			flash.message = "Successfully created observations search index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def reloadNamesIndex = {
		try {
			namesIndexerService.rebuild();
			flash.message = "Successfully created names index"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def updateGroups = {
		int noOfUpdations = 0;
		try {
			noOfUpdations = groupHandlerService.updateGroups();
			flash.message = "Successfully updated group associations for taxonConcepts ${noOfUpdations}"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}
	
	def updateExternalLinks = {
		try {
			int noOfUpdations = externalLinksService.updateExternalLinks();
			flash.message = "Successfully updated externalLinks for taxonConcepts ${noOfUpdations}"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}

	def recomputeInfoRichness = {
		try {
			speciesService.computeInfoRichness();
			flash.message = "Successfully updated species information richness"
		} catch(e) {
			e.printStackTrace();
			flash.message = e.getMessage()
		}
		redirect(action: "index")
	}
	
	
	def loadUsers() {
		def defaultRoleNames = ['ROLE_USER']
		
		new File("/tmp/users.tsv").splitEachLine("\\t") {
			def fields = it;
			def user = new SUser (
					username : fields[1],
					name : fields[1],
					password : fields[2],
					enabled : true,
					accountExpired : false,
					accountLocked : false,
					passwordExpired : false,
					email : fields[3],
					dateCreated : new Date(Long.parseLong(fields[9])),
					lastLoginDate : new Date(Long.parseLong(fields[11])),
					profilePic:fields[15]);
				
				if(fields[13]) {
					user.timezone = Float.parseFloat(fields[13])
				}
		
			SUser.withTransaction {
				if(!user.save(flush: true) ){
					user.errors.each { println it; }
				} else {
		
					def securityConf = SpringSecurityUtils.securityConfig
					Class<?> PersonRole = grailsApplication.getDomainClass(securityConf.userLookup.authorityJoinClassName).clazz
					Class<?> Authority = grailsApplication.getDomainClass(securityConf.authority.className).clazz
					PersonRole.withTransaction { status ->
						defaultRoleNames.each { String roleName ->
							String findByField = securityConf.authority.nameField[0].toUpperCase() + securityConf.authority.nameField.substring(1)
							def auth = Authority."findBy${findByField}"(roleName)
							if (auth) {
								PersonRole.create(user, auth)
							} else {
								println "Can't find authority for name '$roleName'"
							}
						}
					}
				}
			}
		
		}
	}
	
	
	def parseSynonyms = {
		NamesParser namesParser = new NamesParser();
		Synonyms.withTransaction {
				Synonyms.list().eachWithIndex { syn, index ->
						def parsedNames = namesParser.parse([syn.name]);
						if(parsedNames[0]?.canonicalForm) {
								syn.canonicalForm = parsedNames[0].canonicalForm;
								syn.normalizedForm = parsedNames[0].normalizedForm;;
								syn.italicisedForm = parsedNames[0].italicisedForm;;
								syn.binomialForm = parsedNames[0].binomialForm;;
								if(!syn.save(flush:true, insert:true)) {
										syn.errors.each {println it}
								}
						}
				};
		}
		namesLoaderService.syncNamesAndRecos(true);
	}
	
	def user = {
		String actionId = params.id ?: "list"
		log.debug actionId
		render (template:"/admin/user/$actionId");
	}
}
