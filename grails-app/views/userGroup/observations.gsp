<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>

<meta name="layout" content="main" />
<link rel="image_src"
	href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />
<r:require modules="userGroups_show, observations_list"/>
<g:set var="entityName"
	value="${userGroupInstance.name}" />
<title><g:message code="default.show.label" args="[userGroupInstance.name]" />
</title>

</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="observation span12">
				

				<div class="page-header clearfix">
					<div style="width: 100%;">
						<uGroup:showHeader model=['userGroupInstance':userGroupInstance] />
					</div>
				</div>
				
				<g:if test="${flash.message }">
					<div class="message alert">
						${flash.message}
					</div>
				</g:if>
				
				<div>
					
					<uGroup:showSidebar/>
					
					<div class="super-section userGroup-section">
						<div class="section">
								<h5>Observations</h5>
								<obv:showObservationsList model="['observationInstanceList':observations, 'observationInstanceTotal':totalCount, 'queryParams':params]" />
						</div>						
					</div>
				</div>
				
			</div>
		</div>
	</div>
	<g:javascript>
		$(document).ready(function(){
			window.params = {
			<%
				params.each { key, value ->
					println '"'+key+'":"'+value+'",'
				}
			%>
				"tagsLink":"${g.createLink(action: 'tags')}",
				"queryParamsMax":"${params.queryParams?.max}"
			}
		});
	</g:javascript>
	<r:script>
	</r:script>	
</body>
</html>
