<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="title" value="Activity Stream - ${userGroupInstance.name} "/>
<%def imagePath = userGroupInstance.mainImage()?.fileName;%>
<g:set var="description" value="${userGroupInstance.description.replaceAll(/<.*?>/, '').trim() }" />
<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':'', 'imagePath':imagePath]"/>
<title>${title} | ${params.controller.capitalize()} | ${Utils.getDomainName(request)}</title>
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
