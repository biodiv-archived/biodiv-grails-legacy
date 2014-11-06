<%@ page import="species.groups.UserGroup"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'group.value.user')} "/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="userGroups_list" />
</head>
<body>
	
			<div class="span12">
				<uGroup:showSubmenuTemplate model="['entityName':'My Groups']"/>
				
				<div class="alertMsg ${(flash.message)?'alert':'' }"
					style="clear: both;">
					${flash.message}
				</div>

				<sec:ifLoggedIn>
					<div id="myGroupsInfo" class="navbar super-section"
						style="overflow: auto; background-image: none;">
						<button type="button" class="close" data-dismiss="alert">×</button>
						<uGroup:getCurrentUserUserGroupsSidebar />
					</div>
				</sec:ifLoggedIn>
			</div>
		
</body>
</html>
