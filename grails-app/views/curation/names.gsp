<%@ page import="species.ScientificName.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@page import="species.TaxonomyDefinition"%>
<html>
<head>
<meta name="layout" content="main" />
<r:require modules="species_list" />
<title><g:message code="cuation.names.species.names.list" /></title>
</head>
<body>
	<div class="span12">
		<s:showSubmenuTemplate model="['entityName':'Names']" />
		<uGroup:rightSidebar />


		<form id="namesFileForm" enctype="multipart/form-data" method="POST">
			<label><g:message code="default.file.with.scientific.name.label" /> </label> <input type="file" name="namesFile"
				placeholder="${g.message(code:'curation.names')}">
			<button type="submit" class="btn"><g:message code="button.upload.curate" /></button>
		</form>

		<table class="table table-bordered">
			<thead>
				<tr>
					<g:sortableColumn property="name"
						title="${message(code: 'commonNames.name.label', default: 'Given Name')}" />

					<th><g:message code="default.name.canonical.form.label" /></th>

					<th><g:message code="default.existing.concept.name.label" /></th>
					<th><g:message code="default.Concept.canonical.form.label" /></th>

				</tr>
			</thead>
			<tbody>
				<g:each in="${parsedNames}" status="i" var="parsedName">
					<%def taxonConcept = TaxonomyDefinition.findByCanonicalFormIlikeAndRank(parsedName.canonicalForm, TaxonomyRank.SPECIES.ordinal()) %>
					<tr
						class="${parsedName.normalizedForm && parsedName.normalizedForm.equalsIgnoreCase(taxonConcept?.normalizedForm)?:'error'}">

						<td><span title="${parsedName?.normalizedForm }">
								${fieldValue(bean: parsedName, field: "name")}
						</span></td>
						<td>
							${fieldValue(bean: parsedName, field: "canonicalForm")}
						</td>


						<td title="${taxonConcept?.normalizedForm }">
							<% def species =  taxonConcept?Species.findByTaxonConcept(taxonConcept):null%>
							<g:if test="${species}">
								<a target="_blank"
									href="${uGroup.createLink(controller:'species', action:'show', id:species.id, userGroupWebaddress:params.webaddress)}">
									${fieldValue(bean: taxonConcept, field: "name")} </a>
							</g:if> <g:else>
								${fieldValue(bean: taxonConcept, field: "name")}
							</g:else></td>
						<td>
							${fieldValue(bean: taxonConcept, field: "canonicalForm")}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</body>
</html>
