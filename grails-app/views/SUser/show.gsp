<%@page import="species.participation.Observation"%>
<%@ page import="species.auth.SUser"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.participation.DownloadLog"%>
<%@page import="species.participation.ActivityFeedService"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.participation.SpeciesBulkUpload"%>
<html>
<head>

<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'user', action:'show', id:user.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${user.name}"/>
<%def imagePath = user.profilePicture();%>
<g:set var="description" value="${Utils.stripHTML(user.aboutMe)?:'' }" />

<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath]"/>


<r:require modules="observations_show,chart" />
<gvisualization:apiImport />
<g:set var="entityName"
	value="${message(code: 'SUser.label', default: 'SUser')}" />

<style>
.prop .name {
	clear: both;
}
.super-section  {
    background-color:white;
}
.thumbnail .observation_story {
    width: 715px;
}
</style>
</head>
<body>
	<div class="span12">
		<div class="page-header clearfix">
			<div style="width: 100%;">
				<div class="span8 main_heading" style="margin-left: 0px;">
					<h1>
						${fieldValue(bean: user, field: "name")}
					</h1>
				</div>

				<div style="float: right; margin: 10px 0;">
					<sUser:ifOwns model="['user':user]">

						<a class="btn btn-info pull-right"
							href="${uGroup.createLink(action:'edit', controller:'user', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i
                                                            class="icon-edit"></i><g:message code="button.edit.profile" /> </a>
                                                    <a class="btn btn-info" style="float: right; margin-right: 5px;"href="${uGroup.createLink(action:'myuploads', controller:'user', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><iclass="icon-edit"></i><g:message code="button.my.uploads" /> </a>

					</sUser:ifOwns>
				</div>
			</div>
		</div>
		<div style="clear: both;"></div>


		<%--				<obv:identificationByEmail model="['source':'userProfileShow', 'requestObject':request]" />--%>
		<div>
			<div class="row section" style="">
				<div class="figure span3"
					style="float: left; max-height: 220px; max-width: 220px; font-size: 75%;">
					<a
						href="${uGroup.createLink(action:'show', controller:'user', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
						<img class="normal_profile_pic" src="${user.profilePicture()}" /> </a>
					<div class="prop">
						<span class="name"><i class="icon-time"></i><g:message code="default.member.since.label" /> </span>
						<div class="value">
							<g:formatDate format="dd/MM/yyyy" date="${user.dateCreated}"
								type="datetime" style="MEDIUM" />
						</div>
					</div>
					<g:if test="${user.lastLoginDate}">
						<div class="prop">
							<span class="name"><i class="icon-time"></i><g:message code="default.last.visited.label" /></span>
							<div class="value">
								
									<g:formatDate format="dd/MM/yyyy" date="${user.lastLoginDate}"
										type="datetime" style="MEDIUM" />
							
							</div>
						</div>
					</g:if>
				</div>
                                <div style="width:660px;float:left;">
                                    <sUser:showUserStory model="['userInstance':user, 'showDetails':true]"></sUser:showUserStory>
                                </div>
				
			</div>
			<%
				def downloadLogList = DownloadLog.findAllByAuthorAndStatus(user, 'Success', [sort: 'createdOn', order: 'asc'])
				def speciesBulkUploadList = SpeciesBulkUpload.findAllByAuthor(user, [sort: 'startDate', order: 'asc'])
			%>

                        <div id="userprofilenavbar" class="navbar">
                            <!--data-spy="affix affix-top" data-offset-top="10px" style="z-index:10000"-->
                            <div class="navbar-inner">
                                <ul class="nav">

                                    <li><a href="#aboutMe"><i class="icon-user"></i><g:message code="default.about.me.label" /></a></li>
                                    <li class="divider-vertical"></li>
                                    <li class="dropdown">
                                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                                <i class="icon-book"></i> <g:message code="default.content.label" />
                                                <b class="caret"></b>
                                            </a>
                                            <ul class="dropdown-menu">
                                                <li><a href="#observations"><i class="icon-screenshot"></i><g:message code="default.observation.label" /></a></li>
                                                <li><a href="#identifications"><i class="icon-eye-open"></i><g:message code="suser.show.identifications" /></a></li>
                                                <li><a href="#downloads"><i class="icon-download"></i><g:message code="suser.show.Downloads" /></a></li>
                                            </ul>
                                    </li>
                                    <li class="divider-vertical"></li>
                                    <li><a href="#groups"><i class="icon-group"></i><g:message code="default.groups.label" /></a></li>
                                    <li class="divider-vertical"></li>
                                    <li><a href="#activity"><i class="icon-tasks"></i><g:message code="button.activity" /></a></li>
                                    <g:if test="${user.hideEmailId}">
                                        <li style="padding:5px 0px">
                                        <% String staticMessage = '';
                                        if(currentUser) {
                                            staticMessage = g.message(code:'suser.message')+' <a href="'+currentUserProfile+'">'+currentUser.name+'</a>'
                                        }
