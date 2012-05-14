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
	href="${resource(dir:'js/jquery/jquery.jqGrid-4.1.2/css',file:'ui.jqgrid.css')}" />

<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/i18n/grid.locale-en.js"/>
<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/jquery.jqGrid.src.js"/>
<g:javascript src="jquery/jquery.jqDock-1.8/jquery.jqDock.min.js"/>
<g:javascript src="species/species.js"/>

</head>
<body>
	<div class="container_12 big_wrapper outer_wrapper">
		<div class="page-header clearfix">
			<div class="span8">
				<h1>
					<g:message code="default.taxonBrowser.heading" default="Taxonomy Browser" />
				</h1>
			</div>
			<div style="float: right;">
				<g:searchBox />
			</div>
		</div>

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>


		<div class="grid_12">
			<t:showTaxonBrowser model="['expandAll':false]"/>
		</div>
	</div>

</body>
</html>
