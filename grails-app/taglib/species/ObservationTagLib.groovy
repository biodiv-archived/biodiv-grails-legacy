package species

import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import content.eml.Document;

import grails.util.GrailsNameUtils;
import org.grails.rateable.RatingException;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil; 
import java.lang.Math;

class ObservationTagLib {
	static namespace = "obv"
	
	def observationService
	def grailsApplication
    def springSecurityService;
    def chartService;

	def create = {attrs, body ->
		out << render(template:"/common/observation/editObservationTemplate", model:attrs.model);
	}
	
	def show = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationTemplate", model:attrs.model);
		}
	}
	
	def showSnippet = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationSnippetTemplate", model:attrs.model);
		}
	}

    def showSnippetTablet = {attrs, body->
		out << render(template:"/common/observation/showObservationSnippetTabletTemplate", model:attrs.model);
	}
	
	def showStory = { attrs, body ->
		out << render(template:"/common/observation/showObservationStoryTemplate", model:attrs.model);
	}
	
	def addFlag= {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/addFlagTemplate", model:attrs.model);
		}
	}
	
	def showStoryTablet = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationStoryTabletTemplate", model:attrs.model);
		}
	}

	def showObservationInfo = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationInfoTemplate", model:attrs.model);
		}
	}

	def showRelatedStory = {attrs, body->
        //println attrs.model;
			out << render(template:"/common/observation/showObservationRelatedStoryTemplate", model:attrs.model);
	}
	
	def showGroupFilter = {attrs, body->
			out << render(template:"/common/speciesGroupFilterTemplate", model:attrs.model);
	}
	
	def showRating = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationRatingTemplate", model:attrs.model);
		}
	}

	def showLocation = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationLocationTemplate", model:attrs.model);
		}
	}
	def showTagsSummary = {attrs, body->
		if(attrs.model.observationInstance) {
			def tags = observationService.getRelatedTagsFromObservation(attrs.model.observationInstance)
			out << render(template:"/common/observation/showTagsSummaryTemplate", model:[tags:tags]);
		}
	}
	
