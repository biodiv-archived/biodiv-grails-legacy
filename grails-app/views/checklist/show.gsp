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
							<th>State(s)</th>
							<th>District(s)</th>
							<th>Taluk(s)</th>
						</tr>
					</thead>
					<tbody>
							<tr>
								<td>${checklistInstance.speciesGroup?.name}</td>
								<td>${checklistInstance.speciesCount}</td>
								<td>${checklistInstance.placeName}</td>
								<td>${checklistInstance.state.join(",")}</td>
								<td>${checklistInstance.district.join(",")}</td>
								<td>${checklistInstance.taluka.join(",")}</td>
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
					<g:if test="${checklistInstance.refText}">
						<dt>References</dt>
    					<dd>${checklistInstance.refText}</dd>
					</g:if>
    				<g:if test="${checklistInstance.sourceText}" >
    					<dt>Links</dt>
    					<dd>${checklistInstance.sourceText}</dd>
    				</g:if>
    				<dt>Lat/Long</dt>
    				<dd>${checklistInstance.latitude + " " + checklistInstance.longitude}</dd>
    				
<%--    				<dt>All India</dt>--%>
<%--    				<dd>${checklistInstance.allIndia}</dd>--%>
<%--    				--%>
    			</dl>
			</div>
			
			<div>
				<table class="table table-hover span8" style="margin-left: 0px;">
					
					<thead>
						<tr>
							<g:each in="${checklistInstance.fetchColumnNames()}" var="cName">
								<th>${cName}</th>
							</g:each>
						</tr>
					</thead>
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
							</g:if>
							
							<g:if test="${row.reco}">
								<g:if test="${row.reco.taxonConcept && row.reco.taxonConcep.canonicalForm != null}">
									<td>
									<a href="${uGroup.createLink(action:'show', controller:'species', id:reco.taxonConcept.findSpeciesId(), 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
										<i> ${row.reco.taxonConcept.canonicalForm}</i>
									</a>
									</td>
								</g:if>
								<g:else>
									<td><i>${row.value}</i></td>
								</g:else>
							</g:if>
							<g:else>
								<td>${row.value}</td>
							</g:else>
						</g:each>
					</tbody>
				</table>
			
			</div>	
			<div class="union-comment" style="clear: both;">
				<feed:showAllActivityFeeds model="['rootHolder':checklistInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
				<%
					def canPostComment = customsecurity.hasPermissionAsPerGroups([object:checklistInstance, permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
				%>
				<comment:showAllComments model="['commentHolder':checklistInstance, commentType:'super', 'canPostComment':canPostComment, 'showCommentList':false]" />
			</div>
			
			</div>
		
	<r:script>
	</r:script>
</body>
</html>
