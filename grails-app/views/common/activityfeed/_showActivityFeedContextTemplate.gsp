<%@page import="species.participation.Checklists"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.Discussion"%>
<%@page import="species.participation.Comment"%>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.Species"%>
<%@page import="species.participation.ActivityFeedService"%>
<%@page import="content.eml.Document"%>

<div class="activityFeedContext thumbnails" >
		<div class="feedParentContext thumbnail clearfix">
		<g:if test="${feedInstance.rootHolderType ==  Discussion.class.getCanonicalName()}">
			<g:render template="/discussion/showDiscussionSnippetTemplate" model="['discussionInstance':feedParentInstance]"/>
		</g:if> 
		<g:elseif test="${feedInstance.rootHolderType ==  Observation.class.getCanonicalName() || feedInstance.rootHolderType ==  Checklists.class.getCanonicalName() }" >
			<%
				def tmpUserGroup = (feedHomeObject && feedHomeObject.class.getCanonicalName() == UserGroup.class.getCanonicalName()) ? feedHomeObject : null
			%>
			<obv:showSnippet model="['observationInstance':feedParentInstance, userGroup:tmpUserGroup, userGroupWebaddress:tmpUserGroup?.webaddress]"></obv:showSnippet>
		</g:elseif>
		<g:elseif test="${feedInstance.rootHolderType ==  UserGroup.class.getCanonicalName()}" >
		<div class="span10">
			<table class="table" style="margin-left: 0px;">
				<thead>
					<tr>
						<th style="width:20%"><g:message code="default.group.label" /></th>
						<th style="width:20%"><g:message code="default.name.label" /></th>
						<th><g:message code="default.species.groups.label" /> </th>
						<th><g:message code="default.habitats.label" /></th>
						<th><g:message code="default.members.label" /></th>
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
		<g:elseif test="${feedInstance.rootHolderType ==  Species.class.getCanonicalName()}" >
			<s:showSnippet model="['speciesInstance':feedParentInstance]" />
		</g:elseif>
		<g:elseif test="${feedInstance.rootHolderType ==  Document.class.getCanonicalName()}" >
			<g:render template="/document/showDocumentSnippetTemplate" model="['documentInstance':feedParentInstance]"/>
		</g:elseif>
		<g:else>
			${feedInstance.rootHolderType}
		</g:else>
	</div>
	<feed:showAllActivityFeeds model="['rootHolder':feedParentInstance, 'subRootHolderType':feedInstance.subRootHolderType, 'subRootHolderId':feedInstance.subRootHolderId, 'feedType':ActivityFeedService.SPECIFIC, 'refreshType':ActivityFeedService.MANUAL, 'feedPermission':feedPermission]" />
</div>
