package speciespage

import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.taggable.TagLink;

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
import species.AbstractObjectService;
import species.participation.UsersResource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder as LCH;


class ObservationService extends AbstractObjectService {

    static transactional = false

    def recommendationService;
    def observationsSearchService;
    //def curationService;
    def userGroupService;
    def activityFeedService;
    def SUserService;
    //def speciesService;
    def messageSource;
    def resourcesService;
    def request;
    /**
     * 
     * @param params
     * @return
     */
    Observation createObservation(params) {
        //log.info "Creating observations from params : "+params
        Observation observation = new Observation();
        updateObservation(params, observation);
        return observation;
    }
    /**
     * 
     * @param params
     * @param observation
     */
    void updateObservation(params, observation, boolean updateResources = true){
        //log.debug "Updating obv with params ${params}"
        
        if(params.author)  {
            observation.author = params.author;
        }

        if(params.url) {
            observation.url = params.url;
        }
        observation.group = params.group?:SpeciesGroup.get(params.group_id);
        observation.notes = params.notes;
        if( params.fromDate != ""){
            observation.fromDate = parseDate(params.fromDate);
            observation.toDate = params.toDate ? parseDate(params.toDate) : observation.fromDate
        }
        observation.placeName = params.placeName//?:observation.reverseGeocodedName;
        observation.reverseGeocodedName = params.reverse_geocoded_name?:observation.placeName

        //observation.location = 'POINT(' + params.longitude + ' ' + params.latitude + ')'
        observation.locationAccuracy = params.location_accuracy?:params.locationAccuracy;
        observation.geoPrivacy = params.geoPrivacy ? (params.geoPrivacy.trim().toLowerCase().toBoolean()):false;

        observation.habitat = params.habitat?:Habitat.get(params.habitat_id);

        observation.agreeTerms = (params.agreeTerms?.equals('on'))?true:false;
            println "+++++++++++++++++++++++++++++++++++++++++++++++++++++"
            println "1 SETTING LICENCE TYPE"
            println observation.agreeTerms
            println params
        if(params.license_0) {
            log.debug "Setting license to ${params.license_0}"
            observation.license = (new XMLConverter()).getLicenseByType(params.license_0, false)
            println observation.license
        } else if(observation.agreeTerms) {
            println "+++++++++++++++++++++++++++++++++++++++++++++++++++++"
            println "SETTING LICENCE TYPE"
            log.debug "Setting license to ${LicenseType.CC_BY}"
            observation.license = (new XMLConverter()).getLicenseByType(LicenseType.CC_BY, false)
        }
        observation.sourceId = params.sourceId ?: observation.sourceId
        observation.checklistAnnotations = params.checklistAnnotations?:observation.checklistAnnotations
        observation.language = params.locale_language;

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
        //        if(params.latitude && params.longitude) {
        //            observation.topology = geometryFactory.createPoint(new Coordinate(params.longitude?.toFloat(), params.latitude?.toFloat()));
        //        } else 
        if(params.areas) {
            WKTReader wkt = new WKTReader(geometryFactory);
            try {
                Geometry geom = wkt.read(params.areas);
                observation.topology = geom;
            } catch(ParseException e) {
                log.error "Error parsing polygon wkt : ${params.areas}"
            }
        }

		//XXX: in all normal case updateResources flag will be true, but when updating some checklist and checklist
		// has some global update like habitat, group in that case updating its observation info but not the resource info
		if(updateResources){


	        def resourcesXML = createResourcesXML(params);
            def instance = observation
            if(params.action == "bulkSave"){
                instance = springSecurityService.currentUser
            }
	        def resources = saveResources(instance, resourcesXML);
	        observation.resource?.clear();
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            
	        resources.each { resource ->
                if(!resource.context){
                    resource.saveResourceContext(observation)
                }
	            observation.addToResource(resource);
                
	        }
            
		}
    }

