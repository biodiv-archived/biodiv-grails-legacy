<%@ page import="species.participation.Observation"%>
<div>
<!--h5><g:message code="default.tagcloud.label" args="['Tag Cloud']" /></h5-->
<tc:tagCloud controller="observation" action="list" bean="${Observation}" style 
						color="${[start: '#084B91', end: '#9FBBE5']}"
						size="${[start: 12, end: 30, unit: 'px']}"
						paramName='tag'/>
</div>