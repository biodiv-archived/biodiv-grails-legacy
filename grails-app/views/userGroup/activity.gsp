<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName" value="${userGroupInstance.name}" />
<title><g:message code="default.show.label"
		args="[userGroupInstance.name]" />
</title>
<script src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
<r:require modules="userGroups_show,comment" />
<style>
.comment-textbox {
	width: 100%;
}
</style>
</head>
<body>
	<div class="observation span12 bodymarker">
		<uGroup:showSubmenuTemplate model="['entityName':'Activity Stream']"/>
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]"/>
		<div class="userGroup-section">
			<div>
				<%
					def canPostComment = customsecurity.hasPermissionForAction([object:userGroupInstance, permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
				%>
				<comment:showAllComments
					model="['commentHolder':userGroupInstance, commentType:'super', 'canPostComment':canPostComment, 'showCommentList':false]" />
			</div>
			<feed:showFeedWithFilter model="['rootHolder':userGroupInstance, feedType:'GroupSpecific', feedPermission:'editable', feedCategory:'All','feedOrder':'latestFirst']" />
		</div>	
	</div>
</body>
</html>
