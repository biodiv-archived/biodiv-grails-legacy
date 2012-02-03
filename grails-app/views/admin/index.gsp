

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'name.label', default: 'Name')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>
</head>
<body>
	<div class="container_16">
		<g:if test="${flash.message}">
			<div
				class="ui-state-highlight ui-corner-all grid_10 prefix_3 suffix_3">
				<span class="ui-icon-info" style="float: left; margin-right: .3em;"></span>
				${flash.message}
			</div>
		</g:if>

		<div class="grid_16">
			<div>
			<ul>
				<li><a href="${createLink(action:'loadData')}">Load sample data</a></li>
				<li><a href="${createLink(action:'loadNames')}">Load sample names</a></li>
				<br/>
				<li><a href="${createLink(action:'updateGroups')}">Update groups for taxon concepts</a></li>
				<li><a href="${createLink(action:'updateExternalLinks')}">Update external links for taxon concepts</a></li>
				<li><a href="${createLink(action:'reloadNames')}">Sync names and recommendations</a></li>
				
				<br/>
				<li><a href="${createLink(action:'reloadNamesIndex')}">Recreate names index</a>
				<li><a href="${createLink(action:'reloadSearchIndex')}">Recreate search index</a>			
				<li><a href="${createLink(action:'recomputeInfoRichness')}">Recompute information richness</a></li>				
			</ul>
			</div>
		</div>
	</div>
</body>
</html>
