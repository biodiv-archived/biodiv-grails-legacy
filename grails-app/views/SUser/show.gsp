<%@page import="species.participation.Observation"%>
<%@ page import="species.auth.SUser"%>
<%@ page import="species.utils.Utils"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'SUser.label', default: 'SUser')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>
<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css')}"
	type="text/css" media="all" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'js/jquery/jquery.jcarousel-0.2.8/themes/classic/',file:'skin.css')}" />
	
<g:javascript src="tagit.js"></g:javascript>

<g:javascript src="jquery/jquery.jcarousel-0.2.8/jquery.jcarousel.js"/>

<g:javascript src="species/carousel.js"/>

<g:javascript src="species/observations.js"/>

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
					${fieldValue(bean: SUserInstance, field: "name")}
					<sUser:ifOwns model="['user':SUserInstance]">
						<span style="font-size: 60%; float: right;" class="btn btn-primary"> <g:link
								controller="SUser" action="edit" id="${SUserInstance.id }">Edit Profile
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
								id="${SUserInstance.id }">
								<img class="normal_profile_pic" src="${SUserInstance.icon()}" />
							</g:link>

							<%--						<div class="prop">--%>
							<%--							<span class="name">Member since </span> <span class="value">--%>
							<%--							<sUser:showDate --%>
							<%--								model="['SUserInstance':SUserInstance, 'propertyName':'dateCreated']" />--%>
							<%--							</span>--%>
							<%--						</div>--%>
							<%----%>
							<%----%>
							<%--						<div class="prop">--%>
							<%--							<span class="name">Last visited </span> <span class="value">--%>
							<%--							<sUser:showDate --%>
							<%--								model="['SUserInstance':SUserInstance, 'propertyName':'lastLoginDate']" />--%>
							<%--							</span>--%>
							<%--						</div>--%>
							<%--					--%>

							<div class="prop">
								<span class="name"><i class="icon-time"></i>Member since </span> <div class="value">
									<g:formatDate format="dd/MM/yyyy"
										date="${SUserInstance.dateCreated}" type="datetime"
										style="MEDIUM" /> </div>
							</div>
							<div class="prop">
								<span class="name"><i class="icon-time"></i>Last visited </span> <div class="value">
									<g:formatDate format="dd/MM/yyyy"
										date="${SUserInstance.lastLoginDate}" type="datetime"
										style="MEDIUM" /> </div>
							</div>
						</div>

					

					<div class="span8 observation_story">

						<div class="prop">
							<span class="name"><i class="icon-user"></i><g:message code="suser.username.label"
									default="Username" /> </span> <div class="value"> ${fieldValue(bean: SUserInstance, field: "username")}
							</div>
						</div>

						<sUser:ifOwnsOrIsPublic model="['user':SUserInstance, 'isPublic':!SUserInstance.hideEmailId]">
							<div class="prop">
								<span class="name"><i class="icon-envelope"></i><g:message code="suser.email.label"
										default="Email" /> </span> <div class="value"> <a
									href="mailto:${fieldValue(bean: SUserInstance, field: 'email')}">
										${fieldValue(bean: SUserInstance, field: "email")} </a> </div>

							</div>
						</sUser:ifOwnsOrIsPublic>

						<g:if test="${SUserInstance.location}">
							<div class="prop">
								<span class="name"><i class="icon-map-marker"></i><g:message code="suser.location.label"
										default="Location" /> </span> <div class="value"> ${fieldValue(bean: SUserInstance, field: "location")}
								</div>
							</div>
						</g:if>
						
						<div class="prop">
							<span class="name"><i class="icon-road"></i><g:message code="suser.website.label"
									default="Website" /> </span> 
								<div class="value"> 
									<g:if test="${Utils.isURL(SUserInstance.website) }">
										<a
									target="blank"
									href="${fieldValue(bean: SUserInstance, field: 'website')}">
										${fieldValue(bean: SUserInstance, field: 'website')} </a>
									</g:if>
									<g:else>
										${fieldValue(bean: SUserInstance, field: 'website')}
									</g:else>							
									<% def openId = SUserInstance.openIds.find { it.url.indexOf('facebook') != -1 }
									def facebookUrl = openId?.url %>
									<g:if test="${facebookUrl}">
										 <div class="facebookButton" style="background-repeat:no-repeat; margin:0px;height:33px;"> 
													<a class="fbJustConnect"
												target="_blank" 
												href="${facebookUrl}">Facebook Profile</a>
										</div>
									</g:if>
									
								</div>
								
								
									
						</div>
						
						
					</div>
				
				</div>

				
				<g:if test="${SUserInstance.aboutMe}">
					<div class="section">
						<h5><i class="icon-user"></i>About Me</h5>
						${SUserInstance.aboutMe.encodeAsHTML().replace('\n', '<br/>\n')}
					</div>
				</g:if>

<%--				<g:if test="${SUserInstance.openIds}">--%>
<%--					<div class="section" style="clear: both;">--%>
<%--						<h5>--%>
<%--							<span class="name" style="color: #b1b1b1;"><i class="icon-gift"></i>${SUserInstance.openIds?.size()}</span>--%>
<%--							External Provider Identification<g:if test="${SUserInstance.openIds?.size()>1}">s</g:if>--%>
<%--						</h5>--%>
<%--						<g:each in="${SUserInstance.openIds}" var="openId">--%>
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
						<i class="icon-screenshot"></i><obv:showNoOfObservationsOfUser model="['user':SUserInstance]"/></i>
						</span> 	Observations
					</h5>
					<obv:showRelatedStory
						model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':SUserInstance.id, 'id':'a']" />
				</div>

				<div class="section" style="clear: both;">
					<h5>
						<span class="name" style="color: #b1b1b1;">
						<i class="icon-check"></i><obv:showNoOfRecommendationsOfUser model="['user':SUserInstance]"/></i>
						</span> 	Identifications
					</h5>
					<div>
						<ul id="recoSummary" class="pollBars">
							
						</ul>
						<div id="seeMoreMessage" class="message"></div>
						<div id="seeMore" class="btn btn-mini observation_links">Show all</div>
					</div>
					
					
				</div>

				<div class="section" style="clear: both;overflow:auto;">
					<obv:showAllTags
						model="['tagFilterByProperty':'User', 'tagFilterByPropertyValue':SUserInstance.id, 'isAjaxLoad':false]" />
				</div>
			
			</div>
			</div>
		</div>


	</div>
<g:javascript>
	$(document).ready(function() {
		$("#seeMoreMessage").hide();
		$('#tc_tagcloud a').click(function(){
			var tg = $(this).contents().first().text();
			window.location.href = "${g.createLink(controller:'observation', action: 'list')}?tag=" + tg ;
	    	return false;
	 	});
	 	
      
         $("#seeMore").click(function(){
         	preLoadRecos(100, true);
		 });
         
         preLoadRecos(3, false);
	});
	   function preLoadRecos(max, seeAllClicked){
         	$("#seeMoreMessage").hide();
         	
         	
        	$.ajax({
         		url: "${createLink(action:'getRecommendationVotes', id:SUserInstance.id) }",
				method: "POST",
				dataType: "json",
				data: {max:max , offset:0},	
				success: function(data) {
					$("#recoSummary").html(data.recoHtml);
					var uniqueVotes = parseInt(data.uniqueVotes);
					if(uniqueVotes > 3 && !seeAllClicked){
						$("#seeMore").show();
					} else {
						$("#seeMore").hide();
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
	
         
</g:javascript>	
</body>

</html>
