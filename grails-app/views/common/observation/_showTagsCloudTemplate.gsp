<%@ page import="species.participation.Observation"%>
<div>
<!--h5><g:message code="default.tagcloud.label" args="['Tag Cloud']" /></h5-->
	<g:if test="${tags}">
		<tc:tagCloud tags="${tags}" action="list" sort="${true}" style 
						color="${[start: '#084B91', end: '#9FBBE5']}"
						size="${[start: 12, end: 30, unit: 'px']}"
						paramName='tag'/>
	</g:if>					
	<g:else>
		<span class="msg" style="padding-left: 50px;">No tags</span>
	</g:else>
</div>
