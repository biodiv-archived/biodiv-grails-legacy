<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>

<meta name="layout" content="main" />
<link rel="image_src"
	href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
<r:require modules="userGroups_show, observations_list" />
<g:set var="entityName" value="${userGroupInstance.name}" />
<title><g:message code="default.show.label"
		args="[userGroupInstance.name]" />
</title>

</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate model="['entityName':'Observations']" />
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]" />
		<div class="userGroup-section center_panel">

			<div class="btn-group pull-right" style="z-index: 10;">
				<uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
					<g:link controller="observation" action="create"
						params="['userGroup':userGroupInstance.id]"
						class="btn btn-large btn-info">
						<i class="icon-plus"></i>Add an Observation</g:link>
				</uGroup:isAMember>
			</div>

			<obv:showObservationsListWrapper
				model="['totalObservationInstanceList':totalObservationInstanceList, 'observationInstanceList':observationInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters, 'userGroup':userGroupInstance]" />
		</div>

	</div>
	<r:script>
	</r:script>
</body>
</html>
