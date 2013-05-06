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
.paginateButtons {
	margin: 3px 0px 3px 0px;
}

.paginateButtons a {
	padding: 2px 4px 2px 4px;
	background-color: #A4A4A4;
	border: 1px solid #EEEEEE;
	text-decoration: none;
	font-size: 10pt;
	font-variant: small-caps;
	color: #EEEEEE;
}

.paginateButtons a:hover {
	text-decoration: underline;
	background-color: #888888;
	border: 1px solid #AA4444;
	color: #FFFFFF;
}
</style>
</head>
<body>
	<div class="body" style="margin-left: 20px;">
		<h1>Browse Documents</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>



		<div class="document-list tab-content span8">
			<g:render template="/document/search" model="['params':params]" />

			<g:render template="/document/documentListTemplate" />
		</div>
	</div>
	<g:render template="/document/documentSidebar" />
</body>
</html>
