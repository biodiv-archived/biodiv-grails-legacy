package speciespage

import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.grails.taggable.TagLink;
import species.Classification;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import groovy.io.FileType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

import species.Resource;
import species.Habitat;
import species.Language;
import species.License;
import species.License.LicenseType;
import species.TaxonomyDefinition;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.participation.Featured;
import species.groups.SpeciesGroup;
import species.participation.ActivityFeed;
import species.participation.Comment;
import species.participation.Follow;
import species.participation.Observation;
import species.participation.Checklists;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import species.participation.Flag.FlagType
import species.participation.RecommendationVote.ConfidenceType;
import species.participation.Annotation
import species.sourcehandler.XMLConverter;
import species.utils.ImageType;
import species.utils.Utils;
import species.groups.UserGroupMemberRole;
import species.groups.UserGroupMemberRole.UserGroupMemberRoleType;
import java.beans.Introspector;
import species.CommonNames;
import species.Language;
import species.Species;
import species.Metadata
import species.SpeciesPermission;
import species.dataset.Dataset;


//import org.apache.lucene.document.DateField;
import org.apache.lucene.document.DateTools;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import java.net.URLDecoder;
import org.apache.solr.common.util.DateUtil;
import grails.plugin.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.util.WebUtils;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import content.eml.Coverage;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import species.groups.UserGroupController;
import species.groups.UserGroup;
import species.AbstractMetadataService;
import species.participation.UsersResource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder as LCH;
import static org.springframework.http.HttpStatus.*;
import species.ScientificName.TaxonomyRank;

import species.NamesMetadata.NameStatus;
import grails.plugin.cache.Cacheable;

import species.trait.Fact;
import species.trait.Trait;
import species.trait.TraitValue;
class ObservationService extends AbstractMetadataService {

    static transactional = false

    def recommendationService;
    def observationsSearchService;
    //def curationService;
    def userGroupService;
    def activityFeedService;
    def SUserService;
    def speciesService;
    def messageSource;
    def resourcesService;
    def request;
    def speciesPermissionService;
	def customFieldService;
    def factService;
    /**
     * 
     * @param params
     * @return
     */
    Observation create(params) {
        return super.create(Observation.class, params);
    }

    /**
     * 
     * @param params
     * @param observation
     */
    Observation updateObservation(params, observation, boolean updateResources = true){
        return update(observation, params, Observation.class, true, updateResources);
    }

    Observation update(observation, params, klass = null, boolean update=true, boolean updateResources = true){
        log.debug "Updating obv with params ${params}"
        observation = super.update(observation, params, Observation.class);
        observation.notes = params.notes;

        observation.sourceId = params.sourceId ?: observation.sourceId
        observation.checklistAnnotations = params.checklistAnnotations?:observation.checklistAnnotations
        if(params.url) {
            observation.url = params.url;
        }

        //XXX: in all normal case updateResources flag will be true, but when updating some checklist and checklist
		// has some global update like habitat, group in that case updating its observation info but not the resource info
		if(updateResources){
            updateResource(observation, params);
    	}
        return observation;
    }

    void updateResource(instance, params) {
        log.debug "Updating existing resources"
        def resourcesXML = createResourcesXML(params);
        def rootDirLocatorInstance = instance;
        if(params.action == "bulkSave"){
            rootDirLocatorInstance = springSecurityService.currentUser
        }
        def resources = saveResources(rootDirLocatorInstance, resourcesXML);
        log.debug "Clearing existing resources and adding afresh ${resources}"
        instance.resource?.clear();
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        resources.each { resource ->
            if(!resource.context){
                resource.saveResourceContext(instance)
            }
            instance.addToResource(resource);
        }
    }

    Map saveObservation(params, sendMail=true, boolean updateResources = true) {
        //TODO:edit also calls here...handle that wrt other domain objects
        params.author = springSecurityService.currentUser;
        def observationInstance, feedType, feedAuthor, mailType; 
        try {

            if(params.action == "save" || params.action == "bulkSave") {
                observationInstance = create(params);
                if(params.action == 'bulkSave'){
                    observationInstance.protocol = Observation.ProtocolType.MULTI_OBSERVATION
					observationInstance.habitat = observationInstance.habitat?:Habitat.findByName(Habitat.HabitatType.ALL.value())
                }else if(params.action == 'save') {
                    observationInstance.protocol = params.protocol? Observation.ProtocolType.getEnum(params.protocol) : Observation.ProtocolType.SINGLE_OBSERVATION
                    if(!observationInstance.protocol) {
                        observationInstance.protocol = Observation.SINGLE_OBSERVATION;
                    }
                }
                feedType = activityFeedService.OBSERVATION_CREATED
                feedAuthor = observationInstance.author
                mailType = utilsService.OBSERVATION_ADDED
            } else {
                observationInstance = Observation.get(params.id.toLong())
                params.author = observationInstance.author;
                updateObservation(params, observationInstance, updateResources)
                feedType = activityFeedService.OBSERVATION_UPDATED
                feedAuthor = springSecurityService.currentUser
                if(params.action == "update") {
                    mailType = activityFeedService.OBSERVATION_UPDATED
                }
            }
            def result = super.save(observationInstance, params, sendMail, feedAuthor, feedType, null);

            if(result.success) {
                log.debug "Successfully created observation : "+observationInstance
                params.obvId = observationInstance.id
                /*activityFeedService.addActivityFeed(observationInstance, null, feedAuthor, feedType);

                saveObservationAssociation(params, observationInstance, sendMail)

                if(sendMail)
                    utilsService.sendNotificationMail(mailType, observationInstance, null, params.webaddress);
                */
                
                log.debug "Saving ratings for the resources"
                observationInstance?.resource?.each { res ->
                    if(res.rating) {
                        res.rate(springSecurityService.currentUser, res.rating);
                    }
                }

                params["createNew"] = true
                params["oldAction"] = params.action
                log.debug "============PARAMS ACTION=============== " + params.action
                log.debug "============FOR OBSERVATION ============ " + observationInstance
                String uuidRand =  UUID.randomUUID().toString()
                log.debug "==================UUID GENERATED======== " + uuidRand
                observationInstance.resource.each { resource ->
                    log.debug "=============FOR RESOURCE=========== " + resource + " =======ITS CONTEXT ======== " + resource.context?.value() + " =====ITS FILE NAME===== " +  resource.fileName
                    if((resource.context?.value() == Resource.ResourceContext.USER.toString())){
                        log.debug "========CONTEXT IS USER========= " 
						if( resource.type != Resource.ResourceType.VIDEO){
	                        def usersResFolder = resource.fileName.tokenize('/')[0]
	                        log.debug "======USERS RES FOLDER========== " + usersResFolder
	                        def obvDir = new File(grailsApplication.config.speciesPortal.observations.rootDir);
	                        log.debug "======OBV DIR FROM CONFIG======= " + obvDir
	                        if(!obvDir.exists()) {
	                            obvDir.mkdir();
	                        }
	                        /////UUID FIRST TYM HI FOR A OBV,NEXT TYM SE USE SAME UUID ---DONE
	                        obvDir = new File(obvDir, uuidRand);
	                        log.debug "=====NEW OBV DIR CREATED======== " + obvDir
	                        obvDir.mkdir();                
	                        /////change filename of resource to this uuid and inside that check for clash of filename
	                        File newUniq = utilsService.getUniqueFile(obvDir, Utils.generateSafeFileName(resource.fileName.tokenize('/')[-1]));
	                        def a = newUniq.getAbsolutePath().tokenize('/')[-1]
	                        def newFileName = a.tokenize('.')[0]
	                        log.debug "=====NEW UNIQUE FILE NAME IN THIS NEW OBVDIR======== " + newFileName
	                        //ITERATING OVER RESOURCES FOLDER IN USERSRES AND COPYING IN NEW NAME
	                        String userRootDir = grailsApplication.config.speciesPortal.usersResource.rootDir
	                        def usersResDir = new File(userRootDir, usersResFolder)
	                        def finalSuffix = ""
	                        log.debug "=========ITERATING IN THIS USER RES FOLDER ========= " + usersResDir
	                        usersResDir.eachFileRecurse (FileType.FILES) { file ->
	                            log.debug "=========PICKED UP THIS FILE==================== " + file
	                            def fName = file.getName();
	                            def tokens = fName.tokenize("_");
	                            def nameSuffix = ""
	                            if(tokens.size() == 1){
	                                nameSuffix = "."+fName.tokenize(".")[-1] 
	                                finalSuffix = nameSuffix
	                            }
	                            else {
	                                tokens.each{ t->
	                                    if(!t.isNumber()){
	                                        nameSuffix = nameSuffix + "_" + t
	                                    }
	                                }
	                            }
	                            log.debug "========NAME SUFFIX======== " + nameSuffix
	                            Path source = Paths.get(file.getAbsolutePath());
	                            Path destination = Paths.get(grailsApplication.config.speciesPortal.observations.rootDir +"/"+ uuidRand +"/"+ newFileName + nameSuffix );
	                            log.debug "=======SOURCE============= " + source 
	                            log.debug "====DESTINATION=========== " + destination
	                            try {
	                                //Files moved but empty folder there
	                                log.debug "===================MOVING FILE================================"
	                                Files.move(source, destination);
	                            } catch (IOException e) {
	                                log.debug "======EXCEPTION IN MOVING FILE==============="
	                                e.printStackTrace();
	                            }
	                        }
	                        try{
	                            log.debug "=========DELETING DIRECTORY=========="
	                            FileUtils.deleteDirectory(usersResDir);
	
	                        }catch(IOException e){
	                            log.debug "========ERROR IN DELETION=========="
	                            e.printStackTrace();
	                        }                        
	                        //// UPDATING FILE NAME OF RES IN DB
	                        ////check format of filename---- slash kaise hai
	                        log.debug "=======UPDATING RESOURCE FILE NAME WITH======== : " + "/"+ uuidRand +"/"+ newFileName + finalSuffix
	                        resource.fileName = "/"+ uuidRand +"/"+ newFileName + finalSuffix
						}		
						
                        log.debug "=======UPDATING RESOURCE CONTEXT======"
                        resource.saveResourceContext(observationInstance)

                        def usersRes = UsersResource.findByRes(resource)
                        ////////////////////
                        ////  CHECK STATUS SET CORRECT----DOES CHECKLIST CALL COME HERE???
                        log.debug "============UPDATING STATUS OF THIS USER RESOURCE========== " + usersRes
						if(usersRes){
                            usersRes.status = UsersResource.UsersResourceStatus.USED_IN_OBV
                            if(!usersRes.save(flush:true)){
                                usersRes.errors.allErrors.each { log.error it }
                                return false
                            }
						}

                    }
                }
				
				customFieldService.updateCustomFields(params, observationInstance.id)
                def traitParams = ['contributor':observationInstance.author.email, 'attribution':observationInstance.author.email, 'license':License.LicenseType.CC_BY.value(), replaceFacts:true];
                traitParams.putAll(getTraits(params.traits));
                factService.updateFacts(traitParams, observationInstance);
                return utilsService.getSuccessModel('', observationInstance, OK.value())
            } else {
                observationInstance.errors.allErrors.each { log.error it }
                def errors = [];
                observationInstance.errors.allErrors .each {
                    def formattedMessage = messageSource.getMessage(it, null);
                    errors << [field: it.field, message: formattedMessage]
                }
                
                return utilsService.getErrorModel('Failed to save observation', observationInstance, OK.value(), errors)
            }
        } catch(e) {
            e.printStackTrace();
            return utilsService.getErrorModel('Failed to save observation', observationInstance, OK.value(), [e.getMessage()])
        }
    }

	
    /**
     * @param params
     * @param observationInstance
     * @return
     * saving Groups, tags and resources
     */
    /*def saveObservationAssociation(params, observationInstance, boolean sendMail = true){
        def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
        observationInstance.setTags(tags);

        if(params.groupsWithSharingNotAllowed) {
            setUserGroups(observationInstance, [params.groupsWithSharingNotAllowed], sendMail);
        } else {
            if(params.userGroupsList) {
                def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();
                setUserGroups(observationInstance, userGroups, sendMail);
            }
        }

        log.debug "Saving ratings for the resources"
        observationInstance?.resource?.each { res ->
            if(res.rating) {
                res.rate(springSecurityService.currentUser, res.rating);
            }
        }
    }*/

    /**
     *
     * @param params
     * @return
     */
    Map getRelatedObservations(params) {
        int max = Math.min(params.limit ? params.limit.toInteger() : 12, 100)
        int offset = params.offset ? params.offset.toInteger() : 0
        def relatedObv = [observations:[],max:max];
        if(params.filterProperty == "speciesName") {
            UserGroup userGroupInstance = getUserGroup(params);
            relatedObv = getRelatedObservationBySpeciesName(params.id?params.id.toLong():params.filterPropertyValue.toLong(), max, offset, userGroupInstance, params.fetchField)
        } else if(params.filterProperty == "speciesGroup"){
            relatedObv = getRelatedObservationBySpeciesGroup(params.filterPropertyValue.toLong(),  max, offset)
        } else if(params.filterProperty == "featureBy") {
            Long ugId = getUserGroup(params)?.id;
            relatedObv = getFeaturedObject(ugId, max, offset, params.controller)
        } else if(params.filterProperty == "user"){
            relatedObv = getRelatedObservationByUser(params.filterPropertyValue.toLong(), max, offset, params.sort, params.webaddress)
        } else if(params.filterProperty == "nearByRelated"){
            relatedObv = getNearbyObservationsRelated(params.id?:params.filterPropertyValue, max, offset)
        } else if(params.filterProperty == "nearBy"){
            double lat = params.lat?params.lat.toDouble():-1;
            double lng = params.long?params.long.toDouble():-1;
            relatedObv = getNearbyObservations(lat, lng, max, offset)
        } else if(params.filterProperty == "taxonConcept") {
            relatedObv = getRelatedObservationByTaxonConcept(params.filterPropertyValue.toLong(), max, offset)
        } else if(params.filterProperty == "latestUpdatedObservations") {
            relatedObv = getLatestUpdatedObservation(params.webaddress,params.sort, max, offset)
        } else if(params.filterProperty == "latestUpdatedSpecies") {
            //relatedObv = speciesService.getLatestUpdatedSpecies(params.webaddress,params.sort, max, offset)
        } 
        else if(params.filterProperty == 'bulkUploadResources') {
            relatedObv = resourcesService.getBulkUploadResourcesOfUser(SUser.read(params.filterPropertyValue.toLong()), max, offset)
        }
        else if(params.filterProperty == "contributedSpecies") {
           relatedObv = speciesService.getuserContributionList(params.filterPropertyValue.toInteger(),max,offset)
           
        } 
        
        else{
            if(params.id) {
                relatedObv = getRelatedObservation(params.filterProperty, params.id.toLong(), max, offset)
            } else {
                log.debug "no id"
            }
        }

        if((params.contextGroupWebaddress || params.webaddress) && (params.filterProperty != 'bulkUploadResources')&&(params.filterProperty!='contributedSpecies') ){
            //def group = UserGroup.findByWebaddress(params.contextGroupWebaddress)
            //println group.webaddress
            relatedObv.observations.each { map ->
                boolean inGroup = map.observation.userGroups.find { it.webaddress == params.contextGroupWebaddress || it.webaddress == params.webaddress} != null
                map.inGroup = inGroup;
                //map.observation.inGroup = inGroup;
            }
        }
        return [relatedObv:relatedObv, max:max]
    }



