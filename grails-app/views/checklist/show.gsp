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
<style>
.comment-post-btn{
	position:relative;
}
</style>
</head>
<body>
	
			<div class="span12">
				<clist:showSubmenuTemplate model="['entityName':checklistInstance.title, 'subHeading':checklistInstance.attribution]" />
			
				<div style="clear:both;"></div>
					<g:if test="${params.pos && lastListParams}">
						<div class="nav" style="width:100%;">
							<g:if test="${test}">
								<a class="pull-left btn ${prevObservationId?:'disabled'}" href="${uGroup.createLink([action:"show", controller:"checklist", id:prevObservationId, 'pos':params.int('pos')-1, 'userGroupWebaddress':(userGroup?userGroup.webaddress:userGroupWebaddress)])}">Prev Checklist</a>
								<a class="pull-right  btn ${nextObservationId?:'disabled'}"  href="${uGroup.createLink([action:"show", controller:"checklist",
									id:nextObservationId, 'pos':params.int('pos')+1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next Checklist</a>
								<%lastListParams.put('userGroupWebaddress', userGroup?userGroup.webaddress:userGroupWebaddress);
									lastListParams.put('fragment', params.pos);
								 %>
								<a class="btn" href="${uGroup.createLink(lastListParams)}" style="text-align: center;display: block;width: 125px;margin: 0 auto;">List Checklist</a>
							</g:if>
							<g:else>
								<a class="pull-left btn ${prevObservationId?:'disabled'}" href="${uGroup.createLink([action:"show", controller:"checklist",
									id:prevObservationId, 'pos':params.int('pos')-1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Prev Checklist</a>
								<a class="pull-right  btn ${nextObservationId?:'disabled'}"  href="${uGroup.createLink([action:"show", controller:"checklist",
									id:nextObservationId, 'pos':params.int('pos')+1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next Checklist</a>
								<%lastListParams.put('userGroupWebaddress', userGroup?userGroup.webaddress:userGroupWebaddress);
								lastListParams.put('fragment', params.pos);	 
								%>
								<a class="btn" href="${uGroup.createLink(lastListParams)}" style="text-align: center;display: block;width: 125px;margin: 0 auto;">List Checklist</a>
							</g:else>
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
								<td><sUser:interestedSpeciesGroups model="['userInstance':checklistInstance]"/></td>
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
				<div class="sidebar_section" >
					<a class="speciesFieldHeader" data-toggle="collapse" href="#license_information"><h5>License information</h5></a>
					<div id="license_information" class="speciesField collapse in">
							<table>
								<tr>
									<td class="prop"><span class="grid_3 name">Attribution</span></td> 
									<td class="linktext">${checklistInstance.attribution}</td>
								</tr>
								<tr>
									<td class="prop"><span class="grid_3 name">License</span></td> 
									<td><img src="${resource(dir:'images/license',file:checklistInstance?.license?.name?.getIconFilename()+'.png', absolute:true)}"
										title="${checklistInstance.license.name}"/></td>
								</tr>
							</table>
					</div>
				</div>	
				
				<g:if test="${checklistInstance.sourceText}" >
					<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#source"><h5>Source</h5></a>
						<div id="source" class="speciesField collapse in">
							<dl class="dl linktext">
								<dd>${checklistInstance.sourceText}</dd>
							</dl>
						</div>
					</div>		
				</g:if>
				
				<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#checklist_details"><h5>Checklist details</h5></a>
						<div id="checklist_details" class="speciesField collapse in">
							<dl class="dl linktext">
								<dd>${checklistInstance.description}</dd>
    						</dl>
    					</div>
    			</div>
				
				<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#checklistdata"><h5>Checklist</h5></a>
					<div id="checklistdata" class="speciesField collapse in">
					<clist:showData
						model="['checklistInstance':checklistInstance]">
					</clist:showData>
				</div>
			</div>
			
			<g:if test="${checklistInstance.refText}" >
				<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#references"><h5>References</h5></a>
					<div id="references" class="speciesField collapse in">
						<dl class="dl linktext">
							<dd>${checklistInstance.refText}</dd>
						</dl>
						</div>
					</div>		
			</g:if>
				
				
			<div class="union-comment" style="clear: both;">
				<feed:showAllActivityFeeds model="['rootHolder':checklistInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
				<%
					def canPostComment = customsecurity.hasPermissionAsPerGroups([object:checklistInstance, permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
				%>
				<comment:showAllComments model="['commentHolder':checklistInstance, commentType:'super', 'canPostComment':canPostComment, 'showCommentList':false]" />
			</div>
			
			</div>
	</div>	
	<g:javascript>
	$(document).ready(function(){
		window.params = {};
	});
	</g:javascript>
</body>
</html>
