package species

import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import grails.util.GrailsNameUtils;
import org.grails.rateable.RatingException;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil; 

class ObservationTagLib {
	static namespace = "obv"
	
	def observationService
	def grailsApplication
	
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
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationSnippetTabletTemplate", model:attrs.model);
		}
	}

	
	def showStory = {attrs, body->
		if(attrs.model.observationInstance) {
			out << render(template:"/common/observation/showObservationStoryTemplate", model:attrs.model);
		}
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
		def emailInfoModel = observationService.getIdentificationEmailInfo(attrs.model, attrs.model.requestObject, "");
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
	
	def rating = {attrs, body->
		//out << render(template:"/common/ratingTemplate", model:attrs.model);
        println '=============================='
        println attrs
        def resource = attrs.model.resource
        boolean hideForm = attrs.model.hideForm
        int index = attrs.model.index
        if(resource) {
            def averageRating = resource.averageRating ?(int) resource.averageRating: 0
            resource = GrailsHibernateUtil.unwrapIfProxy(resource);
            println GrailsNameUtils.getPropertyName(resource.class);
            out << """
                <div class="rating pull-right">
            """

            if(!hideForm) {
                out << """<form class="ratingForm" method="get" title="Rate it"
                    action="${uGroup.createLink(controller:'rateable', action:'rate', id:resource.id, type:GrailsNameUtils.getPropertyName(resource.class)) }">
                    """
            }
            String name = index?(resource.id?'rating_'+index:'rating_{{>i}}'):'rating'
            out << """
                    <input class="star required" type="radio" name="${name}" value="1"
                    title="Worst" ${(averageRating==1)?'checked':''} /> <input class="star" type="radio" name="${name}"
                    value="2" title="Bad"  ${(averageRating==2)?'checked':''} /> <input class="star" type="radio"
                    name="${name}" value="3" title="OK"  ${(averageRating==3)?'checked':''} /> <input class="star"
                    type="radio" name="${name}" value="4" title="Good"  ${(averageRating==4)?'checked':''} /> <input
                    class="star" type="radio" name="${name}" value="5" title="Best"  ${(averageRating==5)?'checked':''} />
                    <div class="noOfRatings">(${resource.totalRatings ?: 0} ratings)</div>
                """
            if(!hideForm) {
                out << "</form>"
            } 
            out <<  "</div>"
        } else {
            throw new RatingException("There must be a 'bean' domain object included in the ratings tag.")
        }
	}
}