    Map getRelatedObservation(String property, long obvId, int limit, long offset){
        if(!property){
            return [observations:[], count:0]
        }

        def propertyValue = Observation.read(obvId)[property]
        def query = "from Observation as obv where obv." + property + " = :propertyValue and obv.id != :parentObvId and obv.isDeleted = :isDeleted order by obv.createdOn desc"
        def obvs = Observation.findAll(query, [propertyValue:propertyValue, parentObvId:obvId, max:limit, offset:offset, isDeleted:false])
        def result = [];
        obvs.each {
            result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
        }
        return [observations:result, count:obvs.size()]
    }


    /**
     * 
     * @param params
     * @return
     */
    Map getRelatedObservationBySpeciesName(long obvId, int limit, int offset, UserGroup userGroupInstance = null, String fetchFields='*'){
        return getRelatedObservationBySpeciesNames(obvId, limit, offset, userGroupInstance, fetchFields);
    }
    /**
     * 
     * @param params
     * @return
     */
    Map getRelatedObservationByUser(long userId, int limit, long offset, String sort, String webaddress = null){
        def sql =  Sql.newInstance(dataSource);
        def userGroupInstance = null
        if(webaddress){
            userGroupInstance = userGroupService.get(webaddress) 
        }
        //getting count
        def count
        if(userGroupInstance) {
            count = sql.rows("select count(*) from observation obv , user_group_observations ugo where obv.author_id = :userId and ugo.observation_id = obv.id and ugo.user_group_id =:ugId and obv.is_deleted = :isDeleted ", [isDeleted:false, userId:userId, isDeleted: false, ugId:userGroupInstance.id]);
        } else {
            count = sql.rows("select count(*) from observation obv where obv.author_id = :userId and obv.is_deleted = :isDeleted ", [isDeleted:false, userId:userId, isDeleted: false]);
                 
        }
        
        def queryParams = [isDeleted:false]
        def countQuery = "select count(*) from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted  "
        queryParams["userId"] = userId
        queryParams["isDeleted"] = false;
        //queryParams["isShowable"] = true;
        //def count = Observation.executeQuery(countQuery, queryParams)
        
        //getting observations
        def query = "from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted "
        def orderByClause = "order by obv." + (sort ? sort : "createdOn") +  " desc"
        query += orderByClause

        queryParams["max"] = limit
        queryParams["offset"] = offset
        def observationsRows
        if(userGroupInstance) {
            observationsRows = sql.rows("select obv.id from observation obv , user_group_observations ugo where obv.author_id = :userId and ugo.observation_id = obv.id and ugo.user_group_id =:ugId and obv.is_deleted = :isDeleted  " + "order by obv." + (sort ? sort : "created_on") +  " desc limit :max offset :offset", [isDeleted:false, userId:userId, isDeleted: false, ugId:userGroupInstance.id , max:limit, offse:offset]);
        } else {
            observationsRows = sql.rows("select obv.id from observation obv where obv.author_id = :userId and obv.is_deleted = :isDeleted  " + "order by obv." + (sort ? sort : "created_on") +  " desc limit :max offset :offset", [isDeleted:false, userId:userId, isDeleted: false, max:limit, offset:offset]);
                 
        }
        //def observations = Observation.findAll(query, queryParams);
        def result = [];
        def observations = []
        observationsRows.each {
            observations.add(Observation.read(it.getProperty("id")))
        }
        observations.each {
            result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
        }
        return ["observations":result, "count":count[0]["count"]]
    }

    /**
     * 
     * @param params
     * @return
     */
    List getRelatedObservationBySpeciesGroup(long groupId, int limit, long offset){
        log.debug(groupId)

        def query = ""
        def obvs = null
        //if filter group is all
        def groupIds = getSpeciesGroupIds(groupId)
        if(!groupIds) {
            query = "from Observation as obv where obv.isDeleted = :isDeleted order by obv.createdOn desc "
            obvs = Observation.findAll(query, [max:limit, offset:offset, isDeleted:false])
        }else if(groupIds instanceof List){
            //if group is others
            query = "from Observation as obv where obv.isDeleted = :isDeleted and obv.group is null or obv.group.id in (:groupIds) order by obv.createdOn desc"
            obvs = Observation.findAll(query, [groupIds:groupIds, max:limit, offset:offset, isDeleted:false])
        }else{
            query = "from Observation as obv where obv.isDeleted = :isDeleted and obv.group.id = :groupId order by obv.createdOn desc"
            obvs = Observation.findAll(query, [groupId:groupIds, max:limit, offset:offset, isDeleted:false])
        }
        log.debug(" == obv size " + obvs.size())

        def result = [];
        obvs.each {
            result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
        }

        return result
    }

    /**
     * 	
     * @param obvId
     * @return
     */
    String getSpeciesNames(obvId){
        return Observation.read(obvId).fetchSpeciesCall();
    }

    /**
     * 
     * @param speciesName
     * @param params
     * @return
     */
    Map getRelatedObservationBySpeciesNames(long obvId, int limit, int offset, UserGroup userGroupInstance = null, String fetchFields='*'){
        Observation parentObv = Observation.read(obvId);
        if(!parentObv.maxVotedReco) {
            return ["observations":[], "count":0];
        }
        return getRelatedObservationByReco(obvId, parentObv.maxVotedReco, limit, offset, userGroupInstance, fetchFields)
    }

    private Map getRelatedObservationByReco(long obvId, Recommendation maxVotedReco, int limit=3, int offset=0 , UserGroup userGroupInstance = null, String fetchFields='*') {
        def result = [];
        def count=0;
        String ff = ''
        if(fetchFields) {
            fetchFields.split(',').each {
                ff += "o."+it + ' as '+ it+', ';
            }
            ff='new map('+ff+' '+"'"+'observation'+"'"+' as controller)';
        } else {
            ff = 'o';
        }

        String query = "select "+ff+" from Observation o "+(userGroupInstance?" join o.userGroups u":"")+" where o.isDeleted = :isDeleted and o.id != :obvId "+(maxVotedReco.taxonConcept?"and o.maxVotedReco.taxonConcept.id=:maxVotedRecoTaxonConcept":"and o.maxVotedReco.id=:maxVotedReco")+(userGroupInstance?" and u.id=:userGroupId":"")+" order by o.isShowable desc, o.lastRevised desc";
        def params = ['isDeleted':false, 'obvId':obvId]
        if(maxVotedReco.taxonConcept) params['maxVotedRecoTaxonConcept'] = maxVotedReco.taxonConcept.id;
        else params['maxVotedReco'] = maxVotedReco.id;
        if(userGroupInstance) params['userGroupId'] = userGroupInstance.id;
        
        log.debug "getRelatedObservationByReco Sql : ${query} with params ${params}"

	    def observations = Observation.executeQuery(query, params, [max:limit, offset:offset]);
        /*
        def observations = Observation.withCriteria () {
//            projections {
//                groupProperty('sourceId')
//                groupProperty('isShowable')
//				groupProperty('lastRevised')
//            }
            and {
                if(maxVotedReco.taxonConcept) {
                    maxVotedReco {
                        taxonConcept {
                            eq("id", maxVotedReco.taxonConcept.id)
                        }
                    }
                } else {
                    eq("maxVotedReco", maxVotedReco)
                }
                eq("isDeleted", false)
                if(obvId) ne("id", obvId)
                if(userGroupInstance){
                    userGroups{
                        eq('id', userGroupInstance.id)
                    }
                }
            }
            order("isShowable", "desc")
            order("lastRevised", "desc")
            if(limit >= 0) maxResults(limit)
                firstResult (offset?:0)
        }
        */

        observations.each {
            def obv;
            if(userGroupInstance && !(it instanceof Observation)) obv = it[0];
            else obv = it;
            if(obv)
                result.add(['observation':obv, 'title':(obv.isChecklist)? obv.title : maxVotedReco.name]);
        }

        if(limit < 0)
            return ["observations":result];

        if(!fetchFields) {
            count = Observation.createCriteria().count {
                //            projections {
                //                count(groupProperty('sourceId'))
                //            }
                and {
                    eq("maxVotedReco", maxVotedReco)
                    eq("isDeleted", false)
                    eq("isChecklist", false)
                    if(obvId) ne("id", obvId)
                        if(userGroupInstance){
                            userGroups{
                                eq('id', userGroupInstance.id)
                            }
                        }    
                }
            }
        } else {
            count = -1;
        }
        return ["observations":result, "count":count]
    }
    
