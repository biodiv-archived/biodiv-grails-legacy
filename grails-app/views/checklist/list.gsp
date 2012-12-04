<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'checklist', action:'list')}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'checklistList.label', default: 'Checklist')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<%--<script src="http://maps.google.com/maps/api/js?sensor=true"></script>--%>
<r:require modules="checklist"/>
</head>
<body>
	
			<div class="span12">
				<div class="page-header clearfix">
						<h1>
							<g:message code="default.observation.heading" args="[entityName]" />
						</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message alert alert-info">
						${flash.message}
					</div>
				</g:if>
				
				<table class="table table-hover span8" style="margin-left: 0px;">
				<thead>
					<tr>
						<th>Title</th>
						<th>Species Group</th>
						<th>No. of Species</th>
						<th>Place Name</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${checklistList}" status="i"
						var="checklistInstance">
						<tr>
							<td><a href="${uGroup.createLink(controller:'checklist', action:'show', id:checklistInstance.id)}">${checklistInstance.title}</a></td>
							<td>${checklistInstance.speciesGroup?.name}</td>
							<td>${checklistInstance.speciesCount}</td>
							<td>${checklistInstance.placeName}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
				
			</div>
		
	<r:script>
	</r:script>
</body>
</html>
