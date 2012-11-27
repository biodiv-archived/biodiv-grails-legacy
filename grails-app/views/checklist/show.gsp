<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'checklist', action:'show', id:checklistInstance.id)}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'checklistShow.label', default: 'Checklist Show')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<%--<script src="http://maps.google.com/maps/api/js?sensor=true"></script>--%>
<r:require modules="checklist"/>
</head>
<body>
	
			<div class="span12">
				<div class="page-header clearfix">
						<h1>
							${checklistInstance.title}
						</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message alert alert-info">
						${flash.message}
					</div>
				</g:if>
			<div>	
				<table class="table table-hover" style="margin-left: 0px;">
					<thead>
						<tr>
							<th>Species Group</th>
							<th>No. of Species</th>
							<th>Place Name</th>
						</tr>
					</thead>
					<tbody>
							<tr>
								<td>${checklistInstance.speciesGroup?.name}</td>
								<td>X</td>
								<td>${checklistInstance.placeName}</td>
							</tr>
					</tbody>
				</table>
			</div>
			
			<div>
			    <dl class="dl-horizontal">
			     	<dt>Description</dt>
    				<dd>${checklistInstance.description}</dd>
			    	<dt>Attribution</dt>
    				<dd>${checklistInstance.attribution}</dd>
    				
			    	<dt>License</dt>
			    	<dd>${checklistInstance.license.name}</dd>
<%--    				<dd>--%>
<%--    					<img class="small_profile_pic"--%>
<%--						src="${checklistInstance.license.name.getIconFilename()}"--%>
<%--						title="${checklistInstance.license.name}" />--%>
<%--					</dd>--%>
<%--    				--%>
    				<dt>References</dt>
    				<dd>${checklistInstance.refText}</dd>
    				<dt>Links</dt>
    				<dd>${checklistInstance.linkText}</dd>
    			</dl>
			</div>
			
			<div>
				<table class="table table-hover span8" style="margin-left: 0px;">
<%--					<thead>--%>
<%--						<tr>--%>
<%--							<th>Species Group</th>--%>
<%--							<th>No. of Species</th>--%>
<%--							<th>Place Name</th>--%>
<%--						</tr>--%>
<%--					</thead>--%>
					<tbody>
						<%
							def preRowNo = -1
						%>
						<g:each in="${checklistInstance.row}" var="row">
							<%
								def currentRowNo = row.rowId
							%>
							<g:if test="${preRowNo !=  currentRowNo}">
								<g:if test="${preRowNo != -1}">
									</tr>
								</g:if>
								<%
									preRowNo = currentRowNo
								%>
								<tr>
									<td>${row.value}</td>
							</g:if>
							<g:else>
								<td>${row.value}</td>
							</g:else>
						</g:each>
					</tbody>
				</table>
			
			</div>	
				
				
			</div>
		
	<r:script>
	</r:script>
</body>
</html>
