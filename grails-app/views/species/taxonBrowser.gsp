<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Taxonomy Browser</title>

<link rel="stylesheet" type="text/css" media="screen"
	href="${resource(dir:'js/jquery/jquery.jqGrid-4.1.2/css',file:'ui.jqgrid.css', absolute:true)}" />

<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/i18n/grid.locale-en.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/jquery.jqGrid.src.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="jquery/jquery.jqDock-1.8/jquery.jqDock.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="species/species.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

</head>
<body>
	<div class="container_12">

		<div class="grid_12">
			<t:showTaxonBrowser model="['expandAll':false]"/>
		</div>
	</div>

</body>
</html>
