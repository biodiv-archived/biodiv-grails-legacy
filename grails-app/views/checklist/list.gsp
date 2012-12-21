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
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
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
				
				<div class="gallerytoolbar">
						<div class="filters" style="position: relative; overflow: visible;">
							<obv:showGroupFilter model="['hideHabitatFilter':true]"/>
						</div>
				</div>
					
				<div class="info-message">
				<g:if test="${instanceTotal > 0}">
					<span class="name" style="color: #b1b1b1;margin-top: 10px;"><i
						class="icon-screenshot"></i> ${instanceTotal} </span>
						checklist<g:if test="${instanceTotal > 1}">s</g:if>
				</g:if>
				</div>
				<g:if test="${!isSearch}">
					<div id="map_view_bttn" class="btn-group" style="clear:both;">
						<a class="btn btn-success dropdown-toggle" data-toggle="dropdown"
							href="#"
							onclick="$(this).parent().css('background-color', '#9acc57'); showChecklistMapView(); return false;">
							Map view <span class="caret"></span> </a>
					</div>
				</g:if>
				
				<div id="checklist_list_map" class="observation"
					style="clear: both; display: none;">
					<clist:showChecklistLocation
						model="['checklistInstanceList':checklistMapInstanceList, 'userGroup':userGroup]">
					</clist:showChecklistLocation>
				</div>
				
				<div class="checklist_list_main" style="clear:both;">
				<table class="table table-hover span8" style="margin-left: 0px;">
					<thead>
						<tr>
							<th>Title</th>
							<th>Species Group</th>
							<th>No. of Species</th>
							<th>Place Name</th>
						</tr>
					</thead>
<%--						<clist:showFilteredCheckList />--%>
					<tfoot class="table-footer"></tfoot>
				</table>
				<div class="mainContentList">
					<clist:showFilteredCheckList />
				</div>
    			</div>
			
			<g:if test="${instanceTotal > (queryParams.max?:0)}">
				<div class="centered">
					<div class="btn loadMore">
						<span class="progress" style="display: none;">Loading ... </span>
						<span class="buttonTitle">Load more</span>
					</div>
				</div>
			</g:if>
	
			<%
				activeFilters?.loadMore = true
				activeFilters?.append = true
				activeFilters?.webaddress = userGroup?.webaddress
			%>
		active Filters ====== ${ activeFilters}
	query params ================ ${queryParams }
	total === ${instanceTotal}
			<div class="paginateButtons" style="visibility: hidden; clear: both">
				<p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'checklist'}"
					userGroup="${userGroup}" userGroupWebaddress="${userGroupWebaddress}"
				 	max="${queryParams.max}" params="${activeFilters}" />
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
		"isGalleryUpdate":true,
		"offset":0
	}

});
</g:javascript>		
</body>
</html>
