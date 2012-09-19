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
		<uGroup:showSubmenuTemplate />
		<div class="page-header">
			<h1>Groups</h1>
		</div>

		<uGroup:showUserGroupsListWrapper
			model="['totalUserGroupInstanceList':totalUserGroupInstanceList, 'userGroupInstanceList':UserGroupInstanceList, 'userGroupInstanceTotal':userGroupInstanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters]" />
	</div>

	<g:javascript>
		$(document).ready(function(){
			window.params = {
			<%
				params.each { key, value ->
					println '"'+key+'":"'+value+'",'
				}
			%>
				"tagsLink":"${g.createLink(action: 'tags')}",
				"queryParamsMax":"${params.queryParams?.max}"
			}
		});
	</g:javascript>
	<r:script>
		$( "#search" ).unbind('click');
		$( "#search" ).click(function() {          
			var target = "${createLink(action:'search')}" + window.location.search;
			//updateGallery(target, ${queryParams.max}, 0, undefined, false);
        	return false;
		});
	</r:script>

</body>
</html>