    Map getRelatedObvForSpecies(resInstance, int limit, int offset, boolean includeExternalUrls = true) {
        def taxon = resInstance.taxonConcept
        //List<Recommendation> scientificNameRecos = recommendationService.searchRecoByTaxonConcept(taxonConcept);
        def resList = []
        def obvLinkList = []
        if(taxon) { 
            def classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);

            String query = "select r.id, obv.id from Observation obv  join obv.maxVotedReco.taxonConcept.hierarchies as reg join obv.resource r where obv.isDeleted = :isDeleted  and reg.classification = :classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!') order by obv.lastRevised desc";
            if(!includeExternalUrls) {
                query = "select r.id, obv.id from Observation obv  join obv.maxVotedReco.taxonConcept.hierarchies as reg join obv.resource r where obv.isDeleted = :isDeleted  and reg.classification = :classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!') and NOT (r.url != null and length(r.fileName) = 1) order by obv.lastRevised desc"
            }
            def resIdList = Observation.executeQuery (query, ['classification':classification, 'isDeleted': false, max : limit.toInteger(), offset: offset.toInteger()]);
             /*
            def query = "select res.id from Observation obv, Resource res where obv.resource.id = res.id and obv.maxVotedReco in (:scientificNameRecos) and obv.isDeleted = :isDeleted order by res.id asc"
            def hqlQuery = sessionFactory.currentSession.createQuery(query) 
            hqlQuery.setMaxResults(limit);
            hqlQuery.setFirstResult(offset);
            def queryParams = [:]
            queryParams["scientificNameRecos"] = scientificNameRecos
            queryParams["isDeleted"] = false
            hqlQuery.setProperties(queryParams);
            def resIdList = hqlQuery.list();
            */
            resIdList.each{
                resList.add(Resource.get(it.getAt(0)));
                obvLinkList.add(it.getAt(1));
            }
            def resListSize = resList.size()
            return ['resList': resList, 'obvLinkList': obvLinkList, 'count' : resListSize ]
        } else {
            return ['resList': resList, 'obvLinkList': obvLinkList, 'count': 0]
        }
    }

    Map getRelatedObservationByTaxonConcept(long taxonConceptId, int limit, long offset){
        def taxon = TaxonomyDefinition.read(taxonConceptId);
        if(!taxon) return ['observations':[], 'count':0]

        //List<Recommendation> scientificNameRecos = recommendationService.searchRecoByTaxonConcept(taxonConcept);
        //if(scientificNameRecos) {
        if(taxon) {
            def classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);
            String query = "select obv.id from Observation obv  join obv.maxVotedReco.taxonConcept.hierarchies as reg where obv.isDeleted = :isDeleted  and reg.classification = :classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!') order by obv.lastRevised desc";
            def resIdList = Observation.executeQuery (query, ['classification':classification, 'isDeleted': false, max : limit.toInteger(), offset: offset.toInteger()]);


            /*def criteria = Observation.createCriteria();
            def observations = criteria.list (max: limit, offset: offset) {
                and {
                    'in'("maxVotedReco", scientificNameRecos)
                    eq("isDeleted", false)
                    //eq("isShowable", true)
                }
                order("lastRevised", "desc")
            }*/
            String countQuery = "select count(*) from Observation obv  join obv.maxVotedReco.taxonConcept.hierarchies as reg where obv.isDeleted = :isDeleted  and reg.classification = :classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!') ";
            def countRes = Observation.executeQuery (countQuery, ['classification':classification, 'isDeleted': false]);


            def count = countRes[0]//observations.totalCount;
            def result = [];
            def iter = resIdList.iterator();
            while(iter.hasNext()){
                def obv = Observation.read(iter.next());
                result.add(['observation':obv, 'title':obv.fetchSpeciesCall()]);
            }
            return ['observations':result, 'count':count]
        } else {
            return ['observations':[], 'count':0]
        }
    }

    Map getLatestUpdatedObservation(String webaddress,String sortBy, int max, int offset ){
        def p = [:]
        p.webaddress = webaddress
        p.sort = sortBy
        def result = getFilteredObservations(p, max, offset).observationInstanceList
        def res = []
        result.each{
            res.add(["observation":it, 'title':(it.isChecklist)? it.title : (it.maxVotedReco?it.maxVotedReco.name:"Unknown")])
        }
        return ['observations':res]
    }

    Map getRecommendation(params){
        return getRecommendations( params.recoId, params.recoName, params.commonName, params.languageName)
    }

    /**
    * recoName
    * canName
    * commonName
    * languageName
    * 
    **/
    Map getRecommendations(Long recoId, String recoName, String commonName, String languageName) {
		Recommendation commonNameReco, scientificNameReco;
		Long languageId = Language.getLanguage(languageName).id;
		//auto select from ui
		if(recoId){
			Recommendation r = Recommendation.get(recoId)
			if(r.isScientificName){
				scientificNameReco = r
				if(commonName)
					commonNameReco = recommendationService.findReco(commonName, false, languageId, scientificNameReco.taxonConcept, true, false);
			}else{
                if(commonName){
                    if(commonName.toLowerCase() == r.lowercaseName)
                        commonNameReco = r
                    else
                        commonNameReco = recommendationService.findReco(commonName, false, languageId, r.taxonConcept, true, false);
                }else{
                    commonNameReco = null
                }

                if(r.taxonConcept)
				    scientificNameReco = recommendationService.findReco(r.taxonConcept.canonicalForm, true, null, r.taxonConcept, true, false)//;Recommendation.findByTaxonConceptAndAcceptedName(r.taxonConcept, r.acceptedName)
			}
			return [mainReco : (scientificNameReco ?:commonNameReco), commonNameReco:commonNameReco];
		}
			
		//reco id is not given
	    if(commonName) {
            utilsService.benchmark('findReco.commonName') {
                commonNameReco = recommendationService.findReco(commonName, false, languageId, null, true, false);
            }
        }
        utilsService.benchmark('findReco.sciName') {
            scientificNameReco = recommendationService.findReco(recoName, true, null, null, true, false);
        }

        return [mainReco : (scientificNameReco ?:commonNameReco), commonNameReco:commonNameReco];
    }


    /**
     * 
     */
    

    /**
     * 
     * @param confidenceType
     * @return
     */
    ConfidenceType getConfidenceType(String confidenceType) {
        if(!confidenceType) return null;
        for(ConfidenceType type : ConfidenceType) {
            if(type.name().equals(confidenceType)) {
                return type;
            }
        }
        return null;
    }
    /**
     * 
     * @param flagType
     * @return
     */
    FlagType getObservationFlagType(String flagType){
        if(!flagType) return null;
        for(FlagType type : FlagType) {
            if(type.name().equals(flagType)) {
                return type;
            }
        }
        return null;
    }

   

    Map findAllTagsSortedByObservationCount(int max){

        LinkedHashMap tags = [:]
        def sql =  Sql.newInstance(dataSource);
        try {
            //query with observation delete handle
            String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where tl.tag_ref = obv.id and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + max ;

            //String query = "select t.name as name from tag_links as tl, tags as t where t.id = tl.tag_id group by t.name order by count(t.name) desc limit " + max ;
            sql.rows(query).each{
                tags[it.getProperty("name")] = it.getProperty("obv_count");
            };
        } catch(e) {
            e.printStackTrace();
        } finally {
            sql.close();
        }
        sql.close();
        return tags;
    }

    def getNoOfTags() {
        def count = TagLink.executeQuery("select count(*) from TagLink group by tag_id");
        return count.size()
    }

    Map getNearbyObservationsRelated(String observationId, int limit, int offset){
        int maxRadius = 1.79662235//=200km considering 111.32 km per 1 degree;
        int maxObvs = 50;
        def sql =  Sql.newInstance(dataSource);
        long totalResultCount = 0;
        def nearbyObservations = []

        try {
            Observation observation = Observation.read(Long.parseLong(observationId));
            String centroid = "ST_GeomFromText('POINT(${observation.longitude} ${observation.latitude})',${ConfigurationHolder.getConfig().speciesPortal.maps.SRID})"
//           def rows = sql.rows("select count(*) as count from observation as g1 where ST_DWithin(ST_Centroid(g1.topology), ${centroid}, :maxRadius) and g1.is_deleted = false", [observationId: Long.parseLong(observationId), maxRadius:maxRadius]);
//            totalResultCount = Math.min(rows[0].getProperty("count")-1, maxObvs);
            limit = Math.min(limit, maxObvs - offset);
            def resultSet = sql.rows("select g1.id,  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology),${centroid})/1000) as distance from observation as g1 where ST_DWithin(ST_Centroid(g1.topology), ${centroid}, :maxRadius) and g1.is_deleted = false order by g1.topology <-> ${centroid} limit :max offset :offset", [observationId: Long.parseLong(observationId), maxRadius:maxRadius, max:limit, offset:offset])


/*            def rows = sql.rows("select count(*) as count from observation as g1, observation as g2 where ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g1.id = :observationId and g1.id <> g2.id", [observationId: Long.parseLong(observationId), maxRadius:maxRadius]);
            totalResultCount = Math.min(rows[0].getProperty("count"), maxObvs);
            limit = Math.min(limit, maxObvs - offset);
            def resultSet = sql.rows("select g2.id,  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) as distance from observation as g1, observation as g2 where  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g1.id = :observationId and g1.id <> g2.id order by ST_Distance_Sphere(g1.topology, g2.topology), g2.last_revised desc limit :max offset :offset", [observationId: Long.parseLong(observationId), maxRadius:maxRadius, max:limit, offset:offset])
*/
            for (row in resultSet){
                nearbyObservations.add(["observation":Observation.findById(row.getProperty("id")), "title":"Found "+row.getProperty("distance")+" km away"])
            }
        } catch (e) {
            e.printStackTrace();
        } finally {
            sql.close();
        }
        return ["observations":nearbyObservations];//, "count":totalResultCount]
    }

    Map getNearbyObservations(double latitude, double longitude, int limit, int offset) {
        if(!latitude || ! longitude) return [count:0];
        int maxRadius = 200;
        int maxObvs = 50;
        def nearbyObservations = []
        long totalResultCount = 0;
        def sql =  Sql.newInstance(dataSource);
        try {
            String point = "ST_GeomFromText('POINT(${longitude} ${latitude})',${ConfigurationHolder.getConfig().speciesPortal.maps.SRID})"
            def rows = sql.rows("select count(*) as count from observation as g2 where ST_DWithin(${point}, ST_Centroid(g2.topology),"+maxRadius/111.32+") and g2.is_deleted = false", [maxRadius:maxRadius]);
            totalResultCount = Math.min(rows[0].getProperty("count"), maxObvs);
            limit = Math.min(limit, maxObvs - offset);
            def resultSet = sql.rows("select g2.id,  ROUND(ST_Distance_Sphere(${point}, ST_Centroid(g2.topology))/1000) as distance from observation as g2 where  ST_DWithin(${point}, ST_Centroid(g2.topology),"+maxRadius/111.32+") and g2.is_deleted = false order by ${point} <-> g2.topology, g2.last_revised desc limit :max offset :offset", [maxRadius:maxRadius, max:limit, offset:offset])

            for (row in resultSet){
                nearbyObservations.add(["observation":Observation.findById(row.getProperty("id")), "title":"Found "+row.getProperty("distance")+" km away"])
            }
        } catch (e) {
            e.printStackTrace();
        } finally {
            sql.close();
        }
        return ["observations":nearbyObservations, "count":totalResultCount]
    }

    Map getAllTagsOfUser(userId){
        def sql =  Sql.newInstance(dataSource);
        LinkedHashMap tags = [:]
        try {
            String query = "select t.name as name,  count(t.name) as obv_count from tag_links as tl, tags as t, observation as obv where obv.author_id = " + userId + " and tl.tag_ref = obv.id and t.id = tl.tag_id and obv.is_deleted = false group by t.name order by count(t.name) desc, t.name asc ";

            sql.rows(query).each{
                tags[it.getProperty("name")] = it.getProperty("obv_count");
            };
        } catch(e) {
            e.printStackTrace();
        } finally {
            sql.close();
        }
        return tags;

    }

    Map getAllRelatedObvTags(params){
        //XXX should handle in generic way
        params.limit = 100
        params.offset = 0
        def obvIds = getRelatedObservations(params).relatedObv.observations.observation.collect{it.id}
        obvIds.add(params.id.toLong())
        return getTagsFromObservation(obvIds)
    }

    Map getRelatedTagsFromObservation(obv){
        int tagsLimit = 30;
        //println "HACK to load obv at ObvService: ${obv.author}";
        def tagNames = obv.tags

        LinkedHashMap tags = [:]
        if(tagNames.isEmpty()){
            return tags
        }
        
        Sql sql =  Sql.newInstance(dataSource);
        try {
            String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where t.name in " +  getSqlInCluase(tagNames) + " and tl.tag_ref = obv.id and tl.type = '"+GrailsNameUtils.getPropertyName(obv.class).toLowerCase()+"' and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

            sql.rows(query, tagNames).each{
                tags[it.getProperty("name")] = it.getProperty("obv_count");
            };
        } catch(e) {
            e.printStackTrace();
        } finally {
            sql.close();
        }
        return tags;
    }

    protected Map getTagsFromObservation(obvIds){
        int tagsLimit = 30;
        LinkedHashMap tags = [:]
        if(!obvIds){
            return tags
        }

        def sql =  Sql.newInstance(dataSource);
        try {
        String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where tl.tag_ref in " + getSqlInCluase(obvIds)  + " and tl.tag_ref = obv.id and tl.type = '"+GrailsNameUtils.getPropertyName(Observation.class).toLowerCase()+"' and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

        sql.rows(query, obvIds).each{
            tags[it.getProperty("name")] = it.getProperty("obv_count");
        };
        } catch(e) {
            e.printStackTrace();
        } finally {
            sql.close();
        }
        return tags;
    }

    Map getFilteredTags(params){
        //TODO:FIXIT ... doesnt return all tags as the last param true is not handled
        return getTagsFromObservation(getFilteredObservations(params, -1, -1, true).observationInstanceList.collect{it[0]});
    }

    private String getSqlInCluase(list){
        return "(" + list.collect {'?'}.join(", ") + ")"
    }

    /**
     * Filter observations by group, habitat, tag, user, species
     * max: limit results to max: if max = -1 return all results
     * offset: offset results: if offset = -1 its not passed to the 
     * executing query
     Spatial Query format 
    //create your query 
    Query q = session.createQuery(< your hql > ); 
    Geometry geom = < your parameter value> 
    //now first create a custom type 
    Type geometryType = new CustomType(new GeometryUserType()); 
    q.setParameter(:geoExp0, geom, geometryType); 
     */
    Map getFilteredObservations(def params, max, offset, isMapView = false, List eagerFetchProperties=null) {
        def queryParts = getFilteredObservationsFilterQuery(params, eagerFetchProperties) 
        String query = queryParts.query;
        long checklistCount = 0, allObservationCount = 0, speciesCount = 0, subSpeciesCount = 0;

        def boundGeometry = queryParts.queryParams.remove('boundGeometry'); 
        /*        if(isMapView) {//To get id, topology of all markers
                  query = queryParts.mapViewQuery + queryParts.filterQuery;// + queryParts.orderByClause
                  } else {*/
        query += queryParts.filterQuery + queryParts.orderByClause
        //		}

        log.debug "query : "+query;
        log.debug "checklistCountQuery : "+queryParts.checklistCountQuery;
        log.debug "allObservationCountQuery : "+queryParts.allObservationCountQuery;
        //log.debug "distinctRecoQuery : "+queryParts.distinctRecoQuery;
        //log.debug "speciesGroupCountQuery : "+queryParts.speciesGroupCountQuery;
        //log.debug "speciesCountQuery : "+queryParts.speciesCountQuery;
        //log.debug "speciesStatusCountQuery : "+queryParts.speciesStatusCountQuery;

        log.debug query;
        log.debug queryParts.queryParams;
        def checklistCountQuery = (queryParts.checklistCountQuery)?sessionFactory.currentSession.createSQLQuery(queryParts.checklistCountQuery):null
        def allObservationCountQuery = sessionFactory.currentSession.createSQLQuery(queryParts.allObservationCountQuery)
        //def distinctRecoQuery = sessionFactory.currentSession.createQuery(queryParts.distinctRecoQuery)
        //def speciesGroupCountQuery = sessionFactory.currentSession.createQuery(queryParts.speciesGroupCountQuery)
        //def speciesCountQuery = sessionFactory.currentSession.createQuery(queryParts.speciesCountQuery)
        //def speciesStatusCountQuery = sessionFactory.currentSession.createQuery(queryParts.speciesStatusCountQuery)

        def hqlQuery = sessionFactory.currentSession.createSQLQuery(query)
        if(params.bounds && boundGeometry) {
            hqlQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
            if(checklistCountQuery)
                checklistCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
                allObservationCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
                //speciesCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
                //distinctRecoQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(org.hibernatespatial.GeometryUserType))
                //speciesGroupCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(org.hibernatespatial.GeometryUserType))
        } 

        if(max > -1){
            hqlQuery.setMaxResults(max);
            //distinctRecoQuery.setMaxResults(10);
            queryParts.queryParams["max"] = max
        }

        if(offset > -1) {
            hqlQuery.setFirstResult(offset);
            //distinctRecoQuery.setFirstResult(0);
            queryParts.queryParams["offset"] = offset
        }

        hqlQuery.setProperties(queryParts.queryParams);
        hqlQuery.setReadOnly(true);
        //hqlQuery.setCacheable(true)
        //hqlQuery.setCacheRegion('obvList');
        /*def qTranslator = (new org.hibernate.hql.ast.ASTQueryTranslatorFactory()).createQueryTranslator('123', query, ['limit':3,'max':2], sessionFactory);
        qTranslator.compile(queryParts.queryParams, false);
        println "******************************"
        println qTranslator;
        println qTranslator.getQueryString();
        println qTranslator.getSQLString();
        println "******************************"
         */
        def observationInstanceList, observationInstanceListIds;
        def distinctRecoList = [];
        def speciesGroupCountList = [];
        if(params.identified=="true") {
            observationInstanceList=getObservationInstanceList(params)
            allObservationCount = observationInstanceList.size()
        }
        else {
            observationInstanceList = hqlQuery.addEntity('obv', Observation).list();
            for(int i=0;i < observationInstanceList.size(); i++) {
                println observationInstanceList[i].isChecklist
                if(observationInstanceList[i].isChecklist) {
                    //observationInstanceList[i] = Checklists.read(observationInstanceList[i].id);
                }
            }

            if(checklistCountQuery){
                checklistCountQuery.setProperties(queryParts.queryParams);
                checklistCount = checklistCountQuery.list()[0];
            }

            allObservationCountQuery.setProperties(queryParts.queryParams)
            //speciesCountQuery.setProperties(queryParts.queryParams)
            //speciesStatusCountQuery.setProperties(queryParts.queryParams)
            allObservationCount = allObservationCountQuery.list()[0]
            /*def speciesCounts = speciesCountQuery.list()
            speciesCount = speciesCounts[0]
            subSpeciesCount = speciesCounts[1]

            def speciesStatusCounts = speciesStatusCountQuery.list()
            def acceptedSpeciesCount = speciesStatusCounts[0]
            def synonymSpeciesCount = speciesStatusCounts[1]
             */
        }

        if(!params.loadMore?.toBoolean()) {
            /*distinctRecoQuery.setProperties(queryParts.queryParams)
            def distinctRecoListResult = distinctRecoQuery.list()
            distinctRecoListResult.each {it->
            def reco = Recommendation.read(it[0]);
            distinctRecoList << [reco.name, reco.isScientificName, it[1]]
            } 
             */
            //speciesGroupCountQuery.setProperties(queryParts.queryParams)
            //speciesGroupCountList = getFormattedResult(speciesGroupCountQuery.list())
        }

        if(params.daterangepicker_start){
            queryParts.queryParams["daterangepicker_start"] = params.daterangepicker_start
        }

        if(params.daterangepicker_end){
            queryParts.queryParams["daterangepicker_end"] =  params.daterangepicker_end
        }

        if(params.observedon_start){
            queryParts.queryParams["observedon_start"] = params.observedon_start
        }

        if(params.observedon_end){
            queryParts.queryParams["observedon_end"] =  params.observedon_end
        }

        return [observationInstanceList:observationInstanceList, allObservationCount:allObservationCount, checklistCount:checklistCount, speciesGroupCountList:speciesGroupCountList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
    }

    /**
     *
     **/
    def getFilteredObservationsFilterQuery(params, List eagerFetchProperties = null) {
        params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        params.habitat = params.habitat.toLong()
		params.isMediaFilter = (params.isMediaFilter) ?: 'false'
        //params.userName = springSecurityService.currentUser.username;

        def queryParams = [:];
        def activeFilters = [:]

        def query = """select 
        CASE
                   WHEN chk.id IS NOT NULL THEN 1
                              WHEN obv.id IS NOT NULL THEN 0
                                     END AS clazz_,
                          """;
        def userGroupQuery = " ", tagQuery = '', featureQuery = '', nearByRelatedObvQuery = '', taxonQuery = '', traitQuery = '', recoQuery='';
        def filterQuery = " where obv.is_deleted = false "
 
        if(!params.sort || params.sort == 'score' || params.sort.toLowerCase() == 'lastrevised') {
            params.sort = "lastRevised"
        }
        def m = sessionFactory.getClassMetadata(Observation);
        def orderByClause = "  obv." +m.getPropertyColumnNames(params.sort)[0] +  " desc, obv.id asc"

        recoQuery += " left outer join recommendation reco on obv.max_voted_reco_id = reco.id "; 
        if(params.fetchField) {
            query += " obv.id as id,"
            params.fetchField.split(",").each { fetchField ->
                if(!fetchField.equalsIgnoreCase('id') && !fetchField.equalsIgnoreCase('title'))
                    query += " obv."+m.getPropertyColumnNames(fetchField)[0]+" as "+fetchField+","
                else if(fetchField.equalsIgnoreCase('title'))
                    query += " chk."+m.getPropertyColumnNames(fetchField)[0]+" as "+fetchField+","

            }
            query = query [0..-2];
            queryParams['fetchField'] = params.fetchField
            //query += " from Observation obv join checklists_observation chk_obv on obv.id = chk_obv.observation_id ";
            query += " from Observation obv left outer join checklists chk on obv.id = chk.id ";
        } else if(params.filterProperty == 'nearByRelated' && !params.bounds) {
            query += " obv.*, chk.* "
            query += " from Observation obv left outer join Checklists chk on chk.id = obv.id  ";
        } 
        else {
            query += " obv.*, chk.* "
            query += " from Observation obv left outer join Checklists chk on chk.id = obv.id inner join suser u on obv.author_id = u.id left outer join resource r on obv.repr_image_id = r.id ";
//            query += recoQuery;
        }
        //def mapViewQuery = "select obv.id, obv.topology, obv.isChecklist from Observation obv "

       
        if(params.featureBy == "true" || params.userGroup || params.webaddress){
            params.userGroup = getUserGroup(params);
        }

        if(params.featureBy == "true" ) {
            if(params.userGroup == null) {
                filterQuery += " and obv.feature_count > 0 "     
                //featureQuery = " join (select f.objectId, f.objectType from Featured f group by f.objectType, f.objectId) as feat"
              //  featureQuery = ", select distinct Featured.objectId from Featured where Featured.objectType = :featType as feat "
            } else {
                featureQuery = " join Featured feat on obv.id = feat.object_id "
            }
            query += featureQuery;
            //filterQuery += " and obv.featureCount > 0 "
            if(params.userGroup == null) {
                //filterQuery += " and feat.userGroup is null "     
            }else {
                filterQuery += " and obv.id = feat.object_id and (feat.object_type =:featType or feat.object_type=:featType1) and feat.user_group_id = :userGroupId "
                queryParams["userGroupId"] = params.userGroup?.id
            }
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Observation.class.getCanonicalName();
            queryParams["featType1"] = Checklists.class.getCanonicalName();
            activeFilters["featureBy"] = params.featureBy
        }
        //TODO: check logic
        if(params.featureBy == "false") {
            featureQuery = ", Featured feat "
            query += featureQuery;
            filterQuery += " and obv.id != feat.object_id and feat.object_type = :featType "
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Observation.class.getCanonicalName();

        }

        if(params.sGroup){
            params.sGroup = params.sGroup.toLong()
            def groupId = getSpeciesGroupIds(params.sGroup)
            if(!groupId){
                log.debug("No groups for id " + params.sGroup)
            }else{
                filterQuery += " and obv.group_id = :groupId "
                queryParams["groupId"] = groupId
                activeFilters["sGroup"] = groupId
            }
        }

        if(params.recom && params.identified!='true'){
            params.recom = params.recom.toLong();
            filterQuery += " and (obv.max_voted_reco_id = :recom) ";
            queryParams["recom"] = params.recom;
            activeFilters["recom"] = params.recom;
        }

        if(params.webaddress) {
            def userGroupInstance =	utilsService.getUserGroup(params)
            params.userGroup = userGroupInstance;
        }

        if(params.userGroup) {
            log.debug "Filtering from usergourp : ${params.userGroup}"
            userGroupQuery = " join user_group_observations  userGroup on userGroup.observation_id = obv.id "
            query += userGroupQuery
            filterQuery += " and userGroup.user_group_id =:userGroupId "
            queryParams['userGroupId'] = params.userGroup.id
            queryParams['userGroup'] = params.userGroup
        } 

        if(params.tag){
            tagQuery = ",  TagLink tagLink, Tags tag "
            query += tagQuery;
            //mapViewQuery = "select obv.topology from Observation obv, TagLink tagLink "
            filterQuery +=  " and obv.id = tagLink.tag_ref and tagLink.type = :tagType and tagLink.tag_id = tag.id and tag.name = :tag "

            queryParams["tag"] = params.tag
            queryParams["tagType"] = GrailsNameUtils.getPropertyName(Observation.class);
            activeFilters["tag"] = params.tag
        }

        if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
            filterQuery += " and obv.habitat_id = :habitat "
            queryParams["habitat"] = params.habitat
            activeFilters["habitat"] = params.habitat
        }

        if(params.user){
            filterQuery += " and obv.author_id = :user "
            queryParams["user"] = params.user.toLong()
            activeFilters["user"] = params.user.toLong()
        }

        if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)){
            filterQuery += " and (obv.is_checklist = false and obv.max_voted_reco_id is null) "
            //queryParams["speciesName"] = params.speciesName
            activeFilters["speciesName"] = params.speciesName
        } 

        if (params.isFlagged && params.isFlagged.toBoolean()){
            filterQuery += " and obv.flag_count > 0 "
            activeFilters["isFlagged"] = params.isFlagged.toBoolean()
        }

        if( params.daterangepicker_start && params.daterangepicker_end){
            def df = new SimpleDateFormat("dd/MM/yyyy")
            def startDate = df.parse(URLDecoder.decode(params.daterangepicker_start))
            def endDate = df.parse(URLDecoder.decode(params.daterangepicker_end))
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(endDate)
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.MINUTE, 59);
            endDate = new Date(cal.getTimeInMillis())

            filterQuery += " and ( created_on between :daterangepicker_start and :daterangepicker_end) "
            queryParams["daterangepicker_start"] =  startDate   
            queryParams["daterangepicker_end"] =  endDate

            activeFilters["daterangepicker_start"] = params.daterangepicker_start
            activeFilters["daterangepicker_end"] =  params.daterangepicker_end
        }

        if(params.observedon_start && params.observedon_end){
            def df = new SimpleDateFormat("dd/MM/yyyy")
            def startDate = df.parse(URLDecoder.decode(params.observedon_start))
            def endDate = df.parse(URLDecoder.decode(params.observedon_end))
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(endDate)
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.MINUTE, 59);
            endDate = new Date(cal.getTimeInMillis())

            filterQuery += " and ( observed_on between :daterangepicker_start and :daterangepicker_end) "
            queryParams["observedon_start"] =  startDate   
            queryParams["observedon_end"] =  endDate

            activeFilters["observedon_start"] = startDate       //params.daterangepicker_start
            activeFilters["observedon_end"] =  endDate          //params.daterangepicker_end
        }

        if(params.bounds) {
            def bounds = params.bounds.split(",")

            def swLat = bounds[0].toFloat()
            def swLon = bounds[1].toFloat()
            def neLat = bounds[2].toFloat()
            def neLon = bounds[3].toFloat()

            //def boundGeometry = getBoundGeometry(swLat, swLon, neLat, neLon)
            //filterQuery += " and within (obv.topology, :boundGeometry) = true " //) ST_Contains( :boundGeomety,  obv.topology) "
            filterQuery += "and obv.latitude > " + swLat + " and  obv.latitude < " + neLat + " and obv.longitude > " + swLon + " and obv.longitude < " + neLon
            //queryParams['boundGeometry'] = boundGeometry
            activeFilters["bounds"] = params.bounds
        }  

        if(params.type == 'nearBy' && params.lat && params.long) {
            String point = "ST_GeomFromText('POINT(${params.long.toFloat()} ${params.lat.toFloat()})',${ConfigurationHolder.getConfig().speciesPortal.maps.SRID})"
            int maxRadius = params.maxRadius?params.int('maxRadius'):200
            filterQuery += " and (ST_DWithin(ST_Centroid(obv.topology), ${point}, "+(maxRadius/111.32)+")) = TRUE ";
            queryParams['maxRadius'] = maxRadius;

            activeFilters["lat"] = params.lat
            activeFilters["long"] = params.long
            activeFilters["maxRadius"] = maxRadius
            
            orderByClause = " ST_Distance(ST_Centroid(obv.topology), ${point})" 
        } 
        
        if(params.filterProperty == 'speciesName' && params.parentId) {
            //Check because ajax calls sending these parameters
            if(params.parentId && params.parentId != '') {
                try { 
                    params.parentId = Integer.parseInt(params.parentId.toString()).toLong(); 
                } catch(NumberFormatException e) { 
                    params.parentId = null 
                }
            }
            Observation parentObv = Observation.read(params.parentId);
            def parMaxVotedReco = parentObv.maxVotedReco;
            if(parMaxVotedReco) {
                if(parMaxVotedReco.taxonConcept) {
                    recoQuery = recoQuery+" left outer join taxonomy_definition t on reco.taxon_concept_id = t.id ";
                    filterQuery += " and obv.max_voted_reco_id = :parMaxVotedReco" //removed check for not equal to parentId to include it in show page 
                    queryParams['parMaxVotedRecoTaxonConcept'] = parMaxVotedReco.taxonConcept
                } else {
                    filterQuery += " and (obv.max_voted_reco_id = :parMaxVotedReco)" //removed check for not equal to parentId to include it in show page 
                }
                queryParams['parMaxVotedReco'] = parMaxVotedReco

                queryParams['parentId'] = params.parentId;
                
                activeFilters["filterProperty"] = params.filterProperty
                activeFilters["parentId"] = params.parentId

            }
        }

        if(params.filterProperty == 'nearByRelated' && !params.bounds && params.parentId) {
            params.maxNearByRadius = 200
            //Check because ajax calls sending these parameters
            if(params.parentId && params.parentId != '') {
                try { 
                    params.parentId = Long.parseLong(params.parentId.toString()); 
                } catch(NumberFormatException e) { 
                    params.parentId = null 
                }
            }
            //nearByRelatedObvQuery = ', Observation as g2';
            //query += nearByRelatedObvQuery;
            Observation observation = Observation.read(params.parentId);
            String centroid = "ST_GeomFromText('POINT(${observation.longitude} ${observation.latitude})',${ConfigurationHolder.getConfig().speciesPortal.maps.SRID})"
            filterQuery += " and ST_DWithin(ST_Centroid(obv.topology), ${centroid}, "+(params.maxNearByRadius/111.32)+") = true and obv.is_deleted = false "

//            filterQuery += " and ST_DWithin(ST_Centroid(obv.topology), ${centroid}, :maxNearByRadius/111.32) and obv.isDeleted = false "                                              //removed check for not equal to parentId to include it in show page
            queryParams['parentId'] = params.parentId
            queryParams['maxNearByRadius'] = params.maxNearByRadius?params.int('maxNearByRadius'):200;
            
            activeFilters["filterProperty"] = params.filterProperty
            activeFilters["parentId"] = params.parentId
            activeFilters["maxNearByRadius"] = params.maxNearByRadius?params.int('maxNearByRadius'):200;

            //"select g2.id,  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) as distance from observation as g1, observation as g2 where  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g2.is_showable = true and g1.id = :observationId and g1.id <> g2.id order by ST_Distance(g1.topology, g2.topology), g2.last_revised desc limit :max offset :offset"
        
        }
        
        if(params.filterProperty == 'taxonConcept') {
            def taxon = TaxonomyDefinition.read(params.filterPropertyValue.toLong());
            if(taxon) {
                List<Recommendation> scientificNameRecos = recommendationService.searchRecoByTaxonConcept(taxon);
                if(scientificNameRecos) {
                    filterQuery += " and obv.max_voted_reco_id in (:scientificNameRecos)";
                    queryParams['scientificNameRecos'] = scientificNameRecos;

                    activeFilters["filterProperty"] = params.filterProperty;
                    activeFilters["parentId"] = params.parentId;
                    activeFilters["filterPropertyValue"] = params.filterPropertyValue;
                }
                /*queryParams['taxon'] = taxon.id
                activeFilters['taxon'] = taxon.id
                taxonQuery = " join obv.maxVotedReco.taxonConcept.hierarchies as reg "
                query += taxonQuery;

                def classification;
                if(params.classification)
                    classification = Classification.read(Long.parseLong(params.classification))
                if(!classification)
                    classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);

                queryParams['classification'] = classification.id 
                activeFilters['classification'] = classification.id
 
                filterQuery += " and reg.classification.id = :classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!')";
                
                activeFilters["filterProperty"] = params.filterProperty
                activeFilters["parentId"] = params.parentId
                activeFilters["filterPropertyValue"] = params.filterPropertyValue;
                */

            }
        }
		
        if(params.taxon) {
            def taxon = TaxonomyDefinition.read(params.taxon.toLong());
            if(taxon){
                queryParams['taxon'] = taxon.id
                activeFilters['taxon'] = taxon.id
                taxonQuery = recoQuery + " join taxonomy_definition t on reco.taxon_concept_id = t.id join taxonomy_registry reg on reg.taxon_definition_id = t.id "
                query += taxonQuery;

                //taxonQuery = " join recommendation reco on obv.max_voted_reco_id = reco.id  "+taxonQuery;

                def classification;
                if(params.classification)
                    classification = Classification.read(Long.parseLong(params.classification))
                if(!classification)
                    classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);

                queryParams['classification'] = classification.id 
                activeFilters['classification'] = classification.id
 
                filterQuery += " and reg.classification_id = :classification and (reg.path like '%!_"+taxon.id+"!_%'  escape '!' or reg.path like '"+taxon.id+"!_%'  escape '!' or reg.path like '%!_"+taxon.id+"' escape '!')";

            }
        }
		
        if(params.dataset) {
            if(params.dataset == 'false') {
                filterQuery += " and obv.dataset_id is null ";
                queryParams['dataset'] = false
                activeFilters['dataset'] = false
            } else {
                def dataset = Dataset.read(params.dataset.toLong());
                if(dataset) {
                    queryParams['dataset'] = dataset.id
                    activeFilters['dataset'] = dataset.id

                    filterQuery += " and obv.dataset_id = :dataset ";
                }
            }
        }
		
		if(params.isMediaFilter.toBoolean()){
			filterQuery += " and obv.is_showable = true ";
		}
		
		if(params.areaFilter && (!params.areaFilter.trim().equalsIgnoreCase('all'))){
			filterQuery += " and obv.location_scale = :locationScale "
			activeFilters["areaFilter"] = params.areaFilter.toLowerCase()
			queryParams['locationScale'] = Metadata.LocationScale.getEnum(params.areaFilter)
		}

        if(params.trait){
            traitQuery = getTraitQuery(params.trait);
            if(!taxonQuery) {
                taxonQuery = recoQuery+" left outer join taxonomy_definition t on reco.taxon_concept_id = t.id ";
                query += taxonQuery;
           }
            filterQuery += traitQuery['filterQuery'];
            orderQuery += traitQuery['orderQuery'];            
            def classification;
            if(params.classification)
                classification = Classification.read(Long.parseLong(params.classification));
            if(!classification)
                classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);

            queryParams['classification'] = classification.id;
            activeFilters['classification'] = classification.id
 
            //filterQuery += " and reg.classification_id = :classification";
            queryParams['trait'] = params.trait;

        }
				
        String checklistObvCond = ""
        if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()) {
            checklistObvCond = " and obv.id != obv.source_id "
        }

        def distinctRecoQuery = "select obv.max_voted_reco_id, count(*) from Observation obv  "+ userGroupQuery +" "+ taxonQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+((params.filterProperty == 'nearByRelated')?nearByRelatedObvQuery:'')+filterQuery+checklistObvCond+ " and obv.max_voted_reco_id is not null group by obv.max_voted_reco_id order by count(*) desc,obv.max_voted_reco_id asc";
        def distinctRecoCountQuery = "select count(distinct obv.max_voted_reco_id)   from Observation obv  "+ userGroupQuery +" "+ taxonQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+((params.filterProperty == 'nearByRelated')?nearByRelatedObvQuery:'')+filterQuery+ checklistObvCond + " and obv.max_voted_reco_id is not null ";
        def speciesGroupCountQuery = "select sg.name, count(*),(case when obv.max_voted_reco_id is not null  then 1 else 2 end) from Observation obv join species_group sg on obv.group_id = sg.id  "+ userGroupQuery +" "+ taxonQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+((params.filterProperty == 'nearByRelated')?nearByRelatedObvQuery:'')+filterQuery+ " and obv.is_checklist=false " + checklistObvCond + "group by sg.name,(case when obv.max_voted_reco_id is not null  then 1 else 2 end) order by sg.name desc";
		def checklistCountQuery =  null
        if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()) {
            filterQuery += " and obv.is_checklist = true "
			checklistCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+ taxonQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+((params.filterProperty == 'nearByRelated')?nearByRelatedObvQuery:'')+filterQuery
			checklistCountQuery += " and obv.is_checklist = true "
            activeFilters["isChecklistOnly"] = params.isChecklistOnly.toBoolean()
        } else {
			filterQuery += " and obv.is_checklist = false "
			activeFilters["isChecklistOnly"] = false
		}


        
		def allObservationCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+ taxonQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+((params.filterProperty == 'nearByRelated')?nearByRelatedObvQuery:'')+filterQuery
		//def speciesCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+ taxonQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+((params.filterProperty == 'nearByRelated')?nearByRelatedObvQuery:'')+filterQuery+" group by obv.maxVotedReco.taxonConcept.rank having obv.maxVotedReco.taxonConcept.rank in :ranks"
        //queryParams['ranks'] = [TaxonomyRank.SPECIES.ordinal(), TaxonomyRank.INFRA_SPECIFIC_TAXA.ordinal()]

		//def speciesStatusCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+ taxonQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+((params.filterProperty == 'nearByRelated')?nearByRelatedObvQuery:'')+filterQuery+" group by obv.maxVotedReco.taxonConcept.status having obv.maxVotedReco.taxonConcept.status in :statuses"
        //queryParams['statuses'] = [NameStatus.ACCEPTED, NameStatus.SYNONYM]

        orderByClause = " order by " + orderByClause;

        return [query:query, allObservationCountQuery:allObservationCountQuery, checklistCountQuery:checklistCountQuery, distinctRecoQuery:distinctRecoQuery, distinctRecoCountQuery:distinctRecoCountQuery, speciesGroupCountQuery:speciesGroupCountQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

    }

    /**
     *
     **/


    private static getBoundGeometry(x1, y1, x2, y2){
        def p1 = new Coordinate(y1, x1)
        def p2 = new Coordinate(y1, x2)
        def p3 = new Coordinate(y2, x2)
        def p4 = new Coordinate(y2, x1)
        Coordinate[] arr = new Coordinate[5]
        arr[0] = p1
        arr[1] = p2
        arr[2] = p3
        arr[3] = p4
        arr[4] = p1
        def gf = new GeometryFactory(new PrecisionModel(), ConfigurationHolder.getConfig().speciesPortal.maps.SRID)
        def lr = gf.createLinearRing(arr)
        def pl = gf.createPolygon(lr, null)
        return pl
    }

    /**
    * getUserGroupObservations
    */
    def getUserGroupObservations(UserGroup userGroupInstance, params, max, offset, isMapView=false) {
		if(!userGroupInstance) return;
        params['userGroup'] = userGroupInstance;
        return getFilteredObservations(params, max, offset, isMapView); 
	}

    /**
     * Gets users observations depending on user group
     **/
    long getAllObservationsOfUser(SUser user , UserGroup userGroupInstance = null) {
        return (long) Observation.createCriteria().count {
            and {
                eq("author", user)
                eq("isDeleted", false)
                //eq("isShowable", true)
            }
            if(userGroupInstance){
                userGroups{
                    eq('id', userGroupInstance.id)
                }
            }
        }
        //return (long)Observation.countByAuthorAndIsDeleted(user, false);
    }

    /**
     * Gets recommendations of user made in a user group
     **/
    long getAllRecommendationsOfUser(SUser user , UserGroup userGroupInstance = null) {
        //TODO: filter on usergroup if required
        def sql =  Sql.newInstance(dataSource);
        def result
        if(userGroupInstance){
            result = sql.rows("select count(recoVote) from recommendation_vote recoVote, observation o, user_group_observations ugo where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and ugo.observation_id = o.id and ugo.user_group_id =:ugId", [userId:user.id, isDeleted:false, ugId:userGroupInstance.id]);
        } else {
            result = sql.rows("select count(recoVote) from recommendation_vote recoVote, observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted ", [userId:user.id, isDeleted:false]);
        }
  //      def result = RecommendationVote.executeQuery("select count(recoVote) from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable", [userId:user.id, isDeleted:false, isShowable:true]);
        return (long)result[0]["count"];
    }

    List getRecommendationsOfUser(SUser user, int max, long offset , UserGroup userGroupInstance = null) {
        def sql =  Sql.newInstance(dataSource);
        if(max == -1) {
            def recommendationVotesList
            if(userGroupInstance){
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o , user_group_observations ugo where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and ugo.observation_id = o.id and ugo.user_group_id =:ugId order by recoVote.voted_on desc", [userId:user.id, isDeleted:false, ugId:userGroupInstance.id]);
            } else {
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted order by recoVote.voted_on desc", [userId:user.id, isDeleted:false]);
            }
            //def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable order by recoVote.votedOn desc", [userId:user.id, isDeleted:false, isShowable:true], [max:max, offset:offset]);
            def finalResult = []
            
            for (row in recommendationVotesList) {
                finalResult.add(RecommendationVote.findById(row.getProperty("id")))
            }
            return finalResult;


            /*
            def recommendationVotesList = sql.rows("select recoVote from recommendation_vote recoVote , observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable order by recoVote.voted_on desc", [userId:user.id, isDeleted:false, isShowable:true])
            //def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable order by recoVote.votedOn desc", [userId:user.id, isDeleted:false, isShowable:true]);
            return recommendationVotesList;
            */
        } else {
            def recommendationVotesList
            if(userGroupInstance){
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o , user_group_observations ugo where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and ugo.observation_id = o.id and ugo.user_group_id =:ugId order by recoVote.voted_on desc limit :max offset :offset", [userId:user.id, isDeleted:false, max:max, offset:offset, ugId:userGroupInstance.id]);
            } else {
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted order by recoVote.voted_on desc limit :max offset :offset", [userId:user.id, isDeleted:false,max:max, offset:offset]);
            }
            //def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable order by recoVote.votedOn desc", [userId:user.id, isDeleted:false, isShowable:true], [max:max, offset:offset]);
            def finalResult = []
            
            for (row in recommendationVotesList) {
                finalResult.add(RecommendationVote.findById(row.getProperty("id")))
            }
            //return recommendationVotesList;
            return finalResult;
        }
    }

    Map getIdentificationEmailInfo(m, requestObj, unsubscribeUrl, controller="", action=""){
        def source = m.source;
        def mailSubject = ""
        def activitySource = ""
        switch (source) {
            case "observationShow":
            mailSubject = messageSource.getMessage("info.share.observation", null, LCH.getLocale())
            activitySource = messageSource.getMessage("observation.label", null, LCH.getLocale())
            break

            case  "observationList" :
            mailSubject = messageSource.getMessage("info.share.list", null, LCH.getLocale())
            activitySource = messageSource.getMessage("info.observation.list", null, LCH.getLocale())
            break

            case "userProfileShow":
            mailSubject = messageSource.getMessage("info.share.profile", null, LCH.getLocale())
            activitySource = messageSource.getMessage("info.user.profile", null, LCH.getLocale())
            break

            case "userGroupList":
            mailSubject = messageSource.getMessage("info.share.lists", null, LCH.getLocale())
            activitySource = messageSource.getMessage("info.user.lists", null, LCH.getLocale())
            break
            case "userGroupInvite":
            mailSubject = messageSource.getMessage("info.invitation.join", null, LCH.getLocale())
            activitySource = messageSource.getMessage("info.user.group", null, LCH.getLocale())
            break
            case controller+action.capitalize():
            mailSubject = messageSource.getMessage("button.share", null, LCH.getLocale())+" "+controller
            activitySource = controller
            break;
            default:
            log.debug "invalid source type ${source}"
        }
        def currentUser = springSecurityService.currentUser?:""
        def currentUserProfileLink = currentUser?utilsService.generateLink("SUser", "show", ["id": currentUser.id], null):'';
        def templateMap = [currentUser:currentUser, activitySource:activitySource, domain:Utils.getDomainName(requestObj)]

        def conf = SpringSecurityUtils.securityConfig
        def messagesourcearg1 = new Object[3];
        messagesourcearg1[0] = currentUser;
        messagesourcearg1[1] = activitySource;
        messagesourcearg1[2] = templateMap["domain"];
        def staticMessage = messageSource.getMessage("grails.plugin.springsecurity.ui.askIdentification.staticMessage", messagesourcearg1, LCH.getLocale())
        if (staticMessage.contains('$')) {
            staticMessage = evaluate(staticMessage, templateMap)
        } 

        templateMap["currentUserProfileLink"] = currentUserProfileLink;
        templateMap["activitySourceUrl"] = m.sourcePageUrl?: ""
        templateMap["unsubscribeUrl"] = unsubscribeUrl ?: ""
        templateMap["userMessage"] = m.userMessage?: ""
        def messagesourcearg = new Object[7];
        messagesourcearg[0] = currentUserProfileLink;
        messagesourcearg[1] = currentUser;
        messagesourcearg[2] = templateMap["domain"];
        messagesourcearg[3] = activitySource != null ? activitySource:'';
        messagesourcearg[4] = templateMap["userMessage"];
        messagesourcearg[5] = templateMap["unsubscribeUrl"];
        messagesourcearg[6] = templateMap["activitySourceUrl"];
             
        def body = messageSource.getMessage("grails.plugin.springsecurity.ui.askIdentification.emailBody", messagesourcearg, LCH.getLocale())

        if (body.contains('$')) {
            body = evaluate(body, templateMap)
        }
        return [mailSubject:mailSubject, mailBody:body, staticMessage:staticMessage, source:source]
    }

    private String evaluate(s, binding) {
        new SimpleTemplateEngine().createTemplate(s).make(binding)
    }

    /**
     *
     * @param params
     * @return
     */
    def getObservationsFromSearch(params) {
        def max = Math.min(params.max ? params.max.toInteger() : 12, 100)
        def offset = params.offset ? params.offset.toLong() : 0

        def model;

        try {
            model = getFilteredObservationsFromSearch(params, max, offset, false);
        } catch(SolrException e) {
            e.printStackTrace();
            //model = [params:params, observationInstanceTotal:0, observationInstanceList:[],  queryParams:[max:0], tags:[]];
        }
        return model;
    }

    /**
     * Filter observations by group, habitat, tag, user, species
     * max: limit results to max: if max = -1 return all results
     * offset: offset results: if offset = -1 its not passed to the
     * executing query
     */
    Map getFilteredObservationsFromSearch(params, max, offset, isMapView){
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        def queryParts = getFilteredObservationsQueryFromSearch(params, max, offset, isMapView);
        def paramsList = queryParts.paramsList
        def queryParams = queryParts.queryParams
        def activeFilters = queryParts.activeFilters

        if(isMapView) {
            //query = mapViewQuery + filterQuery + orderByClause
        } else {
            //query += filterQuery + orderByClause
            queryParams["max"] = max
            queryParams["offset"] = offset
        }

        List<Observation> instanceList = new ArrayList<Observation>();
        def totalObservationIdList = [];
        def facetResults = [:], responseHeader
        long noOfResults = 0, isShowableCount = 0;
        long checklistCount = 0
        List distinctRecoList = [];
        def speciesGroupCountList = [];
        if(paramsList) {
            //Facets
            def speciesGroups;
            paramsList.add('facet', "true");
            paramsList.add('facet.mincount', "1");
            //            paramsList.add(searchFieldsConfig.IS_CHECKLIST, false);

            if(!params.loadMore?.toBoolean()){
                paramsList.add('facet.field', searchFieldsConfig.IS_CHECKLIST);
                params["facet.offset"] = params["facet.offset"] ?: 0;
                paramsList.add('facet.'+searchFieldsConfig.IS_CHECKLIST+'.offset', params["facet.offset"]);

                paramsList.add('facet.field', searchFieldsConfig.IS_SHOWABLE);
            }

            paramsList.add('facet.field', searchFieldsConfig.SOURCE_ID);
            if(offset > -1)
                paramsList.add('f.'+searchFieldsConfig.SOURCE_ID+'.facet.offset', offset);
            if(max > -1)
                paramsList.add('f.'+searchFieldsConfig.SOURCE_ID+'.facet.limit', max);

            //String query = paramsList.get('q')
            //query += " AND "+searchFieldsConfig.IS_SHOWABLE+":true ";
            //paramsList.remove('q');
            //paramsList.add('q', query);

            def queryResponse = observationsSearchService.search(paramsList);

            List sourceIdFacets = queryResponse.getFacetField(searchFieldsConfig.SOURCE_ID).getValues()
            sourceIdFacets.each {
                def instance = Observation.read(Long.parseLong(it.getName()+""));
                if(instance) {
                    totalObservationIdList.add(Long.parseLong(it.getName()+""));
                    instanceList.add(instance);
                }
            }

            if(!params.loadMore?.toBoolean()){
                List isChecklistFacets = queryResponse.getFacetField(searchFieldsConfig.IS_CHECKLIST).getValues()
                isChecklistFacets.each {
                    if(it.getName() == 'true')
                        checklistCount = it.getCount()
                }

                List isShowableFacets = queryResponse.getFacetField(searchFieldsConfig.IS_SHOWABLE).getValues()
                isShowableFacets.each {
                    if(it.getName() == 'true')
                        noOfResults = it.getCount()
                }

            }

            responseHeader = queryResponse?.responseHeader;

        }
        /*if(responseHeader?.params?.q == "*:*") {
          responseHeader.params.remove('q');
          }*/

        return [responseHeader:responseHeader, observationInstanceList:instanceList, resultType:'observation', instanceTotal:noOfResults, checklistCount:checklistCount, observationCount: noOfResults-checklistCount , queryParams:queryParams, activeFilters:activeFilters, totalObservationIdList:totalObservationIdList, distinctRecoList:distinctRecoList, speciesGroupCountList:speciesGroupCountList, canPullResource:userGroupService.getResourcePullPermission(params)]

    }

    private Map getFilteredObservationsQueryFromSearch(params, max, offset, isMapView) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        params.habitat = params.habitat.toLong()
        def queryParams = [isDeleted : false, isShowable:true]

        def activeFilters = [:]


        NamedList paramsList = new NamedList();

        //params.userName = springSecurityService.currentUser.username;
        queryParams["query"] = params.query
        activeFilters["query"] = params.query
        params.query = params.query ?: "";

        String aq = "";
        int i=0;

        if(params.aq instanceof GrailsParameterMap || params.aq instanceof Map) {
            params.aq.each { key, value ->
                queryParams["aq."+key] = value;
                activeFilters["aq."+key] = value;
                if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
                    if(i++ == 0) {
                        aq = key + ': ('+value+')';
                        } else {
                            aq = aq + " AND " + key + ': ('+value+')';
                        }
                }
            }
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String lastRevisedStartDate = '';
        String lastRevisedEndDate = '';
        if(params.daterangepicker_start) {
            Date s = DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']);
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(s)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MINUTE, 0);
            s = new Date(cal.getTimeInMillis())
            //StringWriter str1 = new StringWriter();
            lastRevisedStartDate = dateFormatter.format(s)
            //DateUtil.formatDate(s, cal, str1)
            //println str1
            //lastRevisedStartDate = str1;

        }

        if(params.daterangepicker_end) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            Date e = DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']);
            cal.setTime(e)
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.MINUTE, 59);
            e = new Date(cal.getTimeInMillis())
            //			StringWriter str2 = new StringWriter();
            //			DateUtil.formatDate(e, cal, str2)
            //			println str2
            lastRevisedEndDate = dateFormatter.format(e);
        }

        if(lastRevisedStartDate && lastRevisedEndDate) {
            if(i > 0) aq += " AND";
            aq += " "+searchFieldsConfig.UPLOADED_ON+":["+lastRevisedStartDate+" TO "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;

        } else if(lastRevisedStartDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.UPLOADED_ON+":["+lastRevisedStartDate+" TO NOW]";
            queryParams['daterangepicker_start'] = params.daterangepicker_start;
            activeFilters['daterangepicker_start'] = params.daterangepicker_endparams.daterangepicker_end;
        } else if (lastRevisedEndDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.UPLOADED_ON+":[ * "+lastRevisedEndDate+"]";
            queryParams['daterangepicker_end'] = params.daterangepicker_end;
            activeFilters['daterangepicker_end'] = params.daterangepicker_end;
        }

        String observedOnStartDate = '';
        String observedOnEndDate = '';
        if(params.observedon_start) {
            Date s = DateUtil.parseDate(params.observedon_start, ['dd/MM/yyyy']);
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(s)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MINUTE, 0);
            s = new Date(cal.getTimeInMillis())
            //StringWriter str1 = new StringWriter();
            observedOnStartDate = dateFormatter.format(s)
            //DateUtil.formatDate(s, cal, str1)
            //println str1
            //lastRevisedStartDate = str1;

        }

        if(params.observedon_end) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            Date e = DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']);
            cal.setTime(e)
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.MINUTE, 59);
            e = new Date(cal.getTimeInMillis())
            //			StringWriter str2 = new StringWriter();
            //			DateUtil.formatDate(e, cal, str2)
            //			println str2
            observedOnEndDate = dateFormatter.format(e);
        }

        if(observedOnStartDate && observedOnEndDate) {
            if(i > 0) aq += " AND";
            aq += " "+searchFieldsConfig.OBSERVED_ON+":["+observedOnStartDate+" TO "+observedOnEndDate+"]";
            queryParams['observedon_start'] = params.observedon_start;
            queryParams['observedon_end'] = params.observedon_end;
            activeFilters['observedon_start'] = params.observedon_start;
            activeFilters['observedon_end'] = params.observedon_end;

        } else if(observedOnStartDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.OBSERVED_ON+":["+observedOnStartDate+" TO NOW]";
            queryParams['observedon_start'] = params.observedon_start;
            activeFilters['observedon_start'] = params.observedon_end;
        } else if (observedOnEndDate) {
            if(i > 0) aq += " AND";
            //String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
            aq += " "+searchFieldsConfig.OBSERVED_ON+":[ * "+observedOnEndDate+"]";
            queryParams['observedon_end'] = params.observedon_end;
            activeFilters['observedon_end'] = params.observedon_end;
        }

        if(params.query && aq) {
            params.query = params.query + " AND "+aq
        } else if (aq) {
            params.query = aq;
        }

        paramsList.add('q', Utils.cleanSearchQuery(params.query));
        //options
        if(offset>= 0)
            paramsList.add('start', offset);
        if(max >= 0)
            paramsList.add('rows', max);    
        params['sort'] = params['sort']?:"score"
        String sort = params['sort'].toLowerCase(); 
        if(isValidSortParam(sort)) {
            if(sort.indexOf(' desc') == -1) {
                sort += " desc";
            }
            paramsList.add('sort', sort);
        }

        paramsList.add('fl', params['fl']?:"id");

        //Filters
        if(params.sGroup) {
            params.sGroup = params.sGroup.toLong()
            def groupId = getSpeciesGroupIds(params.sGroup)
            if(!groupId){
                log.debug("No groups for id " + params.sGroup)
            } else{
                paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
                queryParams["groupId"] = groupId
                activeFilters["sGroup"] = groupId
            }
        }

        if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
            paramsList.add('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
            queryParams["habitat"] = params.habitat
            activeFilters["habitat"] = params.habitat
        }

        if(params.tag) {
            paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
            queryParams["tag"] = params.tag
            queryParams["tagType"] = 'observation'
            activeFilters["tag"] = params.tag
        }

        if(params.user){
            paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
            queryParams["user"] = params.user.toLong()
            activeFilters["user"] = params.user.toLong()
        }

        if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)) {
            paramsList.add('fq', searchFieldsConfig.MAX_VOTED_SPECIES_NAME+":"+params.speciesName);
            queryParams["name"] = params.name
            activeFilters["name"] = params.name
        }

        if(params.isFlagged && params.isFlagged.toBoolean()){
            paramsList.add('fq', searchFieldsConfig.ISFLAGGED+":"+params.isFlagged.toBoolean());
            activeFilters["isFlagged"] = params.isFlagged.toBoolean()
        }

        if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()){
            paramsList.add('fq', searchFieldsConfig.IS_CHECKLIST+":"+params.isChecklistOnly.toBoolean());
            activeFilters["isChecklistOnly"] = params.isChecklistOnly.toBoolean()
        }

        if(params.bounds){
            def bounds = params.bounds.split(",")
            def swLat = bounds[0]
            def swLon = bounds[1]
            def neLat = bounds[2]
            def neLon = bounds[3]
            paramsList.add('fq', searchFieldsConfig.LATLONG+":["+swLat+","+swLon+" TO "+neLat+","+neLon+"]");
            activeFilters["bounds"] = params.bounds
        }

        if(params.uGroup) {
            if(params.uGroup == "THIS_GROUP") {
                String uGroup = params.webaddress
                if(uGroup) {
                    paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
                }
                queryParams["uGroup"] = params.uGroup
                activeFilters["uGroup"] = params.uGroup
            } else {
                queryParams["uGroup"] = "ALL"
                activeFilters["uGroup"] = "ALL"
            }
        }

        log.debug "Along with faceting params : "+paramsList;
        return [paramsList:paramsList, queryParams:queryParams, activeFilters:activeFilters];	
    }

    private boolean isValidSortParam(String sortParam) {
        if(sortParam.equalsIgnoreCase("score") || sortParam.equalsIgnoreCase("visitCount")  || sortParam.equalsIgnoreCase("createdOn") || sortParam.equalsIgnoreCase("lastRevised") )
            return true;
        return false;
    }

    def nameTerms(params) {
        List result = new ArrayList();

        def queryResponse = observationsSearchService.terms(params.term, params.field, params.max);
        if(queryResponse) {
            NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
            for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
                Map.Entry tag = (Map.Entry) iterator.next();
                result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Observations"]);
            }
        }
        return result;
    } 

    def delete(params){
        String messageCode;
        String url = utilsService.generateLink(params.controller, 'list', []);
        String label = Utils.getTitleCase(params.controller?:'Observation')
        def messageArgs = [label, params.id]
        def errors = [];
        boolean success = false;
        if(!params.id) {
            messageCode = 'default.not.found.message'
        } else {
            try {
                def observationInstance = Observation.get(params.id.toLong())
                if (observationInstance) {
                    observationInstance.removeResourcesFromSpecies()
                    boolean isFeatureDeleted = Featured.deleteFeatureOnObv(observationInstance, springSecurityService.currentUser, getUserGroup(params))
                    if(isFeatureDeleted && utilsService.ifOwns(observationInstance.author)) {
                        def mailType = observationInstance.instanceOf(Checklists) ? utilsService.CHECKLIST_DELETED : utilsService.OBSERVATION_DELETED
                        try {
                            observationInstance.isDeleted = true;
                            observationInstance.deleteFromChecklist();

                            //Delete underlying observations of checklist
                            if(observationInstance.instanceOf(Checklists)) {
                                observationInstance.deleteAllObservations(); 
                            }
                            if(!observationInstance.hasErrors() && observationInstance.save(flush: true)){
                                utilsService.sendNotificationMail(mailType, observationInstance, null, params.webaddress);
                                observationsSearchService.delete(observationInstance.id);
                                messageCode = 'default.deleted.message'
                                url = utilsService.generateLink(params.controller, 'list', [])
                                ActivityFeed.updateIsDeleted(observationInstance)
                                success = true;
                            } else {
                                messageCode = 'default.not.deleted.message'
                                url = utilsService.generateLink(params.controller, 'show', [id: params.id])
                                observationInstance.errors.allErrors.each { log.error it }
                                observationInstance.errors.allErrors .each {
                                    def formattedMessage = messageSource.getMessage(it, null);
                                    errors << [field: it.field, message: formattedMessage]
                                }

                            }
                        }
                        catch (org.springframework.dao.DataIntegrityViolationException e) {
                            messageCode = 'default.not.deleted.message'
                            url = utilsService.generateLink(params.controller, 'show', [id: params.id])
                            e.printStackTrace();
                            log.error e.getMessage();
                            errors << [message:e.getMessage()];
                        }
                    } else {
                        if(!isFeatureDeleted) {
                            messageCode = 'default.not.deleted.message'
                            log.warn "Couldnot delete feature"
                        }
                        else {
                            messageArgs.add(0,'delete');
                            messageCode = 'default.not.permitted.message'
                            log.warn "${observationInstance.author} doesn't own observation to delete"
                        }
                    }
                } else {
                    messageCode = 'default.not.found.message'
                    url = utilsService.generateLink(params.controller, 'list', [])
                }
            } catch(e) {
                e.printStackTrace();
                url = utilsService.generateLink(params.controller, 'list', [])
                messageCode = 'default.not.deleted.message'
                errors << [message:e.getMessage()];
            }
        }
        
        String message = messageSource.getMessage(messageCode, messageArgs.toArray(), Locale.getDefault())
				
        return [success:success, url:url, msg:message, errors:errors]
    }

    def addAnnotation(params, Observation obv){
        def ann = new Annotation(params)
        obv.addToAnnotations(ann);
    }

    /**
     */
    def locations(params) {
        List result = new ArrayList();

        if(!params.term) return result;

        NamedList paramsList = new NamedList();
        /*        paramsList.add('fl', 'location_exact');
        if(springSecurityService.currentUser){
        paramsList.add('q', "author_id:"+springSecurityService.currentUser.id.toLong())
        } else {
        paramsList.add('q', "*:*");
        }
        paramsList.add('facet', "true");
        paramsList.add('facet.field', "location_exact")
        paramsList.add('facet.mincount', "1")
        paramsList.add('facet.prefix', params.term?:'*');
        paramsList.add('facet.limit', 5);

        def facetResults = []
        if(paramsList) {
        def queryResponse = observationsSearchService.search(paramsList);
        List facets = queryResponse.getFacetField('location_exact').getValues()

        facets.each {
        facetResults.add([value:it.getName(), label:it.getName()+'('+it.getCount()+')',  "category":"My Locations"]);
        }

        }
         */

        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        String query = ""
        if(springSecurityService.currentUser){
            query += searchFieldsConfig.CONTRIBUTOR+":"+springSecurityService.currentUser.name+" AND "
        } else {
            //query += "*:*";
        }
        query += searchFieldsConfig.LOCATION_EXACT+":"+params.term+'*'?:'*';

        paramsList.add("q", query);
        paramsList.add("fl", searchFieldsConfig.LOCATION_EXACT+','+searchFieldsConfig.LATLONG+','+searchFieldsConfig.TOPOLOGY);
        paramsList.add("start", 0);
        paramsList.add("rows", 20);
        //paramsList.add("sort", searchFieldsConfig.UPDATED_ON+' desc,'+searchFieldsConfig.SCORE + " desc ");
        paramsList.add("sort", searchFieldsConfig.UPDATED_ON+' desc');                                                                                
        paramsList.add("sort", searchFieldsConfig.SCORE + " desc ");
        def results = [];
        Map temp = [:]
        if(paramsList) {
            def queryResponse = observationsSearchService.search(paramsList);

            Iterator iter = queryResponse.getResults().listIterator();
            while(iter.hasNext()) {
                def doc = iter.next();
                if(results.size() >= 5) break;
                if(!temp[doc.getFieldValue(searchFieldsConfig.LOCATION_EXACT)]) {
                    results.add(['location':doc.getFieldValue(searchFieldsConfig.LOCATION_EXACT), 'topology':doc.getFieldValue(searchFieldsConfig.TOPOLOGY), 'category':'My Locations']);
                    temp[doc.getFieldValue(searchFieldsConfig.LOCATION_EXACT)] = true;
                }
            }
        }

        return results
    }

    /**
     * To validate topology in all domain class
     */
    public static validateLocation(Geometry gm, obj){
        if(!gm){
            return ['observation.suggest.location']
        }
        Geometry indiaBoundry = getBoundGeometry(6.74678, 68.03215, 35.51769, 97.40238)
        if(!indiaBoundry.covers(gm)){
            return ['location.value.not.in.india', '6.74678', '35.51769', '68.03215', '97.40238']
        }
    }

    /**
     */
    private getFormattedResult(List result){
        def formattedResult = []
        def groupNames = result.collect {it[0]};
        result.groupBy {it[0]} .sort(). each { key, value ->
            if(key == grailsApplication.config.speciesPortal.group.ALL)
                return;
            def r = new Object[3];
            r[0] = key

            value.each { x->
                if(x[2] == 1) {
                    //identified
                    r[1] = x[1];
                } else {
                    //unidentified count
                    r[2] = x[1];
                }
            }

            r[2] = r[2]?:0
            r[1] = (r[1]?:0)+r[2]
            formattedResult.add(r)
        }
        formattedResult.sort { a, b -> b[1] - a[1]}

        return [data:formattedResult, columns:[
        ['string', 'Species Group'],
        ['number', 'All'],
        ['number', 'Unidentified']
            ]]
    }

    /**
     */
    def getDistinctRecoList(params, int max, int offset) {
        def distinctRecoList = [];
        def queryParts = getFilteredObservationsFilterQuery(params) 
        def boundGeometry = queryParts.queryParams.remove('boundGeometry'); 
		log.debug "distinctRecoQuery  : "+queryParts.distinctRecoQuery;
        log.debug "distinctRecoCountQuery  : "+queryParts.distinctRecoCountQuery;
        log.debug "queryparams"+queryParts.queryParams;
        def distinctRecoQuery = sessionFactory.currentSession.createSQLQuery(queryParts.distinctRecoQuery)
        def distinctRecoCountQuery = sessionFactory.currentSession.createSQLQuery(queryParts.distinctRecoCountQuery)

        if(params.bounds && boundGeometry) {
            distinctRecoQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
            distinctRecoCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
        } 

        if(max > -1){
            distinctRecoQuery.setMaxResults(max);
        }
        if(offset > -1) {
            distinctRecoQuery.setFirstResult(offset);
        }

        distinctRecoQuery.setProperties(queryParts.queryParams)
        distinctRecoCountQuery.setProperties(queryParts.queryParams)
        def distinctRecoListResult = distinctRecoQuery.list()
        distinctRecoListResult.each {it->
            def reco = Recommendation.read(it[0]);
            if(params.downloadFrom == 'uniqueSpecies') {
                //HACK: request not available as its from job scheduler
                distinctRecoList << [reco.name, reco.isScientificName, getObservationHardLink(it[0],it[1]), getSpeciesHardLink(reco)]
            }else {
                distinctRecoList << [getSpeciesHyperLinkedName(reco), reco.isScientificName, getObservationHardLink(it[0],it[1]),getObservationHardLink(it[0],it[1],params.user)]
            }
        }
        def count = distinctRecoCountQuery.list()[0]
        return [distinctRecoList:distinctRecoList, totalCount:count];
    }
    /**
     */
    def getDistinctRecoListFromSearch(params, int max, int offset) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        def queryParts = getFilteredObservationsQueryFromSearch(params, max, offset, false);
        def paramsList = queryParts.paramsList
        def queryParams = queryParts.queryParams
        def activeFilters = queryParts.activeFilters

        queryParams["max"] = max
        queryParams["offset"] = offset

        def facetResults = [:], responseHeader
        List distinctRecoList = [];
        if(paramsList) {
            //Facets
            def speciesGroups;
            paramsList.add('facet', "true");
            paramsList.add('facet.offset', offset);
            paramsList.add('facet.mincount', "1");

            if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()) {
                //for checklist only condition ... getting all unique species from checklists
                //removing earlier added condition to filter for only checklists
                for (Iterator iterator = paramsList.iterator(); iterator.hasNext();) {
                    Map.Entry paramEntry = (Map.Entry) iterator.next();
                    if(paramEntry.getValue().equals(searchFieldsConfig.IS_CHECKLIST+":"+params.isChecklistOnly.toBoolean()))
                        paramEntry.setValue(searchFieldsConfig.IS_CHECKLIST+":false");
                }
                paramsList.add('fq', searchFieldsConfig.IS_SHOWABLE+":false");
            } else {
                paramsList.add('fq', searchFieldsConfig.IS_CHECKLIST+":false");
            }

            paramsList.add('facet.field', searchFieldsConfig.MAX_VOTED_SPECIES_NAME+"_exact");
            paramsList.add("f.${searchFieldsConfig.MAX_VOTED_SPECIES_NAME}_exact.facet.limit", max);
            def qR = observationsSearchService.search(paramsList);
            List distinctRecoListFacets = qR.getFacetField(searchFieldsConfig.MAX_VOTED_SPECIES_NAME+"_exact").getValues()
            distinctRecoListFacets.each {
                //TODO second parameter, isScientificName
                def recoWithLink = getSpeciesHyperLinkedName(Recommendation.findByName(it.getName()));
                if(recoWithLink)
                    distinctRecoList.add([recoWithLink, true, it.getCount()]);
            }

        }
        return [distinctRecoList:distinctRecoList] 
    }
	
	private String getSpeciesHyperLinkedName(Recommendation reco){
        if(!reco) return;
		def speciesId = reco.taxonConcept?.findSpeciesId()
		if(!speciesId){
			return reco.name
		}
		
		def link = utilsService.generateLink("species", "show", ["id": speciesId])
		return "" + '<a  href="' +  link +'"><i>' + reco.name + "</i></a>"
	}

    private String getSpeciesHardLink(reco) {
        if(!reco) return ;
        def speciesId = reco.taxonConcept?.findSpeciesId()
        if(!speciesId){
            return ''
        }

        def link = utilsService.createHardLink("species", "show", speciesId)
        return link 
    }
    /**
     */
    def getSpeciesGroupCount(params) {
        def distinctRecoList = [];

        def queryParts = getFilteredObservationsFilterQuery(params) 
        def boundGeometry = queryParts.queryParams.remove('boundGeometry'); 

        log.debug "speciesGroupCountQuery : "+queryParts.speciesGroupCountQuery;

        def speciesGroupCountQuery = sessionFactory.currentSession.createSQLQuery(queryParts.speciesGroupCountQuery)

        if(params.bounds && boundGeometry) {
            speciesGroupCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
        } 
        speciesGroupCountQuery.setProperties(queryParts.queryParams)
        def speciesGroupCountList = getFormattedResult(speciesGroupCountQuery.list())
        return [speciesGroupCountList:speciesGroupCountList];

    } 

    /**
     */ 
    def  getSpeciesGroupCountFromSearch(params) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        def queryParts = getFilteredObservationsQueryFromSearch(params, -1, -1, false);
        def paramsList = queryParts.paramsList

        def facetResults = [:], responseHeader, speciesGroupCountList=[]
        if(paramsList) {
            paramsList.add('facet', "true");
            params["facet.offset"] = params["facet.offset"] ?: 0;
            paramsList.add('facet.offset', params["facet.offset"]);
            paramsList.add('facet.mincount', "1");
            if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()) {
                //for checklist only condition ... getting all unique species from checklists
                //removing earlier added condition to filter for only checklists
                for (Iterator iterator = paramsList.iterator(); iterator.hasNext();) {
                    Map.Entry paramEntry = (Map.Entry) iterator.next();
                    if(paramEntry.getValue().equals(searchFieldsConfig.IS_CHECKLIST+":"+params.isChecklistOnly.toBoolean()))
                        paramEntry.setValue(searchFieldsConfig.IS_CHECKLIST+":false");
                }
                paramsList.add('fq', searchFieldsConfig.IS_SHOWABLE+":false");
            } else { 
                paramsList.add(searchFieldsConfig.IS_CHECKLIST, false);
            }

            paramsList.add('facet.field', searchFieldsConfig.SGROUP);
            //paramsList.add("f.${searchFieldsConfig.SGROUP}.facet.limit", -1);
            def speciesGroups = SpeciesGroup.list();
            speciesGroups.each {
                paramsList.add('facet.query', "${searchFieldsConfig.SGROUP}:${it.id} AND ${searchFieldsConfig.IS_CHECKLIST}:false AND ${searchFieldsConfig.MAX_VOTED_SPECIES_NAME}_exact:Unknown");
                paramsList.add('facet.query', "${searchFieldsConfig.SGROUP}:${it.id} AND ${searchFieldsConfig.IS_CHECKLIST}:false");
            }

            def queryResponse = observationsSearchService.search(paramsList);

            List speciesGroupCountListFacets = queryResponse.getFacetField(searchFieldsConfig.SGROUP).getValues()
            Map speciesGroupUnidentifiedCountListFacets = queryResponse.getFacetQuery()		

            speciesGroups.each {
                if(it.name != grailsApplication.config.speciesPortal.group.ALL) {
                    String key = "${searchFieldsConfig.SGROUP}:${it.id} AND ${searchFieldsConfig.IS_CHECKLIST}:false"
                    String unidentifiedKey = "${searchFieldsConfig.SGROUP}:${it.id} AND ${searchFieldsConfig.IS_CHECKLIST}:false AND ${searchFieldsConfig.MAX_VOTED_SPECIES_NAME}_exact:Unknown";
                    long all = speciesGroupUnidentifiedCountListFacets.get(key);
                    long unIdentified = speciesGroupUnidentifiedCountListFacets.get(unidentifiedKey)
                        if(all | unIdentified)
                            speciesGroupCountList.add([it.name, all, unIdentified]);
                }
            }

            speciesGroupCountList = speciesGroupCountList.sort {a,b->a[0]<=>b[0]}
            speciesGroupCountList = [data:speciesGroupCountList?:[], columns:[
            ['string', 'Species Group'],
            ['number', 'All'],
            ['number', 'Unidentified']
                ]]

        }
        return [speciesGroupCountList:speciesGroupCountList] 
    }

    /**
     */
    def getObservationFeatures(Observation obv) {
        String query = "select t.type as type, t.feature as feature from map_layer_features t where ST_WITHIN('"+obv.topology.toText()+"', t.topology)order by t.type" ;
        log.debug query;
        def features = [:]
        if(!Environment.getCurrent().getName().equalsIgnoreCase("development")) {
        try {
            def sql =  Sql.newInstance(dataSource);
            //sql.in(new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()), obv.topology)

            sql.rows(query).each {
                switch (it.getProperty("type")) {
                    case "140" : features['Rainfall'] = it.getProperty("feature")+" mm";break;
                    case "138" : features['Soil'] = it.getProperty("feature");break;
                    case "161" : features['Temperature'] = it.getProperty("feature")+" C";break;
                    case "139" : features['Forest Type'] = it.getProperty("feature").toLowerCase().capitalize();break;
                    case "136" : features['Tahsil'] = it.getProperty("feature");break;
                }
            };
            sql.close();
        } catch(e) {
            e.printStackTrace();
            log.error e.getMessage();
        }
        }
        return features
    } 

    /**
     * Get map occurences within specified bounds
     */
    def getObservationOccurences(def params) {
        def queryParts = getFilteredObservationsFilterQuery(params) 
        String query = queryParts.query;

        def boundGeometry = queryParts.queryParams.remove('boundGeometry'); 
        query += queryParts.filterQuery + queryParts.orderByClause

        log.debug "occurences query : "+query;
        log.debug queryParts.queryParams;
        
        def hqlQuery = sessionFactory.currentSession.createSQLQuery(query)
        /* if(params.bounds && boundGeometry) {
            hqlQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
        } */
        
        hqlQuery.setMaxResults(1000);
        hqlQuery.setFirstResult(0);

        hqlQuery.setProperties(queryParts.queryParams);
        def observationInstanceList = hqlQuery.list();

        return [observations:observationInstanceList, geoPrivacyAdjust:Utils.getRandomFloat()]
    }

    /*
     * used for download and post in bulk
     */
    def getObservationList(params, max, offset, String action, List eagerFetchProperties=null){
		if(Utils.isSearchAction(params, action)){
            //getting result from solr
            def idList = getFilteredObservationsFromSearch(params, max, offset, false).totalObservationIdList
            def res = []
            idList.each { obvId ->
                res.add(Observation.read(obvId))
            }
            return res
        } else if(params.webaddress){
            def userGroupInstance =	userGroupService.get(params.webaddress)
            if (!userGroupInstance){
                log.error "user group not found for id  $params.id  and webaddress $params.webaddress"
                return []
            }
            return getUserGroupObservations(userGroupInstance, params, max, offset).observationInstanceList;
        }else{
            return getFilteredObservations(params, max, offset, false, eagerFetchProperties).observationInstanceList
        }
    }

    /**
    * Plz use utilsService.getUserGroup
    **/
    @Deprecated
    def getUserGroup(params) {
        return utilsService.getUserGroup(params);
    }

    /**
    * Plz use utilsService.sendNotificationMail
    **/
    @Deprecated
    public sendNotificationMail(String notificationType, def obv, request, String userGroupWebaddress, ActivityFeed feedInstance=null, otherParams = null) {
    return utilsService.sendNotificationMail(notificationType, obv, request, userGroupWebaddress, feedInstance, otherParams);
    }

    boolean hasObvLockPerm(obvId, recoId) {
        def observationInstance = Observation.get(obvId.toLong());
        def taxCon = Recommendation.read(recoId.toLong())?.taxonConcept 
        return springSecurityService.isLoggedIn() && (SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') || SpringSecurityUtils.ifAllGranted('ROLE_SPECIES_ADMIN') || (taxCon && speciesPermissionService.isTaxonContributor(taxCon, springSecurityService.currentUser, [SpeciesPermission.PermissionType.ROLE_CONTRIBUTOR, SpeciesPermission.PermissionType.ROLE_CURATOR, SpeciesPermission.PermissionType.ROLE_TAXON_CURATOR, SpeciesPermission.PermissionType.ROLE_TAXON_EDITOR])) ) 
    }
	
	def Map updateInlineCf(params){
		return customFieldService.updateInlineCf(params)
	}

    def Map updateTags(params,observationInstance){
        def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
        def  result = observationInstance.setTags(tags);
        def tagsObj = getRelatedTagsFromObservation(observationInstance);
        def model = [:];
        def new_des = '';
        for ( e in tagsObj ) {
            model.put(e.key,e.value);
            new_des +=(new_des != '')? ','+e.key:e.key;
        }
        def activityFeed = activityFeedService.addActivityFeed(observationInstance, observationInstance,  springSecurityService.currentUser, activityFeedService.OBSERVATION_TAG_UPDATED,new_des);
            utilsService.sendNotificationMail(activityFeedService.OBSERVATION_TAG_UPDATED, observationInstance, null, null, activityFeed);
        return model;
    }

    def Map updateSpeciesGrp(params,observationInstance, boolean sendMail=true){
        def prevgroupIcon = observationInstance.group.iconClass();
       if(observationInstance.group.id != params.group_id){
            def new_des = observationInstance.group.name+" to ";            
            observationInstance.group  = params.group?:SpeciesGroup.get(params.group_id);
            observationInstance.save();
            def activityFeed = activityFeedService.addActivityFeed(observationInstance, observationInstance,  springSecurityService.currentUser, activityFeedService.OBSERVATION_SPECIES_GROUP_UPDATED,new_des+""+observationInstance.group.name);
            if(sendMail)
                utilsService.sendNotificationMail(activityFeedService.OBSERVATION_SPECIES_GROUP_UPDATED, observationInstance, null, null, activityFeed);
        }
        return [ 'groupName' : observationInstance?.group?.name,'groupIcon' : observationInstance.group.iconClass(),'prevgroupIcon':prevgroupIcon,'prev_group':params.group_id]

    }

    Map getRecommendationVotes(Observation observationInstance, int limit, long offset) {
        return getRecommendationVotes([observationInstance], limit, offset).get(observationInstance.id) 
    }

    Map getRecommendationVotes(List<Observation> observationInstanceList, int limit, long offset) {
		if(limit == 0) limit = 3;
        //NOT USING limit and offset as the max distinct recos at the moment is just 5
		def sql =  Sql.newInstance(dataSource);
       
        Map obvListRecoVotesResult = [:];
        if(!observationInstanceList) return obvListRecoVotesResult;
        //This query works on view
        String query = "";
        List queryParams = [];
        if(observationInstanceList.size() == 1) {
            query = "select * from reco_vote_details as rv where rv.observation_id = ?"; 
            queryParams = [observationInstanceList[0].id]
        } else {
            Long[] obvIds = new Long[observationInstanceList.size()];
            observationInstanceList.eachWithIndex { obv, i ->
                obvIds[i] = obv.id
            }
            queryParams = []
            query = "select * from reco_vote_details as rv where rv.observation_id in ("+obvIds.join(',')+") order by rv.observation_id";
        }

        log.debug 'ObservationService : getRecommendationVotes query : '+query+" queryParams: ${queryParams}"
		List recoVotes = sql.rows(query, queryParams);        
		def currentUser = springSecurityService.currentUser;
        Map recoMaps = [:];
	    Long englishId = Language.getLanguage(null).id

        recoVotes.each { recoVote ->
            Map obvRecoVotesResult = obvListRecoVotesResult.get(recoVote.observation_id);
            if(!obvRecoVotesResult) {
                obvRecoVotesResult = ['recoVotes':[], 'totalVotes':0, 'uniqueVotes':0];
                obvListRecoVotesResult.put(recoVote.observation_id, obvRecoVotesResult);
                recoMaps = [:];
            }

            //collecting reco details
            Map map = [:];
            if(recoMaps.containsKey(recoVote.reco_id))  {
                map = recoMaps.get(recoVote.reco_id);
            } else { 
                map.put("recoId", recoVote.reco_id);
                map.put("isScientificName", recoVote.is_scientific_name);

                if(recoVote.taxon_concept_id) {
                    map.put("speciesId", recoVote.species_id);
                    map.put("normalizedForm", recoVote.normalized_form)
                    if(recoVote.status == NameStatus.SYNONYM && recoVote.is_scientific_name) {
                        map.put("synonymOf", recoVote.normalized_form)
                    }
                }
                map.put("name", recoVote.name);
                
                recoMaps[recoVote.reco_id] = map;
                obvRecoVotesResult.recoVotes << map;
            }

            if(recoVote.common_name_reco_id) {
                if(!map.containsKey('commonNames')) {
                    map.put('commonNames', [:]);
                }
                def cnReco = Recommendation.read(recoVote.common_name_reco_id);
                def cnLangId = (cnReco.languageId != null)?(cnReco.languageId):englishId
                if(!map.commonNames.containsKey(cnLangId)) {
                    map.commonNames[cnLangId] = new HashSet();
                }
                map.commonNames[cnLangId].add(cnReco);           
            }

            if(!map.containsKey('authors')) {
                map.put('authors', []);
            }
            //TODO author details
            map["authors"] << [SUser.read(recoVote.author_id), recoVote.original_author];//s.collect{[it.author,it.originalAuthor]})
            if(!map.containsKey('votedOn')) {
                map.put('votedOn', []);
            }
            map["votedOn"] << recoVote.voted_on

            if(!map.containsKey('noOfVotes'))
                map["noOfVotes"] = 0;

            map["noOfVotes"]++;

            if(recoVote.comment) {
                if(!map.containsKey('recoComments'))
                    map["recoComments"] = [];
                map['recoComments'] << [recoVoteId:recoVote.reco_vote_id, comment:recoVote.comment, author:SUser.read(recoVote.author_id), votedOn:recoVote.voted_on]
            }

            map.put("obvId", recoVote.observation_id);
            map.put("isLocked", recoVote.is_locked);
            if(recoVote.is_locked == false) {
                map.put("showLock", true);
            } else {
                if(recoVote.reco_id == recoVote.max_voted_reco_id){
                    map.put("showLock", false);                   
                }
                else{
                    map.put("showLock", true);
                }
            }
		}

        obvListRecoVotesResult.each { key, obvRecoVotesResult ->
            int totalVotes = 0;
            obvRecoVotesResult.recoVotes.each { map ->
                totalVotes += map.noOfVotes;
                if(map.commonNames) {
                    String cNames = Observation.getFormattedCommonNames(map.commonNames, true)
                    map.put("commonNames", (cNames == "")?"":"(" + cNames + ")");
                }
                map.authors.each {
                    if(it[0].id == currentUser?.id) {
                        map.put("disAgree", true);
                    }
                }
            }
            obvRecoVotesResult.totalVotes = totalVotes;
            obvRecoVotesResult.uniqueVotes = obvRecoVotesResult.recoVotes.size();
            obvRecoVotesResult.recoVotes = obvRecoVotesResult.recoVotes.sort {a,b -> b.noOfVotes <=> a.noOfVotes };
        }
        return obvListRecoVotesResult;

        //utilsService.logSql ({
/*
		def recoVoteCount;
		if(limit == -1) {
			recoVoteCount = sql.rows("select recoVote.recommendation_id as recoId, count(*) as votecount from recommendation_vote as recoVote where recoVote.observation_id = :obvId group by recoVote.recommendation_id order by votecount desc", [obvId:observationInstance.id])
		} else {
			recoVoteCount = sql.rows("select recoVote.recommendation_id as recoId, count(*) as votecount from recommendation_vote as recoVote where recoVote.observation_id = :obvId group by recoVote.recommendation_id order by votecount desc limit :max offset :offset", [obvId:observationInstance.id, max:limit, offset:offset])
		}

        uniqueVotes = recoVoteCount.size();

		def currentUser = springSecurityService.currentUser;
		recoVoteCount.each { recoVote ->
			def reco = Recommendation.read(recoVote[0]);
			def map = reco.getRecommendationDetails(observationInstance);
            totalVotes += recoVote[1];
			map.put("noOfVotes", recoVote[1]);
			map.put("obvId", observationInstance.id);
            map.put("isLocked", observationInstance.isLocked);
			def langToCommonName =  observationInstance.suggestedCommonNames(reco.id);
            String cNames = observationInstance.getFormattedCommonNames(langToCommonName, true)

			//String cNames = suggestedCommonNames(reco.id, true)
			map.put("commonNames", (cNames == "")?"":"(" + cNames + ")");
			map.put("disAgree", (currentUser in map.authors));
            if(observationInstance.isLocked == false) {
                map.put("showLock", true);
            } else {
                //def recVo = RecommendationVote.findWhere(recommendation: reco, observation:observationInstance);
                //if(recVo && recVo.recommendation == observationInstance.maxVotedReco){
                if(reco == observationInstance.maxVotedReco){
                    map.put("showLock", false);                   
                }
                else{
                    map.put("showLock", true);
                }
            }
			result.add(map);
		}
        //} , 'getRecommendationVotes');
		return ['recoVotes':result, 'totalVotes':totalVotes, 'uniqueVotes':uniqueVotes];
*/    
    }

    def suggestedCommonNames(Observation observationInstance, Long recoId) {
		def englishId = Language.getLanguage(null).id
		Map langToCommonName = new HashMap()
		observationInstance.recommendationVote.each { rv ->
			if(rv.recommendation.id == recoId){
				def cnReco = rv.commonNameReco
				if(cnReco){
					def cnLangId = (cnReco.languageId != null)?(cnReco.languageId):englishId
					def nameList = langToCommonName.get(cnLangId)
					if(!nameList){
						nameList = new HashSet()
						langToCommonName[cnLangId] = nameList
					}
					nameList.add(cnReco)
				}
			}
		}
        return langToCommonName;
    }


        long getAllSuggestedRecommendationsOfUser(SUser user , UserGroup userGroupInstance = null) {
        //TODO: filter on usergroup if required
        def sql =  Sql.newInstance(dataSource);
        def result
        if(userGroupInstance){
            result = sql.rows("select count(recoVote) from recommendation_vote recoVote, observation o, user_group_observations ugo where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and ugo.observation_id = o.id and ugo.user_group_id =:ugId and recoVote.given_common_name!=''",  [userId:user.id, isDeleted:false, ugId:userGroupInstance.id]);
        } else {
            result = sql.rows("select count(recoVote) from recommendation_vote recoVote, observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and recoVote.given_common_name!=''", [userId:user.id, isDeleted:false]);
        }
  //      def result = RecommendationVote.executeQuery("select count(recoVote) from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable", [userId:user.id, isDeleted:false, isShowable:true]);
        return (long)result[0]["count"];
    }

    def getDistinctIdentifiedRecoList(params, int max, int offset) {  
        println "identified params+"+params;
        def sql =  Sql.newInstance(dataSource);
        def distinctIdentifiedRecoList = [];
        def distinctIdentifiedQuery
        def distinctIdentifiedCountQuery
        def queryParts = getFilteredObservationsFilterQuery(params)
        if(params.userGroup)
        { 
            if(params.sGroup!=829){
                distinctIdentifiedQuery="select recoVote.recommendation_id as voteID,count(*) as count from recommendation_vote recoVote , recommendation reco, observation obv,user_group_observations ugo where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true and obv.group_id="+params.sGroup+" and ugo.user_group_id ="+params.userGroup.id+" and ugo.observation_id=obv.id group by recoVote.recommendation_id order by count(*) desc limit 10 offset "+params.offset+" "
                distinctIdentifiedCountQuery="select count(recoVote.recommendation_id) from recommendation_vote recoVote , recommendation reco, observation obv,user_group_observations ugo where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true and obv.group_id="+params.sGroup+" and ugo.user_group_id ="+params.userGroup.id+" and ugo.observation_id=obv.id group by recoVote.recommendation_id order by count(*) desc "
                }
             else{
                distinctIdentifiedQuery="select recoVote.recommendation_id as voteID,count(*) as count from recommendation_vote recoVote , recommendation reco, observation obv,user_group_observations ugo where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true and ugo.user_group_id ="+params.userGroup.id+" and ugo.observation_id=obv.id group by recoVote.recommendation_id order by count(*) desc limit 10 offset "+params.offset+" "
                distinctIdentifiedCountQuery="select count(recoVote.recommendation_id) from recommendation_vote recoVote , recommendation reco, observation obv,user_group_observations ugo where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true and ugo.user_group_id ="+params.userGroup.id+" and ugo.observation_id=obv.id group by recoVote.recommendation_id order by count(*) desc "
                }
        }
         else
        { 
            if(params.sGroup!=829){
                distinctIdentifiedQuery="select recoVote.recommendation_id as voteID,count(*) as count from recommendation_vote recoVote , recommendation reco, observation obv where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true and obv.group_id="+params.sGroup+"  group by recoVote.recommendation_id order by count(*) desc limit 10 offset "+params.offset+" "
                distinctIdentifiedCountQuery="select count(recoVote.recommendation_id) from recommendation_vote recoVote , recommendation reco, observation obv where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true and obv.group_id="+params.sGroup+"  group by recoVote.recommendation_id order by count(*) desc "
                }
             else{
                distinctIdentifiedQuery="select recoVote.recommendation_id as voteID,count(*) as count from recommendation_vote recoVote , recommendation reco, observation obv where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true group by recoVote.recommendation_id order by count(*) desc limit 10 offset "+params.offset+" "
                distinctIdentifiedCountQuery="select count(recoVote.recommendation_id) from recommendation_vote recoVote , recommendation reco, observation obv where recoVote.observation_id=obv.id and recoVote.recommendation_id=reco.id and reco.is_scientific_name=true and recoVote.author_id="+params.user+" and obv.is_deleted=false and obv.is_showable=true group by recoVote.recommendation_id order by count(*) desc "
                }
        }
                def distinctIdentifiedRecoListResult =sql.rows(distinctIdentifiedQuery)
                def disntinctIdentifiedCount=sql.rows(distinctIdentifiedCountQuery)
                distinctIdentifiedRecoListResult.each {it->
                def reco = Recommendation.read(it['voteid']);
                if(params.downloadFrom == 'uniqueSpecies') {
                    distinctIdentifiedRecoList << [reco.name, reco.isScientificName, it['count'], getSpeciesHardLink(reco)]
                        }
                else {
                    distinctIdentifiedRecoList << [getSpeciesHyperLinkedName(reco), reco.isScientificName, getIdentifiedObservationHardLink(it['voteid'],it['count'],params.user,true)]
                        }
                    }
                def count=disntinctIdentifiedCount.size()
                println "count==="+count
                return [distinctIdentifiedRecoList:distinctIdentifiedRecoList, totalCount:count];
            }

    private String getObservationHardLink(reco,count) {
        if(!reco) return ;
        def link=utilsService.generateLink("observation", "list", ["recom": reco])
        return "" + '<a  href="' +  link +'"><i>' + count + "</i></a>"
        }

    private String getObservationHardLink(reco,count,user) {
        if(!reco) return ;
        def link=utilsService.generateLink("observation", "list", ["recom": reco,"user":user])
        return "" + '<a  href="' +  link +'"><i>' + count + "</i></a>"
        //return link 
        }

    private String getIdentifiedObservationHardLink(reco,count,user,identified) {
        if(!reco) return ;
        def link=utilsService.generateLink("observation", "list", ["recom": reco,"user":user,"identified":true])
        return "" + '<a  href="' +  link +'"><i>' + count + "</i></a>"
        //return link 
        }

    def getObservationInstanceList(params){
        def finalInstanceResult=[]
        def sql =  Sql.newInstance(dataSource);
        def result=sql.rows("select obv.id from recommendation_vote recoVote,observation obv where recoVote.observation_id=obv.id and recoVote.recommendation_id=:rId and recoVote.author_id=:userId",[rId:(params.recom).toInteger(), userId:(params.user).toInteger()]);

        for(row in result){
            finalInstanceResult.add(Observation.findById(row.getProperty("id")))
        }
        return finalInstanceResult
    }

/*
    boolean factUpdate(params){
        println "================="+params
        def observationInstance = Observation.findById(params.observation);
        def traitParams = ['contributor':observationInstance.author.email, 'attribution':observationInstance.author.email, 'license':License.LicenseType.CC_BY.value()];
        traitParams.putAll(getTraits(params.traits));
        Trait trait;
        TraitValue traitValue;
        def factInstance;
        traitParams.each { key, value ->
                    if(!value) {
                        return;
                    }
                    key = key.trim();
                    value = value ? value.trim() : null ;

                    switch(key) {
                        case ['name', 'taxonid', 'attribution','contributor', 'license'] : break;
                        default :
                        trait=Trait.findById(key);
                        traitValue = TraitValue.findByTraitAndValueIlike(trait, value.trim());
                        factInstance=Fact.findByIdAndTrait(params.factId,trait);
                        
                    }
                }
                
                if(!factInstance){factInstance = new Fact();}
                        factInstance.trait = trait
                        factInstance.traitValue = traitValue;
                        factInstance.objectId = observationInstance.id
                        factInstance.attribution = traitParams['attribution'];
                        factInstance.contributor = traitParams['contributor'] ? SUser.findByEmail(traitParams['contributor']?.trim()) : null;
                        factInstance.license = traitParams['license']? License.findByName(License.fetchLicenseType(traitParams['license'].trim())) : null;
                        factInstance.objectType = observationInstance.class.getCanonicalName(); 

                if(!factInstance.hasErrors() && !factInstance.save()) { 
                    println "Error in Fact upudate"
                    factInstance.errors.allErrors.each {println it }
                    return false;
                } else {
                    println "Successfully updated fact";
                    return true;
                }
               // factService.updateFacts(traitParams, observationInstance);
    }
*/
}
