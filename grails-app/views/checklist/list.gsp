<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="Checklists"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="checklist_list"/>
</head>
<body>
<div class="span12">
			<clist:showSubmenuTemplate model="['entityName':title]" />

			<div class="gallerytoolbar">
				<clist:filterTemplate />		
			</div>
			
			<div style="clear: both;"></div>
			
			<g:if test="${!isSearch && instanceTotal > 0}">
				<div id="map_view_bttn" class="btn-group" style="clear:both;">
					<a class="btn btn-success dropdown-toggle" data-toggle="dropdown"
						href="#"
						onclick="$(this).parent().css('background-color', '#9acc57'); showChecklistMapView(); return false;">
						Map view <span class="caret"></span> </a>
				</div>
			</g:if>
			
			<div id="observations_list_map" class="observation"
				style="clear: both; display: none;">
				<clist:showChecklistLocation
					model="['checklistInstanceList':checklistMapInstanceList, 'userGroup':userGroup]">
				</clist:showChecklistLocation>
			</div>
			
			<clist:showList />
		
</div>
</body>
</html>
