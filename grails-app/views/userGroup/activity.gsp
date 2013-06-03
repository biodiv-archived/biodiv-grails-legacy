<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="title" value="Activity"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
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
				<comment:showAllComments
					model="['commentHolder':userGroupInstance, commentType:'super', 'showCommentList':false]" />
			</div>
			<feed:showFeedWithFilter model="['rootHolder':userGroupInstance, feedType:'GroupSpecific', feedPermission:'editable', feedCategory:'All','feedOrder':'latestFirst']" />
		</div>	
	</div>
</body>
</html>
