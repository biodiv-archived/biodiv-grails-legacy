<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.participation.RecommendationVote"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="speciespage.ChartService"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.UtilsService"%>

<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'observation', action:'show', id:observationInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${(!observationInstance.fetchSpeciesCall()?.equalsIgnoreCase('Unknown'))?observationInstance.fetchSpeciesCall():g.message(code:'link.help.identify')}"/>

<%
def r = observationInstance.mainImage();
def imagePath = '', videoPath='';
if(r) {
    if(r.type == ResourceType.IMAGE) {
        imagePath = r.thumbnailUrl(null, !observationInstance.resource ? '.png' :null, ImageType.LARGE)
    } else if(r.type == ResourceType.VIDEO){
        imagePath = r.thumbnailUrl()
        videoPath = r.getUrl();
    }
}
    
%>
<g:set var="description" value="${Utils.stripHTML(observationInstance.summary()) }" />

<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath, 'videoPath':videoPath]"/>


<style>
.nameContainer {
    float: left;
}


.textbox input{
    text-align: left;
    width: 380px;
    padding:5px;
}

.btn .combobox-clear {
    margin-top: 3px;
}
.nameContainer.textbox #commonName {
    width: 282px;
}
.nameContainer .combobox-container {
    left: 282px;
}
 
.observation_story .observation_footer {
    margin-top:50px;
}
.commonName {
    width:282px !important;
}

</style>
</head>
<body>
            <div class="observation  span12">
                            <obv:showSubmenuTemplate/>
                            
                        <g:if test="${observationInstance}">
                        <g:set var="featureCount" value="${observationInstance.featureCount}"/>
                        <g:set var="obvLock" value="${observationInstance.isLocked}"/>
                            </g:if>
                            
                        <div class="page-header clearfix ">
                                    <div style="width:100%;">
                                        <div class="main_heading" style="margin-left:0px; position:relative">
                                            <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
                                            </span>

                                            <div class="pull-right">
                                                <g:if test="${!observationInstance.dataset}">
                                                <sUser:ifOwns model="['user':observationInstance.author]">
                                                
                                                <a class="btn btn-primary pull-right" style="margin-right: 5px;"
                                                   href="${uGroup.createLink(controller:'observation', action:'edit', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                                                    <i class="icon-edit"></i><g:message code="button.edit" /></a>

                                                <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                                                    href="#"
                                                    onclick="return deleteObservation();"><i class="icon-trash"></i><g:message code="button.delete" /></a>
                                                <form action="${uGroup.createLink(controller:'observation', action:'flagDeleted')}" method='POST' name='deleteForm'>
                                                    <input type="hidden" name="id" value="${observationInstance.id}" />
                                                </form>
 
                                                </sUser:ifOwns>
                                                </g:if>
                                            </div>
                                           <obv:showSpeciesName
                                            model="['observationInstance':observationInstance, 'isHeading':true]" />


                                        </div>
                                    </div>
                                    <div style="clear:both;"></div>
                               </div>
                               
                               <div class="span12" style="margin-left:0px">
                                   <g:render template="/common/observation/showObservationStoryActionsTemplate"
                                   model="['instance':observationInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'showDetails':true,'hideDownload':true, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress, 'userGroup':userGroupInstance, ibpClassification:observationInstance.maxVotedReco?.taxonConcept?.fetchDefaultHierarchy()]" />
                               </div>

                <div class="span8 right-shadow-box" style="margin: 0;">
                <center>
                    <div id="gallerySpinner" class="spinner">
                        <img src="${assetPath(src:'/all/spinner.gif', absolute:true)}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
                    </div>
                </center>
                <div class="galleryWrapper">
                    <g:render template="/observation/galleryTemplate" model="['instance': observationInstance]"/>
                </div>


                <obv:showStory
                model="['observationInstance':observationInstance, 'showDetails':true, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress,'userLanguage':userLanguage]" />
                <g:if test="${customFields?.size() > 0}">
                    <div style="margin-top:8px;" class="sidebar_section">
                        <h5><g:message code="heading.customfields" /></h5>
                        <obv:showCustomFields model="['observationInstance':observationInstance]"/>
                    </div>  
                </g:if>


                    <div class="recommendations sidebar_section" style="overflow:visible;clear:both;">
                        <div>
                            <ul id="recoSummary" class="pollBars recoSummary_${observationInstance.id}">

                            </ul>
                            <div id="seeMoreMessage_${observationInstance.id}" class="message"></div>
                            <div id="seeMore_${observationInstance.id}" class="btn btn-mini" onclick="preLoadRecos(-1, 3, true,${observationInstance.id});"><g:message code="button.show.all" /></div>
                        </div>
                        <div style="width:100%;">
                            <g:hasErrors bean="${recommendationInstance}">
                                <div class="errors">
                                    <g:renderErrors bean="${recommendationInstance}" as="list" />
                                </div>
                            </g:hasErrors>
                            <g:hasErrors bean="${recommendationVoteInstance}">
                                <div class="errors">
                                    <g:renderErrors bean="${recommendationVoteInstance}" as="list" />
                                </div>
                            </g:hasErrors>
                            <form id="addRecommendation" name="addRecommendation"
                                action="${uGroup.createLink(controller:'observation', action:'addRecommendationVote')}"
                                method="GET" class="form-horizontal addRecommendation showPage addRecommendation_${observationInstance.id}">
                                <div class="reco-input">
                                <reco:create
                                    model="['recommendationInstance':recommendationInstance]" />
                                    <input type="hidden" name='obvId'
                                            value="${observationInstance.id}" />
                                    
                                     <input type="submit"
                                            value="${g.message(code:'title.value.add')}" class="btn btn-primary btn-small pull-right comment-post-btn" />
                                            <div style="clear:both"></div>
                                </div>
                                
                            </form>
                            <uGroup:showUserGroupsListInModal model="['userGroupInstanceList':observationInstance.userGroups]" />
                        </div>
                        
                                            </div>
                    <g:set var="utilsService" bean="utilsService"/>
                    <g:if  test="${traitInstanceList}">
                    <div class="sidebar_section" style="margin:10px 0px;">
                        <a class="speciesFieldHeader" data-toggle="collapse" href="#traits"><h5>Traits</h5></a>
                        <div class="sidebar_section pre-scrollable" style="max-height:419px;overflow-x:hidden;">
                            <div id="traits" class="trait">
                                <g:render template="/trait/showTraitListTemplate" model="['instanceList':traitInstanceList, 'factInstance':factInstanceList, 'fromObservationShow': 'show', 'fromSpeciesShow':true, 'instance':observationInstance, displayAny:false, editable:true, 'ifOwns':utilsService.ifOwns(observationInstance.author)]"/>
                            </div>
                        </div>
                        </div>
                    </g:if>
                                                                                      
                    <uGroup:objectPostToGroupsWrapper 
                        model="['observationInstance':observationInstance, 'objectType':observationInstance.class.canonicalName]"/>

                        <%
                        def annotations = observationInstance.fetchChecklistAnnotation()
                        %>
                        <g:if test="${annotations?.size() > 0}">
                        <div class="sidebar_section" style="margin-bottom:0px;">
                            <h5><g:message code="heading.annotations" /></h5>
                            <div>
                                <obv:showAnnotation model="[annotations:annotations, height:297]" />
                            </div>
                        </div>  
                        </g:if>
                    
                        <div class="union-comment">
                    <feed:showAllActivityFeeds model="['rootHolder':observationInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable', 'userLanguage':userLanguage]" />
                    <comment:showAllComments model="['commentHolder':observationInstance, commentType:'super','showCommentList':false, 'userLanguage':userLanguage]" />
                    </div>
                </div>

                <div class="span4">
                    <obv:showLocation model="['observationInstance':observationInstance]" />

                    <div class="sidebar_section">
                        <h5><g:message code="observation.show.related.observations" /> </h5>
                        <div class="tile" style="clear: both">
                            <div class="title"><g:message code="observation.show.other.observations" /><span class="item_count"></span></div>
                            <obv:showRelatedStory
                            model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'related','filterProperty': 'speciesName', 'id':'a','userGroupInstance':userGroupInstance]" />
                        </div>
                        <div class="tile">
                            <div class="title"><g:message code="text.observations.nearby" /></div>
                            <obv:showRelatedStory
                            model="['observationInstance':observationInstance, 'observationId': observationInstance.id, 'controller':'observation', 'action':'related', 'filterProperty': 'nearByRelated', 'id':'nearBy', 'userGroupInstance':userGroupInstance]" />
                        </div>

                    </div>
                </div>
            </div>
