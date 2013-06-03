<%@ page import="species.groups.UserGroup"%>

<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="UserGroups "/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="userGroups_list" />
</head>
<body>
	<div class="span12">
		<uGroup:showSubmenuTemplate   model="['entityName':'Groups']"/>
		
		
		<div class="">
		<uGroup:showUserGroupsListWrapper
			model="['totalUserGroupInstanceList':totalUserGroupInstanceList, 'userGroupInstanceList':UserGroupInstanceList, 'userGroupInstanceTotal':userGroupInstanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters]" />
		</div>
	</div>

	<g:javascript>
		$(document).ready(function(){
			def p = {
			<%
				params.each { key, value ->
					println '"'+key+'":"'+value+'",'
				}
			%>
				"tagsLink":"${g.createLink(action: 'tags')}",
                        }
                        jQuery.extend(window.params, p);
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
