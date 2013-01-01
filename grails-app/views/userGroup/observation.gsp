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
		
		<div class="page-header clearfix">
			<div style="width: 100%;">
				<div class="span8 main_heading" style="margin-left: 0px;">
					<h1>Observations</h1>
				</div>

				<div style="float: right; margin: 10px 0;">
					<sec:permitted className='species.groups.UserGroup'
						id='${userGroupInstance.id}'
						permission='${org.springframework.security.acls.domain.BasePermission.WRITE}'>
						<a
							href="${uGroup.createLink(mapping:'userGroupModule',
						controller:'observation', action:'create', 'userGroupWebaddress':userGroupInstance.webaddress,
						'userGroupId':userGroupInstance.id)}"
							class="btn btn-info"> <i class="icon-plus"></i>Add
							an Observation</a>
					</sec:permitted>
				</div>
			</div>
		</div>
		<div style="clear: both;"></div>
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]" />
		<div class="userGroup-section">
			<obv:showObservationsListWrapper
				model="['totalObservationInstanceList':totalObservationInstanceList, 'observationInstanceList':observationInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters, 'userGroup':userGroupInstance]" />
		</div>

	</div>
	<r:script>
	</r:script>
</body>
</html>
