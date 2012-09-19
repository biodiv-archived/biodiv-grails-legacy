<%@page import="species.participation.Observation"%>
<%@ page import="species.auth.SUser"%>
<%@ page import="species.utils.Utils"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="observations_show"/>
<g:set var="entityName"
	value="${message(code: 'SUser.label', default: 'SUser')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>

<style>
.prop .name {
	clear:both;
}
	</style>
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header">

				<h1>
					${fieldValue(bean: user, field: "name")}
					<sUser:ifOwns model="['user':user]">
						<span style="font-size: 60%; float: right;" class="btn btn-primary"> <g:link
								controller="SUser" action="edit" id="${user.id }">Edit Profile
							</g:link> </span>
					</sUser:ifOwns>
				</h1>

				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>
<%--				<obv:identificationByEmail model="['source':'userProfileShow', 'requestObject':request]" />--%>
				<div class="super-section">
					<div class="row section" style="">
						<div class="figure span3"
							style="float: left; max-height: 220px; max-width: 200px">
							<g:link controller="SUser" action="show"
								id="${user.id }">
								<img class="normal_profile_pic" src="${user.icon()}" />
							</g:link>

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
								<span class="name"><i class="icon-time"></i>Member since </span> <div class="value">
									<g:formatDate format="dd/MM/yyyy"
										date="${user.dateCreated}" type="datetime"
										style="MEDIUM" /> </div>
							</div>
							<div class="prop">
								<span class="name"><i class="icon-time"></i>Last visited </span> <div class="value">
									<g:formatDate format="dd/MM/yyyy"
										date="${user.lastLoginDate}" type="datetime"
										style="MEDIUM" /> </div>
							</div>
						</div>

					

					<div class="span8 observation_story">

						<div class="prop">
							<span class="name"><i class="icon-user"></i><g:message code="suser.username.label"
									default="Username" /> </span> <div class="value"> ${fieldValue(bean: user, field: "username")}
							</div>
						</div>

						<sUser:ifOwnsOrIsPublic model="['user':user, 'isPublic':!user.hideEmailId]">
							<div class="prop">
								<span class="name"><i class="icon-envelope"></i><g:message code="suser.email.label"
										default="Email" /> </span> <div class="value"> <a
									href="mailto:${fieldValue(bean: user, field: 'email')}">
										${fieldValue(bean: user, field: "email")} </a> </div>

							</div>
						</sUser:ifOwnsOrIsPublic>

						<g:if test="${user.location}">
							<div class="prop">
								<span class="name"><i class="icon-map-marker"></i><g:message code="suser.location.label"
										default="Location" /> </span> <div class="value"> ${fieldValue(bean: user, field: "location")}
								</div>
							</div>
						</g:if>
						
						<div class="prop">
							<span class="name"><i class="icon-road"></i><g:message code="suser.website.label"
									default="Website" /> </span> 
								<div class="value"> 
									<g:if test="${Utils.isURL(user.website) }">
										<a target="_blank" href="${user.getWebsiteLink()}">
										${fieldValue(bean: user, field: 'website')} </a>
									</g:if>
									<g:else>
										${fieldValue(bean: user, field: 'website')}
									</g:else>							
									<% def openId = user.openIds.find { it.url.indexOf('facebook') != -1 }
									def facebookUrl = openId?.url %>
									<g:if test="${facebookUrl}">
										 <div class="facebookButton" style="background-repeat:no-repeat; margin:0px 70px;height:33px;"> 
													<a class="fbJustConnect"
												target="_blank" 
												href="${facebookUrl}">Facebook Profile</a>
										</div>
									</g:if>
									
								</div>
								
								
									
						</div>
						
						
					</div>
				
				</div>

				
				<g:if test="${user.aboutMe}">
					<div class="section">
						<h5><i class="icon-user"></i>About Me</h5>
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

				<div class="section" style="clear: both;">
					<h5>
						<span class="name" style="color: #b1b1b1;">
						<i class="icon-screenshot"></i><obv:showNoOfObservationsOfUser model="['user':user]"/></i>
						</span> 	Observations
					</h5>
					<obv:showRelatedStory
						model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':user.id, 'id':'a']" />
				</div>
				
				<div class="section" style="clear: both;">
					<h5>
						<span class="name" style="color: #b1b1b1;">
						<i class="icon-check"></i><obv:showNoOfRecommendationsOfUser model="['user':user]"/></i>
						</span> 	Identifications
					</h5>
					<div>
						<ul id="recoSummary" class="pollBars">
							
						</ul>
						<div id="seeMoreMessage" class="message"></div>
						<div id="seeMore" class="btn btn-mini observation_links">Show all</div>
					</div>
					
					
				</div>
				
				<div class="section" style="clear: both;">
					<h5>
						<span class="name" style="color: #b1b1b1;">
						<i class="icon-screenshot"></i><uGroup:showNoOfUserGroupsOfUser model="['user':user]"/></i>
						</span> Groups
					</h5>
						<uGroup:showUserUserGroups model="['userInstance':user]"></uGroup:showUserUserGroups>
						
				</div>

							
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
			window.location.href = "${g.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
	    	return false;
	 	});
	 	
      	var max =  3;
         $("#seeMore").click(function() {   
         	preLoadRecos(max, true);
         	userRecoffset = max + userRecoffset;
		 });
         
         preLoadRecos(max, true);
         userRecoffset = max + userRecoffset;
	});
	   function preLoadRecos(max, seeAllClicked, obvId, liComponent){
         	$("#seeMoreMessage").hide();        	
         	$.ajax({
         		url: "${createLink(action:'getRecommendationVotes', id:user.id) }",
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
