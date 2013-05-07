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
<r:require modules="core" />
<uploader:head />
<style type="text/css">

</style>
</head>
<body>
	<div class="page-header" style="margin-left: 20px;">
		<h1>Browse Documents</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>



		<div class="document-list tab-content span8 right-shadow-box" style="padding-right:5px;">
			<g:render template="/document/search" model="['params':params]" />

			<g:render template="/document/documentListTemplate" />
		</div>
	</div>
	<g:render template="/document/documentSidebar" />
</body>
</html>
