<%@ page import="content.eml.UFile"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.participation.ActivityFeedService"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'Document.label', default: 'Document')}" />
<title>Document Browser</title>
<r:require modules="add_file" />
<uploader:head />
<style type="text/css">

</style>
</head>
<body>

	<div class="span12">
		<g:render template="/document/documentSubMenuTemplate" model="['entityName':'Browse Documents']" />
		<uGroup:rightSidebar/>
		

		<div class="document-list span8 right-shadow-box" style="margin:0;">
			<g:render template="/document/search" model="['params':params]" />
			
			<obv:showObservationFilterMessage />
			
			<g:render template="/document/documentListTemplate" />
		</div>
		
			<g:render template="/document/documentSidebar" />
	</div>

</body>
</html>
