<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Taxonomy Browser</title>

<r:require modules="species_show"/>

</head>
<body>
	<div class="container_12 big_wrapper outer_wrapper">
		<s:showSubmenuTemplate model="['entityName':'Taxonomy Browser']"/>
		
		<div class="grid_12">
			<t:showTaxonBrowser model="['expandAll':false]"/>
		</div>
	</div>

</body>
</html>
