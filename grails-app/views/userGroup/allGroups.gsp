<%@ page import="species.groups.UserGroup"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="User groups "/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="userGroups_list" />
</head>
<body>

			<div class="span12">
				<uGroup:showSubmenuTemplate  model="['entityName':'All Groups']"/>

				<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]"/>
				<div class="center_panel">
				<div class="alertMsg ${(flash.message)?'alert':'' }"
					style="clear: both;">
					${flash.message}
				</div>
				
				<div id="allGroupsInfo" class="navbar super-section"
					style="clear: both; overflow: auto; background-image: none;">
					<button type="button" class="close" data-dismiss="alert">×</button>
					<uGroup:getSuggestedUserGroups />
				</div>
				</div>
			</div>

</body>
</html>