//	def showTags = {attrs, body->
//		if(attrs.model.observationInstance) {
//			out << render(template:"/common/observation/showObservationTagsTemplate", model:attrs.model);
//		}
//	}

	def showObvStats = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationStatsTemplate", model:attrs.model);
		}
	}
	
	// this will call showTagsList and showTagsCloud
	def showAllTags = {attrs, body->
		def count = attrs.model.count;
		def tags = attrs.model.tags
		if(tags == null) {
			def tagFilterBy = attrs.model.tagFilterByProperty
			
			
			if(tagFilterBy == "Related"){
				def relatedParams = attrs.model.relatedObvParams
				tags = observationService.getAllRelatedObvTags(relatedParams)
				count = tags.size()
			}
			else if(tagFilterBy == "User"){
				def userId = attrs.model.tagFilterByPropertyValue.toLong();
				tags = observationService.getAllTagsOfUser(userId)
				count = tags.size()
			}
			else {
				attrs.model.params.remove('sort')
				tags =  observationService.getFilteredTags(attrs.model.params);
				count = tags.size();
			} 
		}
		//log.debug "==== tags " + tags 
		out << render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:tags, isAjaxLoad:attrs.model.isAjaxLoad]);
	}
		
	def showTagsList = {attrs, body->
		out << render(template:"/common/observation/showTagsListTemplate", model:attrs.model);
	}
	
	
	def showTagsCloud = {attrs, body->
		out << render(template:"/common/observation/showTagsCloudTemplate", model:attrs.model);
	}
	
	
	def showGroupList = {attrs, body->
		out << render(template:"/common/observation/showGroupListTemplate", model:attrs.model);
	}
	
	def showObservationsLocation = {attrs, body->
		out << render(template:"/common/observation/showObservationMultipleLocationTemplate", model:attrs.model);
	}
	
	def showObservationFilterMessage = {attrs, body->
		out << render(template:"/common/observation/showObservationFilterMsgTemplate", model:attrs.model);
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////  Tag List added by specific User ///////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////
	
	def showNoOfTagsOfUser = {attrs, body->
		def tags = observationService.getAllTagsOfUser(attrs.model.userId.toLong());		
		out << tags.size() 
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////  Date related ///////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	def showDate = {attrs, body->
		out << render(template:"/common/observation/showDateTemplate",model:attrs.model);
	}
	
	def showSpeciesName = {attrs, body->
		out << render(template:"/common/observation/showSpeciesNameTemplate",model:attrs.model);
        }
        
    def showObservationsList = {attrs, body->
		out << render(template:"/common/observation/showObservationListTemplate", model:attrs.model);
	}

	def showObservationsListWrapper = {attrs, body->
		out << render(template:"/common/observation/showObservationListWrapperTemplate", model:attrs.model);
	}
	
	def showNoOfObservationsOfUser = {attrs, body->
		def noOfObvs = observationService.getAllObservationsOfUser(attrs.model.user);
		out << noOfObvs
	}
	
	def showNoOfRecommendationsOfUser = {attrs, body->
		def noOfObvs = observationService.getAllRecommendationsOfUser(attrs.model.user);
		out << noOfObvs
	}
	
	def identificationByEmail = {attrs, body->
		def emailInfoModel = observationService.getIdentificationEmailInfo(attrs.model, attrs.model.requestObject, "", params.controller, params.action);
		attrs.model.each { key, value ->
			emailInfoModel[key] = value;
		}
		out << render(template:"/common/observation/identificationByEmailTemplate",model:emailInfoModel);
	}
	
	def showRecoComment = {	attrs, body->
		out << render(template:"/common/observation/showRecoCommentTemplate",model:attrs.model);
	}
	
	def showFooter = {attrs, body->
		out << render(template:"/common/observation/showObservationStoryFooterTemplate", model:attrs.model);
	}
	
	def showSubmenuTemplate = {attrs, body->
		out << render(template:"/observation/observationSubmenuTemplate", model:attrs.model);
	}
	
	def download = {attrs, body->
		out << render(template:"/common/downloadTemplate", model:attrs.model);
	}
	
	def downloadTable = {attrs, body->
		out << render(template:"/common/downloadTableTemplate", model:attrs.model);
	}
	
	def showMapInput = {attrs, body->
		def model = attrs.model
		model.sourceInstance = model.sourceInstance ?: model.observationInstance
		model.placeNameField = (model.sourceInstance.class.getCanonicalName() == Document.class.getCanonicalName()) ? 'coverage.placeName' : 'placeName'
		model.topologyNameField = (model.sourceInstance.class.getCanonicalName() == Document.class.getCanonicalName()) ? 'coverage.topology' : 'topology'
		out << render(template:"/common/observation/showMapInputTemplate",model:attrs.model);
	}
	
	def showAnnotation = {attrs, body->
		out << render(template:"/common/observation/showAnnotationTemplate", model:attrs.model);
	}
	
	def rating = {attrs, body->
		//out << render(template:"/common/ratingTemplate", model:attrs.model);
        def resource = attrs.model.resource
        boolean hideForm = attrs.model.hideForm
        int index = attrs.model.index
        String divClass = attrs.model.class?:'rating'
        if(resource) {
            resource = GrailsHibernateUtil.unwrapIfProxy(resource);
            long averageRating = resource.averageRating ?: 0
            out << """
                <div class="pull-right">
            """

            if(!hideForm) {
                out << """<form class="ratingForm" method="get" title="Rate it">
                    """
            }
            String name = index?(resource.id?'rating_'+index:'rating_{{>i}}'):'rating'

            out << """
               <span class="star_${divClass} 
                    title="Rate" data-score='${averageRating}' data-input-name="${name}"  data-id="${resource.id}" data-type="${GrailsNameUtils.getPropertyName(resource.class)}" data-action="like" ></span>
                    <div class="noOfRatings">(${resource.totalRatings ?: 0} rating${resource.totalRatings!=1?'s':''})</div>
                """
            if(!hideForm) {
                out << "</form>"
            } 
            out <<  "</div>"
        } else {
            throw new RatingException("There must be a 'bean' domain object included in the ratings tag.")
        }
	}

    def like = {attrs, body->
        def resource = attrs.model.resource
        String divClass = attrs.model.class?:'rating'
        boolean hideForm = attrs.model.hideForm
        if(resource) {
            resource = GrailsHibernateUtil.unwrapIfProxy(resource);
            int userRating = springSecurityService.currentUser?((resource.userRating(springSecurityService.currentUser).size()==1)?1:0):0;
            
            out << """
                <div class="pull-right">
            """

            if(!hideForm) {
                out << """<form class="ratingForm" method="get" title="Rate it">
                    """
            }
            out << """
                <span class="like_${divClass} 
                    title="${(userRating>0)?'Unlike':'Like'}" ${(userRating==1)?"data-score='1'":""} data-id="${resource.id}" data-type="${GrailsNameUtils.getPropertyName(resource.class)}" data-action="${(userRating>0)?'unlike':'like'}"></span>
                    <span class="noOfRatings" title='No of likes'>${resource.totalRatings ?: 0}</span>
                """
            if(!hideForm) {
                out << "</form>"
            } 
            out <<  "</div>"
 
        } else {
            throw new RatingException("There must be a 'bean' domain object included in the ratings tag.")
        }
	}

    def getStats = { attrs, body ->
        def noOfTags = observationService.getAllTagsOfUser(attrs.model.user.id.toLong()).size()	
        def noOfObvs = observationService.getAllObservationsOfUser(attrs.model.user);
		def noOfUserRecos = observationService.getAllRecommendationsOfUser(attrs.model.user);
        def noOfComments = attrs.model.user.fetchCommentCount()
        int totalActivity = chartService.getUserActivityCount(attrs.model.user);
        out << """

        <div class="footer">
            <span class="footer-item" title="Observations"> <i
            class="icon-screenshot"></i> ${noOfObvs}
            </span> 

            <span class="footer-item"
            title="Tags"> <i class="icon-tags"></i> 
            ${noOfTags}</span> 

            <span class="footer-item"
            title="Identifications"> <i class="icon-check"></i> 
            ${noOfUserRecos} </span>

            <span class="footer-item" title="Comments"> <i class="icon-comment"></i> 
            ${noOfComments}
        </div>

        """
        
    }

    def featured = { attrs, body ->
        if(attrs.model) {
            def p = [limit:1, offset:0, filterProperty:'featureBy', controller:attrs.model.controller, userGroup:attrs.model.userGroupInstance]
            def related = observationService.getRelatedObservations(p)?.relatedObv
            if(related) {
                attrs.model['relatedInstanceList'] = related.observations;
                attrs.model['relatedInstanceListTotal'] = related.count;
            }
        }
           out << obv.showRelatedStory(attrs, body);
    }

    def addPhotoWrapper = { attrs, body ->
        println "called called==================="
        def resList = []
        def obvLinkList = []
        def resCount = 0
        def offset = 0 
        def resInstance = attrs.model.observationInstance
        switch (attrs.model.resourceListType) {
            case "ofObv" :
                println "RES FOR OBSER==============="
                resList = resInstance.resource
            break
            
            case "ofSpecies" :
                println " RES FOR SPECIES==================="
                resList = resInstance.resources
            break
            
            case "fromRelatedObv" :
                println "RES FROM RELATED OBV=============="
                def taxId = resInstance.taxonConcept.id.toLong()
                // new func service limit offset sp inst and returns a res list based on params
                def relObvMap =  observationService.getRelatedObvForSpecies(resInstance, 1, 0)
                resList = relObvMap.resList
                resCount = relObvMap.count
                obvLinkList = relObvMap.obvLinkList
            break

        }
        attrs.model['resList'] = resList
        attrs.model['offset'] = offset
        attrs.model['resCount'] = resCount
        attrs.model['obvLinkList'] = obvLinkList
        println "========================" + attrs.model
        out << render(template:"/observation/addPhotoWrapper", model:attrs.model);
    }
}