<script type="text/javascript">
$(document).ready(function(){
    window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'observation', action:'getRecommendationVotes',userGroupWebaddress:params.webaddress) }";    
});
</script>


    <asset:script>
    
   
    var observationId = ${observationInstance.id};
    $(document).ready(function(){
        
        $('#voteCountLink').click(function() {
            $('#voteDetails').show();
        });

        $('#voteDetails').mouseout(function(){
            $('#voteDetails').hide();
        });
                        
        $("ul[name='tags']").tagit({select:true,  tagSource: "${uGroup.createLink(controller:params.controller, action: 'tags')}"});
         
        $(".view_obv_tags li.tagit-choice").click(function(){
            var tg = $(this).contents().first().text();
            window.location.href = "${uGroup.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
         });
                 
        $(".nav a.disabled").click(function() {
            return false;
        });

        preLoadRecos(3, 0, false,observationId);
        //loadObjectInGroups();
        var obvLock = ${obvLock};
        if(obvLock){
            showUpdateStatus('This species ID has been validated by a species curator and is locked!', 'success');
            $('.nameContainer input').attr("disabled", "disabled");
            $('.iAgree button').addClass("disabled");
        }
        else{
            $('.nameContainer input').removeAttr("disabled");
            $('.iAgree button').removeClass("disabled");
        } 
        initializeLanguage();
        $(".CustomField_multiselectcombo").multiselect();
        
        var getResourceUrl = "${uGroup.createLink(controller:'observation', action:'getObjResources', userGroupWebaddress:params.webaddress)}";
        galleryAjax(getResourceUrl+'/'+${observationInstance.id},'observation');
        initializeSpeciesGroupHabitatDropdowns();


        $(document).on('click', '.trait button, .trait .none, .trait .any', function(){
            if($(this).hasClass('MULTIPLE_CATEGORICAL')) {
                $(this).parent().parent().find('.all, .any, .none').removeClass('active btn-success');
                if($(this).hasClass('btn-success')) 
                    $(this).removeClass('active btn-success');
                else
                    $(this).addClass('active btn-success');
            } else if($(this).hasClass('SINGLE_CATEGORICAL')){
                if($(this).hasClass('btn-success')) {
                    $(this).removeClass('active btn-success');
                }
                else{
                    $(this).parent().parent().find('.all, .any, .none, button').removeClass('active btn-success');
                    $(this).addClass('active btn-success');
                }
            }
            return false;
        });


    });
    function deleteObservation(){
        var test="${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}";

        if(confirm(test)){
            document.forms.deleteForm.submit();
        }
    }
</asset:script>

</body>
</html>
