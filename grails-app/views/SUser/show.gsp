<%@page import="species.participation.Observation"%>
<%@ page import="species.auth.SUser"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.participation.DownloadLog"%>
<%@page import="species.participation.ActivityFeedService"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.Resource.ResourceType"%>
<html>
<head>

<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'SUser', action:'show', id:user.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${user.name}"/>
<%
def r = user.mainImage();
def imagePath = '';
if(r) {
    def gThumbnail = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)?:null;
    if(r && gThumbnail) {
            if(r.type == ResourceType.IMAGE) {
                    imagePath = gThumbnail
            }
    }
} else{
    imagePath = Utils.getIBPServerDomain()+'/sites/all/themes/ibp/images/map-logo.gif';
}

%>
<g:set var="description" value="${Utils.stripHTML(user.aboutMe)?:'' }" />

<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath]"/>
<title>${title} | User | ${Utils.getDomainName(request)}</title>


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

.section h6, expertice h6 {
    border-bottom: 1px solid #CDCDCD;
    margin-bottom: 5px;
    padding-botton:3px;
}
.map_wrapper {
padding: 0px;
border-radius: 0px;
border-top-left-radius: 0;
margin-bottom: 0px;
}

h6 .btn-link, h5 .btn-link {
    font-size:10px;
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
							href="${uGroup.createLink(action:'edit', controller:'SUser', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i
							class="icon-edit"></i>Edit Profile </a>
					</sUser:ifOwns>
				</div>
			</div>
		</div>
		<div style="clear: both;"></div>


		<%--				<obv:identificationByEmail model="['source':'userProfileShow', 'requestObject':request]" />--%>
		<div>
			<div class="row section" style="">
				<div class="figure span3"
					style="float: left; max-height: 220px; max-width: 220px">
					<a
						href="${uGroup.createLink(action:"show", controller:"SUser", id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
						<img class="normal_profile_pic" src="${user.icon()}" /> </a>

					<%--						<div class="prop">--%>
					<%--							<span class="name">Member since </span> <span class="value">--%>
					<%--							<sUser:showDate --%>
					<%--								model="['SUserInstance':user, 'propertyName':'dateCreated']" />--%>
					<%--							</span>--%>
					<%--						</div>--%>
					<%----%>
					<%----%>
					<%--						<div class="prop">--%>
					<%--							<span class="name">Last visited </span> <span class="value">--%>
					<%--							<sUser:showDate --%>
					<%--								model="['SUserInstance':user, 'propertyName':'lastLoginDate']" />--%>
					<%--							</span>--%>
					<%--						</div>--%>
					<%--					--%>

					<div class="prop">
						<span class="name"><i class="icon-time"></i>Member since </span>
						<div class="value">
							<g:formatDate format="dd/MM/yyyy" date="${user.dateCreated}"
								type="datetime" style="MEDIUM" />
						</div>
					</div>
					<g:if test="${user.lastLoginDate}">
						<div class="prop">
							<span class="name"><i class="icon-time"></i>Last visited </span>
							<div class="value">
								
									<g:formatDate format="dd/MM/yyyy" date="${user.lastLoginDate}"
										type="datetime" style="MEDIUM" />
							
							</div>
						</div>
					</g:if>
				</div>



				
				<sUser:showUserStory model="['userInstance':user, 'showDetails':true]"></sUser:showUserStory>
				
			</div>
			<%
				def downloadLogList = DownloadLog.findAllByAuthorAndStatus(user, 'Success', [sort: 'createdOn', order: 'asc'])
			%>

                        <div id="userprofilenavbar" class="navbar">
                            <!--data-spy="affix affix-top" data-offset-top="10px" style="z-index:10000"-->
                            <div class="navbar-inner">
                                <ul class="nav">

                                    <li><a href="#aboutMe"><i class="icon-user"></i>About Me</a></li>
                                    <li class="divider-vertical"></li>
                                    <li class="dropdown">
                                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                                <i class="icon-book"></i> Content
                                                <b class="caret"></b>
                                            </a>
                                            <ul class="dropdown-menu">
                                                <li><a href="#observations"><i class="icon-screenshot"></i>Observations</a></li>
                                                <li><a href="#identifications"><i class="icon-eye-open"></i>Identifications</a></li>
                                                <li><a href="#downloads"><i class="icon-download"></i>Downloads</a></li>
                                            </ul>
                                    </li>
                                    <li class="divider-vertical"></li>
                                    <li><a href="#groups"><i class="icon-group"></i>Groups</a></li>
                                    <li class="divider-vertical"></li>
                                    <li><a href="#activity"><i class="icon-tasks"></i>Activity</a></li>
                                </ul>
                            </div>
                        </div>
                        <div class="container">
                            <div id="aboutMe" class="super-section" style="overflow:auto;padding-bottom:10px;">
                                <h5>
                                    <i class="icon-user"></i>About Me
                			<sUser:ifOwns model="['user':user]">

						<a class="btn btn-link"
							href="${uGroup.createLink(action:'edit', controller:'SUser', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i
							class="icon-edit"></i>Edit </a>
					</sUser:ifOwns>
                                </h5>
                                <div class="section" style="clear:both;margin-left:20px;">
                                    <g:if test="${user.aboutMe}">
                                        ${user.aboutMe.encodeAsHTML().replace('\n', '<br/>\n')}
                                        </g:if>
                                </div>

                                <div class="section" style="clear:both;margin-left:20px;">
                                    <h6>
                                        Interested in Species Groups &amp; Habitats
                                    </h6>
                                    <sUser:interestedSpeciesGroups model="['userInstance':user]" />
                                    <div style="padding:3px;"></div>
                                    <sUser:interestedHabitats model="['userInstance':user]" />
                                </div>
                               <div id="observations_list_map" class="section observation span6"
                                    style="margin:0px;margin-left:20px;">
                                    <h6>
                                        Observations Spread
                                    </h6>
        			    <obv:showObservationsLocation
						model="['observationInstanceList':totalObservationInstanceList, 'ignoreMouseOutListener':true, width:420, height:400]">
				    </obv:showObservationsLocation>
                                </div>
                                <div id="expertice" class="section span6" style="margin:0px;margin-left:20px;width:420px;">
                                         <chart:showStats model="['title':'Observations by Species Group', columns:obvData.columns, data:obvData.data, htmlData:obvData.htmlData, htmlColumns:obvData.htmlColumns, width:420, height:420, 'hideTable':true]"/>
	                        </div>
 
                            </div>

                            <div id="content" class="super-section" style="clear: both;">
                                <h5>
                                    <span class="name" style="color: #b1b1b1;"> <i
                                            class="icon-book"></i>
                                    </span> Content
                                </h5>
                                <div id="observations" class="section" style="clear:both;margin-left:20px;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"> 
                                            <obv:showNoOfObservationsOfUser
                                            model="['user':user]" /> </span> Observations
                 			<sUser:ifOwns model="['user':user]">
						<a class="btn btn-link"
							href="${uGroup.createLink(action:'create', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-plus"></i>Add Observation</a>
					</sUser:ifOwns>

                                    </h6>
                                    
                                    <obv:showRelatedStory
                                    model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'user', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress]" />
                                </div>
                                <div id="identifications" class="section" style="clear:both;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"> 
                                            <obv:showNoOfRecommendationsOfUser model="['user':user]" /> </span>
                                        Identifications
                 			<sUser:ifOwns model="['user':user]">
						<a class="btn btn-link"
							href="${uGroup.createLink(action:'list', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-list"></i>Browse Observations</a>
					</sUser:ifOwns>

                                    </h6>
                                    <obv:showRelatedStory
                                    model="['controller':'SUser', 'resultController':'observation', 'action':'getRecommendationVotes', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'userIds', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'hideShowAll':true]" />

                                </div>
                                
                                <g:if test="${!downloadLogList.isEmpty()}">
                                <div id="downloads" class="section" style="clear: both;overflow:auto;">
                                    <h6>
                                        <span class="name" style="color: #b1b1b1;"></span> Downloads
                                    </h6>
                                    <obv:downloadTable model="[downloadLogList:downloadLogList]" />
                                </div>
                                </g:if>
                            </div>
			<div id="groups" class="super-section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
						class="icon-group"></i></span> Groups
                 			<sUser:ifOwns model="['user':user]">
						<a class="btn btn-link"
							href="${uGroup.createLink(action:'list', controller:'userGroup', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><i class="icon-plus"></i>Join Groups</a>
					</sUser:ifOwns>

				</h5>
				<uGroup:showUserUserGroups model="['userInstance':user]"></uGroup:showUserUserGroups>

			</div>
			<%--			</div>--%>
			<div id="activity" class="super-section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
                                                class="icon-tasks"></i> </span>Activity
                                        </h5>
                                        <feed:showAllActivityFeeds model="['user':user?.id, feedType:ActivityFeedService.USER, 'feedPermission':false, 'feedOrder':ActivityFeedService.LATEST_FIRST]" />
                        </div>

                        </div>
                    
		</div>
	</div>



	<r:script>
	var userRecoffset = 0;
    $(document).ready(function() {
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
	});
</r:script>
<g:javascript>
$(document).ready(function(){
    window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'SUser', action:'getRecommendationVotes', id:user.id, userGroupWebaddress:params.webaddress) }";
});
</g:javascript>
</body>

</html>