    Map saveObservation(params, sendMail=true, boolean updateResources = true){
        //TODO:edit also calls here...handle that wrt other domain objects
        params.author = springSecurityService.currentUser;
        def observationInstance, feedType, feedAuthor, mailType; 
        try {

            if(params.action == "save" || params.action == "bulkSave"){
                observationInstance = createObservation(params);
                feedType = activityFeedService.OBSERVATION_CREATED
                feedAuthor = observationInstance.author
                mailType = utilsService.OBSERVATION_ADDED
            }else{
                observationInstance = Observation.get(params.id.toLong())
                params.author = observationInstance.author;
                updateObservation(params, observationInstance, updateResources)
                feedType = activityFeedService.OBSERVATION_UPDATED
                feedAuthor = springSecurityService.currentUser
                if(params.action == "update") {
                    mailType = activityFeedService.OBSERVATION_UPDATED
                }
            }
println "---------------------------------------------------"
println observationInstance.license
            if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
                //flash.message = "${message(code: 'default.created.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
                log.debug "Successfully created observation : "+observationInstance
                params.obvId = observationInstance.id
                activityFeedService.addActivityFeed(observationInstance, null, feedAuthor, feedType);

                saveObservationAssociation(params, observationInstance, sendMail)

                if(sendMail)
                    utilsService.sendNotificationMail(mailType, observationInstance, null, params.webaddress);

                params["createNew"] = true
                params["oldAction"] = params.action
                log.debug "============PARAMS ACTION=============== " + params.action
                log.debug "============FOR OBSERVATION ============ " + observationInstance
                String uuidRand =  UUID.randomUUID().toString()
                log.debug "==================UUID GENERATED======== " + uuidRand
                observationInstance.resource.each { resource ->
                    log.debug "=============FOR RESOURCE=========== " + resource + " =======ITS CONTEXT ======== " + resource.context?.value() + " =====ITS FILE NAME===== " +  resource.fileName
                    if(resource.context?.value() == Resource.ResourceContext.USER.toString()){
                        log.debug "========CONTEXT IS USER========= " 
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
                        log.debug "=======UPDATING RESOURCE CONTEXT======"
                        resource.saveResourceContext(observationInstance)

                        def usersRes = UsersResource.findByRes(resource)
                        ////////////////////
                        ////  CHECK STATUS SET CORRECT----DOES CHECKLIST CALL COME HERE???
                        log.debug "============UPDATING STATUS OF THIS USER RESOURCE========== " + usersRes
                        usersRes.status = UsersResource.UsersResourceStatus.USED_IN_OBV
                        if(!usersRes.save(flush:true)){
                            usersRes.errors.allErrors.each { log.error it }
                            return false
                        }

                    }
                }
                return ['success' : true, observationInstance:observationInstance]
            } else {
                observationInstance.errors.allErrors.each { log.error it }
                def errors = [];
                observationInstance.errors.allErrors .each {
                    def formattedMessage = messageSource.getMessage(it, null);
                    errors << [field: it.field, message: formattedMessage]
                }
                return ['success' : false, 'msg':'Failed to save observation', 'errors':errors, observationInstance:observationInstance]
            }
        } catch(e) {
            e.printStackTrace();
            return ['success' : false, 'msg':e.getMessage(), observationInstance:observationInstance]
        }
    }

    /**
     * @param params
     * @param observationInstance
     * @return
     * saving Groups, tags and resources
     */
    def saveObservationAssociation(params, observationInstance, boolean sendMail = true){
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
    }

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
            relatedObv = getRelatedObservationBySpeciesName(params.id.toLong(), max, offset)
        } else if(params.filterProperty == "speciesGroup"){
            relatedObv = getRelatedObservationBySpeciesGroup(params.filterPropertyValue.toLong(),  max, offset)
        } else if(params.filterProperty == "featureBy") {
            Long ugId = getUserGroup(params)?.id;
            relatedObv = getFeaturedObject(ugId, max, offset, params.controller)
        } else if(params.filterProperty == "user"){
            relatedObv = getRelatedObservationByUser(params.filterPropertyValue.toLong(), max, offset, params.sort, params.webaddress)
        } else if(params.filterProperty == "nearByRelated"){
            relatedObv = getNearbyObservationsRelated(params.id, max, offset)
        } else if(params.filterProperty == "nearBy"){
            float lat = params.lat?params.lat.toFloat():-1;
            float lng = params.long?params.long.toFloat():-1;
            relatedObv = getNearbyObservations(lat,lng, max, offset)
        } else if(params.filterProperty == "taxonConcept") {
            relatedObv = getRelatedObservationByTaxonConcept(params.filterPropertyValue.toLong(), max, offset)
        } else if(params.filterProperty == "latestUpdatedObservations") {
            relatedObv = getLatestUpdatedObservation(params.webaddress,params.sort, max, offset)
        } else if(params.filterProperty == "latestUpdatedSpecies") {
            relatedObv = speciesService.getLatestUpdatedSpecies(params.webaddress,params.sort, max, offset)
        } 
        else if(params.filterProperty == 'bulkUploadResources') {
            relatedObv = resourcesService.getBulkUploadResourcesOfUser(SUser.read(params.filterPropertyValue.toLong()), max, offset)
        }
        
        else{
            if(params.id) {
                relatedObv = getRelatedObservation(params.filterProperty, params.id.toLong(), max, offset)
            } else {
                log.debug "no id"
            }
        }

        if((params.contextGroupWebaddress || params.webaddress) && (params.filterProperty != 'bulkUploadResources') ){
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
    Map getRelatedObservationBySpeciesName(long obvId, int limit, int offset){
        return getRelatedObservationBySpeciesNames(obvId, limit, offset)
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
            count = sql.rows("select count(*) from observation obv , user_group_observations ugo where obv.author_id = :userId and ugo.observation_id = obv.id and ugo.user_group_id =:ugId and obv.is_deleted = :isDeleted and obv.is_showable = :isShowable", [isDeleted:false, userId:userId, isDeleted: false, isShowable : true , ugId:userGroupInstance.id]);
        } else {
            count = sql.rows("select count(*) from observation obv where obv.author_id = :userId and obv.is_deleted = :isDeleted and obv.is_showable = :isShowable", [isDeleted:false, userId:userId, isDeleted: false, isShowable : true]);
                 
        }
        
        def queryParams = [isDeleted:false]
        def countQuery = "select count(*) from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted and obv.isShowable = :isShowable "
        queryParams["userId"] = userId
        queryParams["isDeleted"] = false;
        queryParams["isShowable"] = true;
        //def count = Observation.executeQuery(countQuery, queryParams)
        
        //getting observations
        def query = "from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted and obv.isShowable = :isShowable "
        def orderByClause = "order by obv." + (sort ? sort : "createdOn") +  " desc"
        query += orderByClause

        queryParams["max"] = limit
        queryParams["offset"] = offset
        def observationsRows
        if(userGroupInstance) {
            observationsRows = sql.rows("select obv.id from observation obv , user_group_observations ugo where obv.author_id = :userId and ugo.observation_id = obv.id and ugo.user_group_id =:ugId and obv.is_deleted = :isDeleted and obv.is_showable = :isShowable " + "order by obv." + (sort ? sort : "created_on") +  " desc limit :max offset :offset", [isDeleted:false, userId:userId, isDeleted: false, isShowable : true , ugId:userGroupInstance.id , max:limit, offse:offset]);
        } else {
            observationsRows = sql.rows("select obv.id from observation obv where obv.author_id = :userId and obv.is_deleted = :isDeleted and obv.is_showable = :isShowable " + "order by obv." + (sort ? sort : "created_on") +  " desc limit :max offset :offset", [isDeleted:false, userId:userId, isDeleted: false, isShowable : true, max:limit, offset:offset]);
                 
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
    Map getRelatedObservationBySpeciesNames(long obvId, int limit, int offset){
        Observation parentObv = Observation.read(obvId);
        if(!parentObv.maxVotedReco) {
            return ["observations":[], "count":0];
        }
        return getRelatedObservationByReco(obvId, parentObv.maxVotedReco, limit, offset)
    }

    private Map getRelatedObservationByReco(long obvId, Recommendation maxVotedReco, int limit, int offset) {
        def observations = Observation.withCriteria () {
            projections {
                groupProperty('sourceId')
                groupProperty('isShowable')
				groupProperty('lastRevised')
            }
            and {
                eq("maxVotedReco", maxVotedReco)
                eq("isDeleted", false)
                if(obvId) ne("id", obvId)
            }
            order("isShowable", "desc")
            order("lastRevised", "desc")
            if(limit >= 0) maxResults(limit)
                firstResult (offset?:0)
        }

        def result = [];
        observations.each {
            def obv = Observation.get(it[0])
            result.add(['observation':obv, 'title':(obv.isChecklist)? obv.title : maxVotedReco.name]);
        }

        if(limit < 0)
            return ["observations":result];

        def count = Observation.createCriteria().count {
            projections {
                count(groupProperty('sourceId'))
            }
            and {
                eq("maxVotedReco", maxVotedReco)
                eq("isDeleted", false)
                if(obvId) ne("id", obvId)
            }
        }
        return ["observations":result, "count":count]
    }
    
    Map getRelatedObvForSpecies(resInstance, int limit, int offset){
        def taxonConcept = resInstance.taxonConcept
        List<Recommendation> scientificNameRecos = recommendationService.searchRecoByTaxonConcept(taxonConcept);
        def resList = []
        def obvLinkList = []
        if(scientificNameRecos){
            def resIdList = Observation.executeQuery ('''
                select r.id, obv.id from Observation obv join obv.resource r where obv.maxVotedReco in (:scientificNameRecos) and obv.isDeleted = :isDeleted order by obv.lastRevised desc
                ''', ['scientificNameRecos': scientificNameRecos, 'isDeleted': false, max : limit.toInteger(), offset: offset.toInteger()]);

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
        } else{
            return ['resList': resList, 'obvLinkList': obvLinkList, 'count': 0]
        }
    }

    Map getRelatedObservationByTaxonConcept(long taxonConceptId, int limit, long offset){
        def taxonConcept = TaxonomyDefinition.read(taxonConceptId);
        if(!taxonConcept) return ['observations':[], 'count':0]

            List<Recommendation> scientificNameRecos = recommendationService.searchRecoByTaxonConcept(taxonConcept);
        if(scientificNameRecos) {
            def criteria = Observation.createCriteria();
            def observations = criteria.list (max: limit, offset: offset) {
                and {
                    'in'("maxVotedReco", scientificNameRecos)
                        eq("isDeleted", false)
                        eq("isShowable", true)
                }
                order("lastRevised", "desc")
            }
            def count = observations.totalCount;
            def result = [];
            def iter = observations.iterator();
            while(iter.hasNext()){
                def obv = iter.next();
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
        return getRecommendations(params.recoName, params.canName, params.commonName, params.languageName)
    }

    /**
    * recoName
    * canName
    * commonName
    * languageName
    * 
    **/
    Map getRecommendations(String recoName, String canName, String commonName, String languageName) {
        def languageId = Language.getLanguage(languageName).id;
        //		def refObject = params.observation?:Observation.get(params.obvId);

        //if source of recommendation is other that observation (i.e Checklist)
        //		refObject = refObject ?: params.refObject
        Recommendation commonNameReco = recommendationService.findReco(commonName, false, languageId, null);
        Recommendation scientificNameReco = recommendationService.getRecoForScientificName(recoName, canName, commonNameReco);

        //		curationService.add(scientificNameReco, commonNameReco, refObject, springSecurityService.currentUser);

        //giving priority to scientific name if its available. same will be used in determining species call
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
        int maxRadius = 200;
        int maxObvs = 50;
        def sql =  Sql.newInstance(dataSource);
        long totalResultCount = 0;
        def nearbyObservations = []

        try {
            def rows = sql.rows("select count(*) as count from observation as g1, observation as g2 where ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g2.is_showable = true and g1.id = :observationId and g1.id <> g2.id", [observationId: Long.parseLong(observationId), maxRadius:maxRadius]);
            totalResultCount = Math.min(rows[0].getProperty("count"), maxObvs);
            limit = Math.min(limit, maxObvs - offset);
            def resultSet = sql.rows("select g2.id,  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) as distance from observation as g1, observation as g2 where  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g2.is_showable = true and g1.id = :observationId and g1.id <> g2.id order by ST_Distance(g1.topology, g2.topology), g2.last_revised desc limit :max offset :offset", [observationId: Long.parseLong(observationId), maxRadius:maxRadius, max:limit, offset:offset])

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

    Map getNearbyObservations(float latitude, float longitude, int limit, int offset) {
        if(!latitude || ! longitude) return [count:0];
        int maxRadius = 200;
        int maxObvs = 50;
        def nearbyObservations = []
        long totalResultCount = 0;
        def sql =  Sql.newInstance(dataSource);
        try {
            String point = "ST_GeomFromText('POINT(${longitude} ${latitude})',${ConfigurationHolder.getConfig().speciesPortal.maps.SRID})"
            def rows = sql.rows("select count(*) as count from observation as g2 where ROUND(ST_Distance_Sphere(${point}, ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g2.is_showable = true", [maxRadius:maxRadius]);
            totalResultCount = Math.min(rows[0].getProperty("count"), maxObvs);
            limit = Math.min(limit, maxObvs - offset);
            def resultSet = sql.rows("select g2.id,  ROUND(ST_Distance_Sphere(${point}, ST_Centroid(g2.topology))/1000) as distance from observation as g2 where  ROUND(ST_Distance_Sphere(${point}, ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g2.is_showable = true order by ST_Distance(${point}, g2.topology), g2.last_revised desc limit :max offset :offset", [maxRadius:maxRadius, max:limit, offset:offset])

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
    Map getFilteredObservations(def params, max, offset, isMapView = false) {

        def queryParts = getFilteredObservationsFilterQuery(params) 
        String query = queryParts.query;
        long checklistCount = 0, allObservationCount = 0;

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

        log.debug query;
        log.debug queryParts.queryParams;

        def checklistCountQuery = sessionFactory.currentSession.createQuery(queryParts.checklistCountQuery)
        def allObservationCountQuery = sessionFactory.currentSession.createQuery(queryParts.allObservationCountQuery)
        //def distinctRecoQuery = sessionFactory.currentSession.createQuery(queryParts.distinctRecoQuery)
        //def speciesGroupCountQuery = sessionFactory.currentSession.createQuery(queryParts.speciesGroupCountQuery)

        def hqlQuery = sessionFactory.currentSession.createQuery(query)
        if(params.bounds && boundGeometry) {
            hqlQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
            checklistCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
            allObservationCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
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
        def observationInstanceList = hqlQuery.list();

        def distinctRecoList = [];
        def speciesGroupCountList = [];
        checklistCountQuery.setProperties(queryParts.queryParams)
        checklistCount = checklistCountQuery.list()[0]

        allObservationCountQuery.setProperties(queryParts.queryParams)
        allObservationCount = allObservationCountQuery.list()[0]

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
    def getFilteredObservationsFilterQuery(params) {
        params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        params.habitat = params.habitat.toLong()
        //params.userName = springSecurityService.currentUser.username;

        def queryParams = [isDeleted : false]
        def activeFilters = [:]

        def query = "select "

        if(!params.sort || params.sort == 'score') {
            params.sort = "lastRevised"
        }
        def orderByClause = "  obv." + params.sort +  " desc, obv.id asc"

        if(params.fetchField) {
            query += " obv.id as id,"
            params.fetchField.split(",").each { fetchField ->
                if(!fetchField.equalsIgnoreCase('id'))
                    query += " obv."+fetchField+" as "+fetchField+","
            }
            query = query [0..-2];
            queryParams['fetchField'] = params.fetchField
        } else {
            query += " obv "
        }
        query += " from Observation obv "
        //def mapViewQuery = "select obv.id, obv.topology, obv.isChecklist from Observation obv "

        def userGroupQuery = " ", tagQuery = '', featureQuery = '';
        def filterQuery = " where obv.isDeleted = :isDeleted "
        
        if(params.featureBy == "true" || params.userGroup || params.webaddress){
            params.userGroup = getUserGroup(params);
        }

        if(params.featureBy == "true" ) {
            if(params.userGroup == null) {
                filterQuery += " and obv.featureCount > 0 "     
                //featureQuery = " join (select f.objectId, f.objectType from Featured f group by f.objectType, f.objectId) as feat"
              //  featureQuery = ", select distinct Featured.objectId from Featured where Featured.objectType = :featType as feat "
            } else {
                featureQuery = ", Featured feat "
            }
            query += featureQuery;
            //filterQuery += " and obv.featureCount > 0 "
            if(params.userGroup == null) {
                //filterQuery += " and feat.userGroup is null "     
            }else {
                filterQuery += " and obv.id = feat.objectId and (feat.objectType =:featType or feat.objectType=:featType1) and feat.userGroup.id = :userGroupId "
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
            filterQuery += " and obv.id != feat.objectId and feat.objectType = :featType "
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Observation.class.getCanonicalName();

        }

        if(params.sGroup){
            params.sGroup = params.sGroup.toLong()
            def groupId = getSpeciesGroupIds(params.sGroup)
            if(!groupId){
                log.debug("No groups for id " + params.sGroup)
            }else{
                filterQuery += " and obv.group.id = :groupId "
                queryParams["groupId"] = groupId
                activeFilters["sGroup"] = groupId
            }
        }

        if(params.userGroup || params.webaddress) {
            log.debug "Filtering from usergourp : ${params.userGroup}"
            userGroupQuery = " join obv.userGroups userGroup "
            query += userGroupQuery
            filterQuery += " and userGroup.id =:userGroupId "
            queryParams['userGroupId'] = params.userGroup.id
            queryParams['userGroup'] = params.userGroup
        }

        if(params.tag){
            tagQuery = ",  TagLink tagLink "
            query += tagQuery;
            //mapViewQuery = "select obv.topology from Observation obv, TagLink tagLink "
            filterQuery +=  " and obv.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

            queryParams["tag"] = params.tag
            queryParams["tagType"] = GrailsNameUtils.getPropertyName(Observation.class);
            activeFilters["tag"] = params.tag
        }

        if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
            filterQuery += " and obv.habitat.id = :habitat "
            queryParams["habitat"] = params.habitat
            activeFilters["habitat"] = params.habitat
        }

        if(params.user){
            filterQuery += " and obv.author.id = :user "
            queryParams["user"] = params.user.toLong()
            activeFilters["user"] = params.user.toLong()
        }

        if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)){
            filterQuery += " and (obv.isChecklist = false and obv.maxVotedReco is null) "
            //queryParams["speciesName"] = params.speciesName
            activeFilters["speciesName"] = params.speciesName
        }

        if(params.isFlagged && params.isFlagged.toBoolean()){
            filterQuery += " and obv.flagCount > 0 "
            activeFilters["isFlagged"] = params.isFlagged.toBoolean()
        }

        if(params.daterangepicker_start && params.daterangepicker_end){
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

            def boundGeometry = getBoundGeometry(swLat, swLon, neLat, neLon)
            filterQuery += " and within (obv.topology, :boundGeometry) = true " //) ST_Contains( :boundGeomety,  obv.topology) "
            //filterQuery += " and 1=0 ";// and obv.latitude > " + swLat + " and  obv.latitude < " + neLat + " and obv.longitude > " + swLon + " and obv.longitude < " + neLon
            queryParams['boundGeometry'] = boundGeometry
            activeFilters["bounds"] = params.bounds
        } 

        if(params.type == 'nearBy' && params.lat && params.long) {
            String point = "ST_GeomFromText('POINT(${params.long.toFloat()} ${params.lat.toFloat()})',${ConfigurationHolder.getConfig().speciesPortal.maps.SRID})"
            filterQuery += " and ROUND(ST_Distance_Sphere(${point}, ST_Centroid(obv.topology))/1000) < :maxRadius";
            int maxRadius = params.maxRadius?params.int('maxRadius'):200
            queryParams['maxRadius'] = maxRadius;

            activeFilters["lat"] = params.lat
            activeFilters["long"] = params.long
            activeFilters["maxRadius"] = maxRadius
            
            orderByClause = " ST_Distance(${point}, obv.topology)" 
        }

        String checklistObvCond = ""
        if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()){
            checklistObvCond = " and obv.id != obv.sourceId "
        }

        def distinctRecoQuery = "select obv.maxVotedReco.id, count(*) from Observation obv  "+ userGroupQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery+checklistObvCond+ " and obv.maxVotedReco is not null group by obv.maxVotedReco order by count(*) desc,obv.maxVotedReco.id asc";
        def distinctRecoCountQuery = "select count(distinct obv.maxVotedReco.id)   from Observation obv  "+ userGroupQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery+ checklistObvCond + " and obv.maxVotedReco is not null ";

        def speciesGroupCountQuery = "select obv.group.name, count(*),(case when obv.maxVotedReco.id is not null  then 1 else 2 end) from Observation obv  "+ userGroupQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery+ " and obv.isChecklist=false " + checklistObvCond + "group by obv.group.name,(case when obv.maxVotedReco.id is not null  then 1 else 2 end) order by obv.group.name desc";

        filterQuery += " and obv.isShowable = true ";

        if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()){
            filterQuery += " and obv.isChecklist = true "
            activeFilters["isChecklistOnly"] = params.isChecklistOnly.toBoolean()
        }


        def checklistCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery + " and obv.isChecklist = true "
        def allObservationCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery

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

    Date parseDate(date){
        try {
            return date? Date.parse("dd/MM/yyyy", date):new Date();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
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
                eq("isShowable", true)
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
            result = sql.rows("select count(recoVote) from recommendation_vote recoVote, observation o, user_group_observations ugo where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable and ugo.observation_id = o.id and ugo.user_group_id =:ugId", [userId:user.id, isDeleted:false, isShowable:true, ugId:userGroupInstance.id]);
        } else {
            result = sql.rows("select count(recoVote) from recommendation_vote recoVote, observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable", [userId:user.id, isDeleted:false, isShowable:true]);
        }
  //      def result = RecommendationVote.executeQuery("select count(recoVote) from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable", [userId:user.id, isDeleted:false, isShowable:true]);
        return (long)result[0]["count"];
    }

    List getRecommendationsOfUser(SUser user, int max, long offset , UserGroup userGroupInstance = null) {
        def sql =  Sql.newInstance(dataSource);
        if(max == -1) {
            def recommendationVotesList
            if(userGroupInstance){
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o , user_group_observations ugo where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable and ugo.observation_id = o.id and ugo.user_group_id =:ugId order by recoVote.voted_on desc", [userId:user.id, isDeleted:false, isShowable:true , ugId:userGroupInstance.id]);
            } else {
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable order by recoVote.voted_on desc", [userId:user.id, isDeleted:false, isShowable:true]);
            }
            //def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable order by recoVote.votedOn desc", [userId:user.id, isDeleted:false, isShowable:true], [max:max, offset:offset]);
            def finalResult = []
            
            for (row in recommendationVotesList) {
                finalResult.add(RecommendationVote.findById(row.getProperty("id")))
            }
            //return recommendationVotesList;
            return finalResult;

            /*
            def recommendationVotesList = sql.rows("select recoVote from recommendation_vote recoVote , observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable order by recoVote.voted_on desc", [userId:user.id, isDeleted:false, isShowable:true])
            //def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable order by recoVote.votedOn desc", [userId:user.id, isDeleted:false, isShowable:true]);
            return recommendationVotesList;
            */
        } else {
            def recommendationVotesList
            if(userGroupInstance){
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o , user_group_observations ugo where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable and ugo.observation_id = o.id and ugo.user_group_id =:ugId order by recoVote.voted_on desc limit :max offset :offset", [userId:user.id, isDeleted:false, isShowable:true ,max:max, offset:offset, ugId:userGroupInstance.id]);
            } else {
                recommendationVotesList = sql.rows("select recoVote.id from recommendation_vote recoVote , observation o where recoVote.author_id = :userId and recoVote.observation_id = o.id and o.is_deleted = :isDeleted and o.is_showable = :isShowable order by recoVote.voted_on desc limit :max offset :offset", [userId:user.id, isDeleted:false, isShowable:true ,max:max, offset:offset]);
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

    def setUserGroups(Observation observationInstance, List userGroupIds, boolean sendMail = true) {
        if(!observationInstance) return

        def obvInUserGroups = observationInstance.userGroups.collect { it.id + ""}
        def toRemainInUserGroups =  obvInUserGroups.intersect(userGroupIds);
        if(userGroupIds.size() == 0) {
            userGroupService.removeObservationFromUserGroups(observationInstance, obvInUserGroups, sendMail);
        } else {
            userGroupIds.removeAll(toRemainInUserGroups)
            userGroupService.postObservationtoUserGroups(observationInstance, userGroupIds, sendMail);
            obvInUserGroups.removeAll(toRemainInUserGroups)
            userGroupService.removeObservationFromUserGroups(observationInstance, obvInUserGroups, sendMail);
        }
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
                    if(isFeatureDeleted && SUserService.ifOwns(observationInstance.author)) {
                        def mailType = observationInstance.instanceOf(Checklists) ? utilsService.CHECKLIST_DELETED : utilsService.OBSERVATION_DELETED
                        try {
                            observationInstance.isDeleted = true;
                            observationInstance.deleteFromChecklist();
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

    /**
     */
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
            query += searchFieldsConfig.AUTHOR_ID+":"+springSecurityService.currentUser.id.toLong()+" AND "
        } else {
            //query += "*:*";
        }
        query += searchFieldsConfig.LOCATION_EXACT+":"+params.term+'*'?:'*';

        paramsList.add("q", query);
        paramsList.add("fl", searchFieldsConfig.LOCATION_EXACT+','+searchFieldsConfig.LATLONG+','+searchFieldsConfig.TOPOLOGY);
        paramsList.add("start", 0);
        paramsList.add("rows", 20);
        paramsList.add("sort", searchFieldsConfig.UPDATED_ON+' desc,'+searchFieldsConfig.SCORE + " desc ");

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

        def distinctRecoQuery = sessionFactory.currentSession.createQuery(queryParts.distinctRecoQuery)
        def distinctRecoCountQuery = sessionFactory.currentSession.createQuery(queryParts.distinctRecoCountQuery)

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
            distinctRecoList << [getSpeciesHyperLinkedName(reco), reco.isScientificName, it[1]]
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

    /**
     */
    def getSpeciesGroupCount(params) {
        def distinctRecoList = [];

        def queryParts = getFilteredObservationsFilterQuery(params) 
        def boundGeometry = queryParts.queryParams.remove('boundGeometry'); 

        log.debug "speciesGroupCountQuery : "+queryParts.speciesGroupCountQuery;

        def speciesGroupCountQuery = sessionFactory.currentSession.createQuery(queryParts.speciesGroupCountQuery)

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

        def hqlQuery = sessionFactory.currentSession.createQuery(query)
        if(params.bounds && boundGeometry) {
            hqlQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(new org.hibernatespatial.GeometryUserType()))
        } 
        
        hqlQuery.setMaxResults(1000);
        hqlQuery.setFirstResult(0);

        hqlQuery.setProperties(queryParts.queryParams);
        def observationInstanceList = hqlQuery.list();

        return [observations:observationInstanceList, geoPrivacyAdjust:Utils.getRandomFloat()]
    }

    /*
     * used for download and post in bulk
     */
    def getObservationList(params, max, offset, action){
		if(Utils.isSearchAction(params, action)){
            //getting result from solr
            def idList = getFilteredObservationsFromSearch(params, max, offset, false).totalObservationIdList
            def res = []
            idList.each { obvId ->
                res.add(Observation.read(obvId))
            }
            return res
        }else if(params.webaddress){
            def userGroupInstance =	userGroupService.get(params.webaddress)
            if (!userGroupInstance){
                log.error "user group not found for id  $params.id  and webaddress $params.webaddress"
                return []
            }
            return getUserGroupObservations(userGroupInstance, params, max, offset).observationInstanceList;
        }
        else{
            return getFilteredObservations(params, max, offset, false).observationInstanceList

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

}
