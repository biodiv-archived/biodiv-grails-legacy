<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<meta property="og:type" content="article" />
<meta property="og:title" content="${userGroupInstance.name}" />
<meta property="og:url"
	content="${createLink(action:'show', id:userGroupInstance.id, base:Utils.getDomainServerUrl(request))}" />
<g:set var="fbImagePath" value="" />
<%
		def r = userGroupInstance.icon(ImageType.NORMAL);
		fbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)
%>
<meta property="og:image"
	content="${createLinkTo(file: fbImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />
<meta property="og:site_name" content="${Utils.getDomainName(request)}" />
<g:set var="description" value="" />
<g:set var="domain" value="${Utils.getDomain(request)}" />
<%
				String fbAppId;
				if(domain.equals(grailsApplication.config.wgp.domain)) {
					fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
				} else if(domain.equals(grailsApplication.config.ibp.domain)) {
					fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
				}
				
				description = userGroupInstance.description.trim() ;
				
		%>

<meta property="fb:app_id" content="${fbAppId }" />
<meta property="fb:admins" content="581308415,100000607869577" />
<meta property="og:description" content='${description}' />

<link rel="image_src"
	href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />

<g:set var="entityName"
	value="${userGroupInstance.name}" />
<title><g:message code="default.show.label" args="[userGroupInstance.name]" />
</title>
<r:require modules="userGroups_show"/>
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="observation span12">
				

				<div class="page-header clearfix">
					<div style="width: 100%;">
						<uGroup:showHeader model=['userGroupInstance':userGroupInstance] />
						
						<div style="float: right;">
							<sec:permitted className='species.groups.UserGroup' id='${userGroupInstance.id}' permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

								<a class="btn btn-primary pull-right"
									href="${createLink(action:'edit', id:userGroupInstance.id)}">
									Edit Group </a>

								<a class="btn btn-danger btn-primary pull-right"
									style="margin-right: 5px; margin-bottom: 10px;"
									href="${createLink(action:'flagDeleted', id:userGroupInstance.id)}"
									onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This group will be deleted. Are you sure ?')}');">Delete
									Group </a>
							</sec:permitted>
						</div>
					</div>
				</div>
				
				<g:if test="${flash.message }">
					<div class="span12 message alert">
						${flash.message}
					</div>
				</g:if>
				
				<div>
				<div class="span4 sidebar left-sidebar">
					
					<div class="super-section">
						<div class="section">
							<h5><i class="icon-user"></i>Founders</h5>
							<g:each in="${[]}" var="sUser">
								<g:link controller="SUser" action="show" id="${sUser.id}">${sUser.name}</g:link>
							</g:each>
						</div>
				
						<div class="section">
							<h5><i class="icon-user"></i>Members</h5>
							<g:each in="${userGroupInstance.members}" var="sUser">
								<g:link controller="SUser" action="show" id="${sUser.id}">${sUser.name}</g:link>,
							</g:each>
						</div>
					</div>
					
					<div class="super-section">
						<div class="section">
							<uGroup:showAllTags model="['tagFilterByProperty':'All' , 'params':params, 'isAjaxLoad':true]" />
						</div>	
					</div>
					
					<div class="super-section">
						<div class="section">
							<div class="prop">
								<span class="name"><i class="icon-time"></i>Founded</span>
								<obv:showDate
									model="['userGroupInstance':userGroupInstance, 'propertyName':'foundedOn']" /> </div>
						</div>
					</div>
										
					<div class="super-section">
						<div class="section">
							<g:link action="aboutUs">More about us here</g:link> or<br/>
							<g:link action="contactUs">Contact us here</g:link>
						</div>
					</div>
					
				</div>
				
				<div class="super-section span8" style="width:580px">
					<div class="description notes_view">
						${userGroupInstance.description}
					</div>					
				</div>
				</div>
				
			</div>
		</div>
	</div>
	<r:script>
		$(document).ready(function(){

		});
	</r:script>	
</body>
</html>
