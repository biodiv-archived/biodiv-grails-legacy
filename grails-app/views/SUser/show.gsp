<%@page import="species.participation.Observation"%>
<%@ page import="species.auth.SUser"%>
<%@ page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical"
	href="${Utils.getIBPServerDomain() + createLink(controller:'SUser', action:'show', id:user.id)}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="observations_show" />
<g:set var="entityName"
	value="${message(code: 'SUser.label', default: 'SUser')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>

<style>
.prop .name {
	clear: both;
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
		<div class="super-section">
			<div class="row section" style="">
				<div class="figure span3"
					style="float: left; max-height: 220px; max-width: 200px">
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
					<div class="prop">
						<span class="name"><i class="icon-time"></i>Last visited </span>
						<div class="value">
							<g:formatDate format="dd/MM/yyyy" date="${user.lastLoginDate}"
								type="datetime" style="MEDIUM" />
						</div>
					</div>
				</div>



				<div class="span8 observation_story">
					<dl class="dl-horizontal">
						<dt>
							<i class="icon-user"></i>
							<g:message code="suser.username.label" default="Username" />
						</dt>
						<dd>
							${fieldValue(bean: user, field: "username")}
						</dd>
						<dt>
							<i class="icon-user"></i>
							<g:message code="suser.name.label" default="Full Name" />
						</dt>
						<dd>
							${fieldValue(bean: user, field: "name")}
						</dd>

						<sUser:ifOwnsOrIsPublic
							model="['user':user, 'isPublic':!user.hideEmailId]">
							<dt>
								<i class="icon-envelope"></i>
								<g:message code="suser.email.label" default="Email" />
							</dt>
							<dd>
								<a href="mailto:${fieldValue(bean: user, field: 'email')}">
									${fieldValue(bean: user, field: "email")} </a>
							</dd>
						</sUser:ifOwnsOrIsPublic>

						<g:if test="${user.location}">
							<dt>
								<i class="icon-map-marker"></i>
								<g:message code="suser.location.label" default="Location" />
							</dt>
							<dd>
								${fieldValue(bean: user, field: "location")}
							</dd>
						</g:if>

						<dt>
							<i class="icon-road"></i>
							<g:message code="suser.website.label" default="Website" />
						</dt>
						<dd>
							<g:if test="${Utils.isURL(user.website) }">
								<a target="_blank" href="${user.getWebsiteLink()}"> ${fieldValue(bean: user, field: 'website')}
								</a>
							</g:if>
							<g:else>
								${fieldValue(bean: user, field: 'website')}
							</g:else>
							<% def openId = user.openIds.find { it.url.indexOf('facebook') != -1 }
									def facebookUrl = openId?.url %>
							<g:if test="${facebookUrl}">
								<div class="facebookButton"
									style="background-repeat: no-repeat; height: 33px;">
									<a class="fbJustConnect" target="_blank" href="${facebookUrl}">Facebook
										Profile</a>
								</div>
							</g:if>
						</dd>
					</dl>
				</div>
			</div>


			<g:if test="${user.aboutMe}">
				<div class="section">
					<h5>
						<i class="icon-user"></i>About Me
					</h5>
					${user.aboutMe.encodeAsHTML().replace('\n', '<br/>\n')}
				</div>
			</g:if>

			<%--				<g:if test="${user.openIds}">--%>
			<%--					<div class="section" style="clear: both;">--%>
			<%--						<h5>--%>
			<%--							<span class="name" style="color: #b1b1b1;"><i class="icon-gift"></i>${user.openIds?.size()}</span>--%>
			<%--							External Provider Identification<g:if test="${user.openIds?.size()>1}">s</g:if>--%>
			<%--						</h5>--%>
			<%--						<g:each in="${user.openIds}" var="openId">--%>
			<%--							<g:if test="${openId.url.indexOf('facebook') != -1}">--%>
			<%--								<a href="${openId.url}" target="blank">Facebook</a>--%>
			<%--							</g:if>--%>
			<%--							<g:elseif test="${openId.url.indexOf('google') != -1 }">--%>
			<%--								<a href="${openId.url}" target="blank">Google</a>--%>
			<%--							</g:elseif>--%>
			<%--							<g:elseif test="${openId.url.indexOf('yahoo')  != -1}">--%>
			<%--								<a href="${openId.url}" target="blank">Yahoo</a>--%>
			<%--							</g:elseif>--%>
			<%--							<g:else>--%>
			<%--								<a href="${openId.url}" target="blank">openId.url</a>--%>
			<%--							</g:else>--%>
			<%--						</g:each>--%>
			<%--					</div>--%>
			<%----%>
			<%--				</g:if>--%>
			<%--			<div class="super-section">--%>
			<%--				<h3>Interests</h3>--%>
			<div class="section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
						class="icon-screenshot"></i> </span> Species Groups
				</h5>
				<sUser:interestedSpeciesGroups model="['userInstance':user]" />
			</div>

			<div class="section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
						class="icon-screenshot"></i> </span> Habitat
				</h5>
				<sUser:interestedHabitats model="['userInstance':user]" />
			</div>

			<div class="section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
						class="icon-screenshot"></i> <obv:showNoOfObservationsOfUser
							model="['user':user]" /> </span> Observations
				</h5>
				<obv:showRelatedStory
					model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'a', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress]" />
			</div>

			<!-- div class="section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
						class="icon-check"></i>
					<obv:showNoOfRecommendationsOfUser model="['user':user]" /> </span>
					Identifications
				</h5>
				<div>
					<ul id="recoSummary" class="pollBars">

					</ul>
					<div id="seeMoreMessage" class="message"></div>
					<div id="seeMore" class="btn btn-mini observation_links">Show
						all</div>
				</div>


			</div-->

			<div class="section" style="clear: both;">
				<h5>
					<span class="name" style="color: #b1b1b1;"> <i
						class="icon-screenshot"></i> <uGroup:showNoOfUserGroupsOfUser
							model="['user':user]" /> </span> Groups
				</h5>
				<uGroup:showUserUserGroups model="['userInstance':user]"></uGroup:showUserUserGroups>

			</div>
			<%--			</div>--%>

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
	 	
      	var max =  3;
         $("#seeMore").click(function() {   
         	preLoadRecos(max, true);
         	userRecoffset = max + userRecoffset;
		 });
         
         //preLoadRecos(max, true);
         userRecoffset = max + userRecoffset;
	});
	   function preLoadRecos(max, seeAllClicked, obvId, liComponent){
         	$("#seeMoreMessage").hide();        	
         	$.ajax({
         		url: "${uGroup.createLink(controller:params.controller, action:'getRecommendationVotes', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, id:user.id) }",
				method: "POST",
				dataType: "json",
				data: {max:max , offset:userRecoffset, obvId:obvId},	
				success: function(data) {
					if(seeAllClicked){
						$("#recoSummary").append(data.recoHtml);
						var uniqueVotes = parseInt(data.uniqueVotes);
						if(uniqueVotes < 3){
							$("#seeMore").hide();
						} else {
							$("#seeMore").show();
						}
					}else{
						$(liComponent).replaceWith(data.recoHtml)
					}
					
				}, error: function(xhr, status, error) {
	    			handleError(xhr, status, error, undefined, function() {
		    			var msg = $.parseJSON(xhr.responseText);
		    			if(msg.info) {
		    				showRecoUpdateStatus(msg.info, 'info');
		    			}else if(msg.success){
		    				showRecoUpdateStatus(msg.success, 'success');
						} else {
							showRecoUpdateStatus(msg.error, 'error');
						}
					});
			   	}
			});
         }
	
         
</r:script>
</body>

</html>
