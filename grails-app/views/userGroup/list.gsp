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
		<uGroup:showSubmenuTemplate   model="['entityName':'Groups']"/>
		
		
		<div class="">
		<uGroup:showUserGroupsListWrapper
			model="['totalUserGroupInstanceList':totalUserGroupInstanceList, 'userGroupInstanceList':userGroupInstanceList, 'userGroupInstanceTotal':userGroupInstanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters]" />
		</div>
	</div>

	<script type="text/javascript">
		$(document).ready(function(){
			window.params.tagsLink = "${uGroup.createLink(controller:'userGroup', action: 'tags')}";
		});
	</script>
	<r:script>
/*		$( "#search" ).unbind('click');
		$( "#search" ).click(function() {          
			var target = "${createLink(action:'search')}" + window.location.search;
			//updateGallery(target, ${queryParams.max}, 0, undefined, false);
        	return false;
		});*/
	</r:script>

</body>
</html>
