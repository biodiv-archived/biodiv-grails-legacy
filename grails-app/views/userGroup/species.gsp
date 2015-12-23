
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'showobservationstoryfooter.title.species')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>

<asset:javascript src="species.js" />
</head>
<body>
	<g:include controller="species" action="list" model="['userGroup':userGroupInstance, 'params':params]"/>
	<script type="text/javascript">
		$(document).ready(function(){
			window.params.tagsLink = "${uGroup.createLink(controller:'species', action: 'tags')}";
			$('#speciesGallerySort').change(function(){
				updateGallery(window.location.pathname + window.location.search, ${params.limit?:40}, 0, undefined, false);
				return false;
			});
		});
		
	</script>
	<asset:script>
		$(document).ready(function(){
			$(".grid_view").show();
		});
		
	</asset:script>
</body>
</html>
