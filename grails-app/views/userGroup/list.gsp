<%@ page import="species.groups.UserGroup"%>

<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'group.value.user')} "/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="userGroups_list" />
</head>
<body>
<style type="text/css">
.observations_list{
	overflow-y:scroll;
	height:600px;
}
</style>
	<div class="span12">
    <%
    def group=g.message(code:'default.groups.label')
    %>
		<uGroup:showSubmenuTemplate   model="['entityName':group]"/>
		
		
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

$('.observations_list').bind('scroll', function() {
        if($(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight) {
        	if($(".loadMore").is(":visible")){
	            $(".loadMore").trigger('click');
	            console.log("trigger");
        	}
        }
});

	</r:script>

</body>
</html>
