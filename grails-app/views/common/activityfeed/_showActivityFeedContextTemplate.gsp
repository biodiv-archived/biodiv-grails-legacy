<%@page import="species.participation.Checklist"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.Comment"%>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.Species"%>
<%@page import="species.participation.ActivityFeedService"%>

<div class="activityFeedContext thumbnails" >
	<div class="feedParentContext thumbnail clearfix">
		<g:if test="${feedInstance.rootHolderType ==  Observation.class.getCanonicalName()}" >
			<%
				def tmpUserGroup = (feedHomeObject && feedHomeObject.class.getCanonicalName() == UserGroup.class.getCanonicalName()) ? feedHomeObject : null
			%>
			<obv:showSnippet model="['observationInstance':feedParentInstance, userGroup:tmpUserGroup, userGroupWebaddress:tmpUserGroup?.webaddress]"></obv:showSnippet>
		</g:if>
		<g:elseif test="${feedInstance.rootHolderType ==  UserGroup.class.getCanonicalName()}" >
		<div class="span10">
			<table class="table" style="margin-left: 0px;">
				<thead>
					<tr>
						<th>Group</th>
						<th>Species Groups</th>
						<th>Habitats</th>
						<th>Members</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<uGroup:showSnippet model="['userGroupInstance':feedParentInstance, 'showLeave':false, showJoin:false ,'userGroupTitle':feedParentInstance.name]"></uGroup:showSnippet>
					</tr>
				</tbody>
			</table>
		</div>
		</g:elseif>
		<g:elseif test="${feedInstance.rootHolderType ==  Checklist.class.getCanonicalName()}" >
			<div class="span10">
				<clist:showSnippet model="['checklistInstance':feedParentInstance, userGroup:tmpUserGroup]"></clist:showSnippet>
			</div>	
		</g:elseif>
		<g:elseif test="${feedInstance.rootHolderType ==  Species.class.getCanonicalName()}" >
			<s:showSnippet model="['speciesInstance':feedParentInstance]" />
		</g:elseif>
		<g:else>
			${feedInstance.rootHolderType}
		</g:else>
	</div>
	<%
		def isCommentThread = (feedInstance.subRootHolderType == Comment.class.getCanonicalName() && feedInstance.rootHolderType == UserGroup.class.getCanonicalName()) 
	%>
	<g:if test="${isCommentThread}">
		<div class="feedSubParentContext ${feedInstance.fetchMainCommentFeed().subRootHolderType + feedInstance.fetchMainCommentFeed().subRootHolderId}">
		Comment thread
<%--			<comment:showCommentWithReply model="['feedInstance' : feedInstance.fetchMainCommentFeed(), 'feedPermission':feedPermission, 'showAlways':true]" />	--%>
		</div>
	</g:if>
	<feed:showAllActivityFeeds model="['rootHolder':feedParentInstance, 'isCommentThread':isCommentThread, 'subRootHolderType':feedInstance.subRootHolderType, 'subRootHolderId':feedInstance.subRootHolderId, 'feedType':ActivityFeedService.SPECIFIC, 'refreshType':ActivityFeedService.MANUAL, 'feedPermission':feedPermission]" />
</div>