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
		args="[userGroupInstance.name]" /></title>

</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate />
		<div class="userGroup-section">
			<div class="section">
				<div class="page-header clearfix">
					<h5>Observations</h5>
					<div class="btn-group pull-right" style="z-index: 10;">
						<uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
							<g:link controller="observation" action="create"
								params="['userGroup':userGroupInstance.id]"
								class="btn btn-large btn-info">
								<i class="icon-plus"></i>Add an Observation</g:link>
						</uGroup:isAMember>
					</div>
				</div>
				<obv:showObservationsListWrapper
					model="['totalObservationInstanceList':totalObservationInstanceList, 'observationInstanceList':observationInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters]" />
			</div>
		</div>

	</div>


	<r:script>
	</r:script>
</body>
</html>
