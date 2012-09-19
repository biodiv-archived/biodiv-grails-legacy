<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'userGroup.label', default: 'UserGroup')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<r:require modules="userGroups_list" />
</head>
<body>
	
			<div class="span12">
				<uGroup:showSubmenuTemplate/>
				<div class="page-header ">
					<h1>My Groups</h1>
				</div>

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
