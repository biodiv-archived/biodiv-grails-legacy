<%@ page import="content.Project"%>

<div style="width:235px;">
	<h3><g:message code="heading.project.tags" /> </h3>
	<g:if test="${tags}">
		<tc:tagCloud bean="${Project}" controller="project" action="list" sort="${true}" 
						color="${[start: '#084B91', end: '#9FBBE5']}"
						size="${[start: 12, end: 30, unit: 'px']}"
						paramName='tag'/>
						
		<span class="pull-right"><g:message code="link.more.tags" /> </span>
	</g:if>					
	<g:else>
		<span class="msg" style="padding-left: 50px;"><g:message code="link.no.tags" /></span>
	</g:else>
</div>
