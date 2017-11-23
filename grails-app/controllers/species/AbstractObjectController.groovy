package species;

import java.util.List;
import java.util.Map;

import org.grails.taggable.*
import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder;
import groovy.xml.StreamingMarkupBuilder;
import groovy.xml.XmlUtil;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import grails.plugin.springsecurity.SpringSecurityUtils;

import grails.converters.JSON;
import grails.converters.XML;

import grails.plugin.springsecurity.annotation.Secured
import grails.util.Environment;
import species.participation.RecommendationVote.ConfidenceType
import species.participation.Flag.FlagType
import species.utils.ImageType;
import species.utils.ImageUtils
import species.utils.Utils;
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.groups.UserGroupController;
import species.Habitat;
import species.Species;
import species.participation.Observation;
import species.Resource;
import species.BlockedMails;
import species.Resource.ResourceType;
import species.auth.SUser;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList
import species.participation.Featured
import species.participation.ResourceRedirect


import static org.springframework.http.HttpStatus.*;
import grails.plugin.cache.Cacheable;

abstract class AbstractObjectController {
    
    def utilsService;
    def observationService;

    def related() {
        def result = [];

        if(params.filterProperty?.equals('featureBy')) {
            String cacheKey = "${params.webaddress?:'IBP'}-${params.controller}-${params.action}-${params.filterProperty}-${params.filterPropertyValue}-${params.max?:1}-${params.offset?:0}"
            String cacheName = 'featured';

            result = utilsService.getFromCache(cacheName, cacheKey);
            if(!result) {
                def relatedObv = observationService.getRelatedObservations(params).relatedObv;
                result = formatRelatedResults(relatedObv, params);
                utilsService.putInCache(cacheName, cacheKey, result);
            }
        } else {
            def relatedObv = observationService.getRelatedObservations(params).relatedObv;
            result = formatRelatedResults(relatedObv, params);
        }

        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    protected formatRelatedResults(relatedObv, params) {
        if(params.filterProperty != 'bulkUploadResources') {
            if(relatedObv) {
                if(relatedObv.observations)
                    relatedObv.observations = observationService.createUrlList2(relatedObv.observations, observationService.getIconBasePath(params.controller));
            } else {
                log.debug "no related observations"
            }
        } else {
            def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
            List urlList = []
            for(param in relatedObv.observations){
                def res = param['observation'];
                def item = [:]
                item.id = res.id
                if(res.type == ResourceType.IMAGE) {
                    item.imageLink = res.thumbnailUrl(config.speciesPortal.usersResource.serverURL)//thumbnailUrl(iconBasePath)
                } else if(res.type == ResourceType.VIDEO) {
                    item.imageLink = res.thumbnailUrl()
                } else if(res.type == ResourceType.AUDIO) {
                    item.imageLink = config.grails.serverURL+"/images/audioicon.png"
                } 
                item.url = "/resource/bulkUploadResources"
                item.title = param['title']
                item.type = 'resource'
                if(param.inGroup) {
                    item.inGroup = param.inGroup;
                } 
                urlList << item;
            }
            relatedObv.observations = urlList
        }
        return utilsService.getSuccessModel("", null, OK.value(), relatedObv)

    }
	
	def getTargetInstance(Class clazz, id){
		if( id instanceof String){
			id = id.trim().toLong()
	 	}
		def instance = clazz.get(id)
		
		if(!instance || (instance.hasProperty('isDeleted') && instance.isDeleted)){
			instance = new ResourceRedirect().fetchTargetInstance(clazz.canonicalName, id)
	 	}
		
		return instance
	} 

    def getObjResources(){
        def result = [:];

        String cacheKey = "${params.controller}-${params.id}"
        String cacheName = 'resources';

        result = utilsService.getFromCache(cacheName, cacheKey);
        if(!result) {
            result = [:];
            if(params.id){            
                def objInstance;
                
                result['parent'] = params.controller;
                result['parentId'] = params.id;

                if(params.controller == 'species'){
                    objInstance = Species.get(params.long('id'));
                    result['dataset'] = false
                } else if(params.controller == 'observation'){
                    objInstance = Observation.get(params.long('id'));
                    result['dataset'] = (objInstance?.dataset)?true:false;
                }
                result['resources'] = objInstance?.listResourcesByRating();
                //if(objInstance.hasProperty('group'))
                result['defaultThumb'] = objInstance?.fetchSpeciesGroup()?.icon(ImageType.ORIGINAL)?.thumbnailUrl(null, '.png', null);
            }

            if(result)
                utilsService.putInCache(cacheName, cacheKey, result);
        }


        withFormat {
            json { render result as JSON; }
            xml { render result as XML; }
        }
    }

    def getUserGroups(Class clazz, id) {
        return getTargetInstance(clazz, id).userGroups; 
    }

    def userGroups() {
        def result = [];
        switch(params.controller.toLowerCase()) {
            case 'observation' : result = getUserGroups(Observation.class, params.id);
        }
        render result as JSON;
    }
}
