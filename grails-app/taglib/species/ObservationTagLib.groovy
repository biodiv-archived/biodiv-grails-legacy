package species

import species.participation.Checklists
import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import content.eml.Document;

import grails.util.GrailsNameUtils;
import org.grails.rateable.RatingException;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil; 
import java.lang.Math;
import species.participation.UsersResource;
import species.participation.DownloadLog;
import species.participation.Comment;
import species.participation.Discussion;
import species.participation.Flag;
import species.participation.Featured;
import species.participation.ActivityFeed;


class ObservationTagLib {
	static namespace = "obv"
	
	def observationService
	def grailsApplication
    def springSecurityService;
    def chartService;
	def customFieldService;
    //def SUserService;
    def utilsService;

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
        out << render(template:"/common/observation/showObservationRelatedStoryTemplate", model:attrs.model);

	}
	
	def showGroupFilter = {attrs, body->
			out << render(template:"/common/speciesGroupFilterTemplate", model:attrs.model);
	}
	def showGroupIdentifiedFilter = {attrs, body->
			out << render(template:"/common/speciesGroupFilterIdentifiedTemplate", model:attrs.model);
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
        def noOfObvs = observationService.getAllObservationsOfUser(attrs.model.user, attrs.model.userGroup);
		out << noOfObvs
	}
		
	
	def showNoOfRecommendationsOfUser = {attrs, body->
        def noOfObvs = observationService.getAllRecommendationsOfUser(attrs.model.user, attrs.model.userGroup);
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
		model.placeNameField = (model.sourceInstance && model.sourceInstance.class.getCanonicalName() == Document.class.getCanonicalName()) ? 'coverage.placeName' : 'placeName'
		model.topologyNameField = (model.sourceInstance && model.sourceInstance.class.getCanonicalName() == Document.class.getCanonicalName()) ? 'coverage.topology' : 'topology'
		
		def obj = model.sourceInstance
		String sourceType
		if(obj?.instanceOf(Checklists) || obj?.instanceOf(Document)){
			sourceType = 'checklist'
		}else if(obj?.instanceOf(Observation) && (obj?.id != obj.sourceId)){ 
			sourceType = 'checklist-obv'
		}else
			sourceType = 'observation'
		 
		model.sourceType = sourceType
        out << render(template:"/common/observation/showMapInputTemplate",model:attrs.model);
	}
	
	def showAnnotation = {attrs, body->
		out << render(template:"/common/observation/showAnnotationTemplate", model:attrs.model);
	}
	
	def showCustomFields = {attrs, body->
        if(attrs.model.customFields == null)
    		attrs.model.customFields = customFieldService.fetchAllCustomFields(attrs.model.observationInstance);
		out << render(template:"/observation/showCustomFieldsTemplate", model:attrs.model);
	}

	def rating = {attrs, body->
		//out << render(template:"/common/ratingTemplate", model:attrs.model);
        def resource = attrs.model.resource
        boolean hideForm = attrs.model.hideForm?:false
        int index = attrs.model.index?:0
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
               <span class="star_${divClass}" 
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
                out << """<form class="ratingForm" method="get" title="${g.message(code:'observationtaglib.title.rate')}">
                    """
            }
            out << """
                <span class="like_${divClass}" 
                    title="${(userRating>0)?g.message(code:'default.unlike'):g.message(code:'default.like')}" ${(userRating==1)?"data-score='1'":""} data-id="${resource.id}" data-type="${GrailsNameUtils.getPropertyName(resource.class)}" data-action="${(userRating>0)?'unlike':'like'}"></span>
                    <span class="noOfRatings" title='${g.message(code:"observationtaglib.title.likes")}'>${resource.totalRatings ?: 0}</span>
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
        def noOfObvs = observationService.getAllObservationsOfUser(attrs.model.user, attrs.model.userGroup);
		def noOfUserRecos = observationService.getAllRecommendationsOfUser(attrs.model.user, attrs.model.userGroup);
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
            def related;
            related = observationService.getRelatedObservations(p)?.relatedObv
            if(related) {
                attrs.model['relatedInstanceList'] = related.observations;
                attrs.model['relatedInstanceListTotal'] = related.count;
            }
        }
           out << obv.showRelatedStory(attrs, body);
    }

    def addPhotoWrapper = { attrs, body ->
        def resList = []
        def obvLinkList = []
        def resCount = 0
        def offset = 0 
        def resInstance = attrs.model.observationInstance
        def userInstance = attrs.model.userInstance
        switch (attrs.model.resourceListType) {
            case "ofObv" :
                resList = resInstance?.resource
            break
            
            case "ofSpecies" :
                resList = resInstance?.resources
            break
            
            case "fromRelatedObv" :
                def taxId = resInstance?.taxonConcept.id.toLong()
                // new func service limit offset sp inst and returns a res list based on params
                boolean includeExternalUrls = false;
                def relObvMap =  observationService.getRelatedObvForSpecies(resInstance, 4, 0, includeExternalUrls)
                List relatedObv = [];
                /*int ix=0;
                relObvMap.resList.eachWithIndex { it, index ->
                    if(it.url && (it.url.endsWith('no-image.jpg') ||  it.fileName.length() == 1)) {
                        log.debug "Ignoring resource from pulling as it as external Url ${it.url}"
                    } else {
                        relatedObv << it
                        obvLinkList[ix++] = relObvMap.obvLinkList[index];
                    }
                }*/

                relatedObv = relObvMap.resList;
                obvLinkList = relObvMap.obvLinkList;
                resList = relatedObv
                resCount = relObvMap.count
                offset = resCount
            break

            case "fromSpeciesField" :
                if(attrs.model.spFieldId == ""){
                } else {
                    def allSpField = resInstance?.fields
                    allSpField.each{
                        def r = it.resources
                        r.each{
                            resList.add(it)
                        }
                    }
                }
                resCount = resList.size()
            break
            case "fromSingleSpeciesField" :
                
            break

            case "usersResource" :
                def usersResList
                /*if(SUserService.isAdmin(userInstance?.id)){
                    usersResList = UsersResource.findAllByStatus(UsersResource.UsersResourceStatus.NOT_USED.toString())
                } else {*/
                    usersResList = UsersResource.findAllByUserAndStatus(userInstance, UsersResource.UsersResourceStatus.NOT_USED.toString() ,[sort:'id', order:'desc'])
                //}
                usersResList.each{
                    resList.add(it.res)
                }
                resCount = resList.size()
            break
    
        }
        if(springSecurityService.currentUser) {
            attrs.model['currentUser'] = springSecurityService.currentUser
        }
        attrs.model['resList'] = resList
        attrs.model['offset'] = offset
        attrs.model['resCount'] = resCount
        attrs.model['obvLinkList'] = obvLinkList
        out << render(template:"/observation/addPhotoWrapper", model:attrs.model);
    }

    def showNoOfBulkUploadResOfUser = { attrs, body ->
        def res = UsersResource.findAllByUserAndStatus(attrs.model.user, UsersResource.UsersResourceStatus.NOT_USED.toString() ,[sort:'id', order:'desc'])
        out << res.size()
    }

    def showBulkUploadRes = { attrs, body ->
        def res = UsersResource.findAllByUserAndStatus(attrs.model.user, UsersResource.UsersResourceStatus.NOT_USED.toString() ,[sort:'id', order:'desc'])
        if(res.size() > 0){
            out << body()
        }
    }
    def showNoOfObservationsCreated = {attrs, body->
        def noOfObvs = observationService.getAllObservationsOfUser(attrs.model.user, attrs.model.userGroup);
		out << "<td class=countvaluecontributed>"+noOfObvs+"</td>"
	}
    def showNoOfSuggestedUponOfUser={attrs, body->
        def noOfObvs = observationService.getAllSuggestedRecommendationsOfUser(attrs.model.user, attrs.model.userGroup);
		out << "<td class=countvalue>"+noOfObvs+"</td>"
	}
	    def showNoOfDownloadUponOfUser={attrs, body->
        def noOfObvs = DownloadLog.findAllByAuthorAndSourceType(attrs.model.user, attrs.model.sourceType).size();
			out << "<td class=countvalue>"+noOfObvs+"</td>"
	}
		def showNoOfCommentUponOfUser={attrs, body->
        def noOfObvs = Comment.findAllByAuthorAndRootHolderType(attrs.model.user, attrs.model.rootHolderType).size();
		out <<  "<td class=countvalue>"+noOfObvs+"</td>"
	}
		def showNoOfOrganizedUponOfUser={attrs, body->
		String[] activityType=["obv locked","obv unlocked","Featured","UnFeatured","Flagged","Flag removed","Flag deleted"];
		def totalOrganizedObv=0;
		activityType.each{
		def noOfOrganizedOvb=ActivityFeed.findAllByAuthorAndActivityType(attrs.model.user,it).size()
		totalOrganizedObv=noOfOrganizedOvb+totalOrganizedObv
		}
		out << "<td class=countvalue>"+totalOrganizedObv+"</td>"

	}
	def showNoOfDiscussionCreated={attrs,body->
		def noOfDiscussionCreated=Discussion.findAllByAuthor(attrs.model.user).size()
		out << "<td class=countvaluecontributed>"+noOfDiscussionCreated+"</td>"

	}
	def showNoOfDocsUploaded={attrs,body->
		def noOfDocsUploaded=Document.findAllByAuthor(attrs.model.user).size()
		out << "<td class=countvaluecontributed>"+noOfDocsUploaded+"</td>"

	}
	def showNoofOrganizedDocs={attrs,body->
		String[] activityType=["Featured","UnFeatured","Posted resource","Document updated","Flagged"]
		def totalDocsOrganized=0;
		activityType.each{
		def noOfDocsOraganized=ActivityFeed.findAllByAuthorAndRootHolderTypeAndActivityType(attrs.model.user,attrs.model.rootHolderType,it)
		totalDocsOrganized=noOfDocsOraganized.size()+totalDocsOrganized
		
	}
	out << "<td class=countvalue>"+totalDocsOrganized+"</td>";
	}
	//def showNoofCommentedDocs={attrs,body->
	//	def noOfDocsCommented=ActivityFeed.findAllByAuthorAndRootHolderTypeAndActivityType(attrs.model.user,attrs.model.rootHolderType,attrs.model.activityType).size()
	//	out << "<td class=countvalue>"+noOfDocsCommented+"</td>"
	//}
		def showNoofOrganizedDiscussion={attrs,body->
		String[] activityType=["Posted resource","Featured","Flagged"]
		def totalDiscussionOrganized=0;
		activityType.each{ 
		def noOfDiscussionOraganized=ActivityFeed.findAllByAuthorAndRootHolderTypeAndActivityType(attrs.model.user,attrs.model.rootHolderType,it)
		totalDiscussionOrganized=noOfDiscussionOraganized.size()+totalDiscussionOrganized
		
	}
	out << "<td class=countvalue>"+totalDiscussionOrganized+"</td>";
	}
		def showNoofParticipationDiscussion={attrs,body->
		def noOfParticipationDiscussion=ActivityFeed.findAllByAuthorAndRootHolderTypeAndActivityType(attrs.model.user,attrs.model.rootHolderType,attrs.model.activityType).size()
		out << "<td class=countvalue>"+noOfParticipationDiscussion+"</td>"
	}
		def showNoOfAgreedUponOfUser = {attrs, body->
        def noOfObvsSuggested = ActivityFeed.findAllByAuthorAndActivityTypeAndActivityHolderType(attrs.model.user,attrs.model.activityType,attrs.model.activityHolderType).size()
		out << "<td class=countvalue>"+noOfObvsSuggested+"</td>"
	}
			def showNoOfRecommendationsSuggested = {attrs, body->
        def noOfObvs = observationService.getAllRecommendationsOfUser(attrs.model.user, attrs.model.userGroup);
		out << "<td class=countvalue>"+noOfObvs+"</td>"
	}
}

