<%@ page import="species.participation.Discussion"%>

<div id="project-sidebar" class="span4">
    <g:if test="${params.action == 'list'}">
    	<uGroup:objectPostToGroupsWrapper 
			model="[canPullResource:canPullResource, 'objectType':Discussion.class.canonicalName]" />
    </g:if>
  	<g:if test="${!discussionInstance}">
      	<%
				params.offset = 0	
		%>
		<div class="sidebar_section" style="overflow:hidden">
			<h5><g:message code="discussionsidebar.discussion.tags" /></h5>
			<project:showTagsCloud model="[tagType:'discussion', showMoreTagPageLink:uGroup.createLink(controller:'discussion', action:'tagcloud', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)]"></project:showTagsCloud>
		</div>
    </g:if>
</div>