def contact_me_text=g.message(code:'button.contact.me')                                     
%>

                            			    <obv:identificationByEmail
                                            model="['source':params.controller+params.action.capitalize(), 'requestObject':request, 'cssClass':'btn btn-mini', hideTo:true, title:contact_me_text, titleTooltip:'', mailSubject:'', staticMessage:staticMessage,  users:[user]]" />


                                        </li>
                                    </g:if>
                                </ul>
                            </div>
                        </div>
                        <div class="container">
                            <div id="aboutMe" class="super-section" style="overflow:hidden;padding-bottom:10px;">
                                <h5>
                                    <i class="icon-user"></i><g:message code="default.about.me.label" />
                			<sUser:ifOwns model="['user':user]">

						<a class="btn btn-link"
							href="${uGroup.createLink(action:'edit', controller:'user', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i
							class="icon-edit"></i><g:message code="button.edit" /> </a>
					</sUser:ifOwns>
                                </h5>
                                <div class="section" style="clear:both;margin-left:20px;">
                                    <g:if test="${user.aboutMe}">

                                    <%  def styleVar = 'block';
                                        def clickcontentVar = '' 
                                    %> 
                                    <g:if test="${user?.language?.id != userLanguage?.id}">
                                        <%  
                                          styleVar = "none"
                                          clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+user?.language?.threeLetterCode.toUpperCase()+'</a>';
                                        %>
                                    </g:if>

                                    ${raw(clickcontentVar)}
                                    <div style="display:${styleVar}">
                                        ${raw(user.aboutMe.replace('\n', '<br/>\n'))}
                                    </div>
                                        
                                    </g:if>
                                </div>

                                <div class="section" style="clear:both;margin-left:20px;">
                                    <h6>
                                        <g:message code="suser.show.intrested.species" /> &amp; <g:message code="default.habitats.label" />
                                    </h6>
                                    <sUser:interestedSpeciesGroups model="['userInstance':user]" />
                                    <div style="padding:3px;"></div>
                                    <sUser:interestedHabitats model="['userInstance':user]" />
                                </div>
                               <div id="observations_list_map" class="section observation span6"
                                    style="margin:0px;margin-left:20px;">
                                    <h6>
                                         <g:message code="suser.show.observations.spread" />
                                    </h6>
                                    <obv:showObservationsLocation
                                    model="['observationInstanceList':totalObservationInstanceList, 'ignoreMouseOutListener':true, width:460, height:400]">
                                    </obv:showObservationsLocation>
                                    <a id="resetMap" data-toggle="dropdown"
                                        href="#"><i class="icon-refresh"></i>
                                        <g:message code="button.reset" /></a>

                                    <div><i class="icon-info"></i><g:message code="map.limit.info" /></div>
                                    <input id="bounds" name="bounds" value="" type="hidden"/>
                                    <input id="user" name="user" value="${user.id}" type="hidden"/>
                                </div>
                                <div id="expertice" class="section span6" style="margin:0px;margin-left:20px;width:420px;">
                                <%
                                def species_group_observations="${g.message(code:'suser.heading.species.observations')}"
                                %>
                                
                                <chart:showStats model="['title':species_group_observations, columns:obvData.columns, data:obvData.data, htmlData:obvData.htmlData, htmlColumns:obvData.htmlColumns, width:420, height:420, 'hideTable':true]"/>
	                        </div>
 
                            </div>

                            <div id="content" class="super-section" style="clear: both;">
                                <h5>
                                    <span class="name" style="color: #b1b1b1;"> <i
                                            class="icon-book"></i>
                                    </span> <g:message code="default.content.label" />
                                </h5>
                                <div id="observations" class="section" style="clear:both;margin-left:20px;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"> 
                                            <obv:showNoOfObservationsOfUser
                                            model="['user':user, 'userGroup':userGroupInstance]" /> </span> <g:message code="default.observation.label" />
                 			<sUser:ifOwns model="['user':user]">
						<a class="btn btn-link"
							href="${uGroup.createLink(action:'create', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-plus"></i><g:message code="link.add.observation" /></a>
					</sUser:ifOwns>

                                    </h6>
                                    
                                    <obv:showRelatedStory
                                    model="['controller':'observation', 'action':'related', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'user', 'userGroupInstance':userGroupInstance]" />
                                </div>
                                <div id="identifications" class="section" style="clear:both;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"> 
                                            <obv:showNoOfRecommendationsOfUser model="['user':user, 'userGroup':userGroupInstance]" /> </span>
                                       <g:message code="suser.show.identifications" /> 
                 			<sUser:ifOwns model="['user':user]">
						<a class="btn btn-link"
							href="${uGroup.createLink(action:'list', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-list"></i><g:message code="heading.browse.observations" /> </a>
					</sUser:ifOwns>

                                    </h6>
                                    <obv:showRelatedStory
                                    model="['controller':'user', 'resultController':'observation', 'action':'getRecommendationVotes', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'userIds', 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'hideShowAll':true]" />

                                </div>
                                
                                <obv:showBulkUploadRes model="['user':user]">
                                 <div id="bulkUploadResources" class="section" style="clear:both;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"> 
                                            <obv:showNoOfBulkUploadResOfUser model="['user':user]" /> </span>
                                        Bulk Upload Resources 
                                    </h6>
                                    <obv:showRelatedStory
                                    model="['controller':'observation', 'action':'related', 'filterProperty': 'bulkUploadResources', 'filterPropertyValue':user.id, 'id':'bulkUploadResources', 'userGroupInstance':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'hideShowAll':true]" />

                                </div>
                                </obv:showBulkUploadRes>

                                <g:if test="${!downloadLogList.isEmpty()}">
                                <div id="downloads" class="section" style="clear: both;overflow:auto;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"></span> <g:message code="suser.show.Downloads" />
                                    </h6>
                                    <obv:downloadTable model="[downloadLogList:downloadLogList]" />
                                </div>
                                </g:if>
                                
                                <g:if test="${!speciesBulkUploadList.isEmpty()}">
                                <div id="uploads" class="section" style="clear: both;overflow:auto;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"></span> <g:message code="suser.show.species.bulk.uploads" />
                                    </h6>
                                    <s:rollBackTable model="[uploadList:speciesBulkUploadList]" />
                                </div>
                                </g:if>
                                
                            </div>
			<div id="groups" class="super-section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
						class="icon-group"></i></span> <g:message code="default.groups.label" />
                 			<sUser:ifOwns model="['user':user]">
						<a class="btn btn-link"
							href="${uGroup.createLink(action:'list', controller:'userGroup', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-plus"></i><g:message code="button.join.groups" /></a>
					</sUser:ifOwns>

				</h5>
				<uGroup:showUserUserGroups model="['userInstance':user]"></uGroup:showUserUserGroups>

			</div>
			<%--			</div>--%>
			<div id="activity" class="super-section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
                                                class="icon-tasks"></i> </span><g:message code="button.activity" />
                                        </h5>
                                        <feed:showAllActivityFeeds model="['user':user?.id,'userGroup':userGroupInstance, feedType:ActivityFeedService.USER, 'feedPermission':false, 'feedOrder':ActivityFeedService.LATEST_FIRST]" />
                        </div>

                        </div>
                    
		</div>
	</div>



	<r:script>
	var userRecoffset = 0;
        $(document).ready(function() {
            updateMapView({'user':'${user?.id}'});
            $("#seeMoreMessage").hide();
            $('#tc_tagcloud a').click(function(){
			    var tg = $(this).contents().first().text();
			    window.location.href = "${uGroup.createLink(controller:'observation', action: 'list', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}?tag=" + tg ;
	    	    return false;
	 	    });
            var max = 3;
	        $("#seeMore").click(function(){
                preLoadRecos(max, userRecoffset, true);
                userRecoffset = max + userRecoffset;
            });

            preLoadRecos(max, userRecoffset, false);
            userRecoffset = max + userRecoffset;
            $('.linktext').linkify();
            //$('#userprofilenavbar').affix();
        
            $("#resetMap").click(function() {
                var mapLocationPicker = $('#big_map_canvas').data('maplocationpicker');
                //refreshList(mapLocationPicker.getSelectedBounds());
                $("#bounds").val('');
                refreshMapBounds(mapLocationPicker);
            });


	});
</r:script>
<script type="text/javascript">
$(document).ready(function(){
    window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'user', action:'getRecommendationVotes', id:user.id, userGroupWebaddress:params.webaddress) }";
    window.params.observation.listUrl = "${uGroup.createLink(controller:'observation', action: 'listJSON')}"
});
</script>
</body>

</html>
