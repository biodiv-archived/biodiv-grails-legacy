<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@page import="species.TaxonomyDefinition"%>
<html>
<head>
<meta name="layout" content="main" />
<r:require modules="species_list" />
<title>Species names list</title>
</head>
<body>
	<div class="span12">
		<s:showSubmenuTemplate model="['entityName':'Names']" />
		<uGroup:rightSidebar />


<form id="namesFileForm"  enctype="multipart/form-data" method="POST">
<label>Please provide a simple text file with a single scientific name per line </label>
  <input type="file" name="namesFile" placeholder="Names file with <5000 names">
  <button type="submit" class="btn">Upload & Curate</button>
</form>

		<table class="table table-bordered">
			<thead>
				<tr>
					<g:sortableColumn property="name"
						title="${message(code: 'commonNames.name.label', default: 'Given Name')}" />

					<th>Given Name Canonical Form</th>

					<th>Existing Concept Name</th>
					<th>Existing Concept Canonical Form</th>

				</tr>
			</thead>
			<tbody>
				<g:each in="${parsedNames}" status="i" var="parsedName">
				<%def taxonConcept = TaxonomyDefinition.findByCanonicalFormIlikeAndRank(parsedName.canonicalForm, TaxonomyRank.SPECIES.ordinal()) %>
					<tr class="${parsedName.normalizedForm && parsedName.normalizedForm.equalsIgnoreCase(taxonConcept?.normalizedForm)?:'error'}">

						<td>
							<span title="${parsedName?.normalizedForm }">${fieldValue(bean: parsedName, field: "name")}</span>
						</td>
						<td>
							${fieldValue(bean: parsedName, field: "canonicalForm")}
						</td>
						
						<td  title="${taxonConcept?.normalizedForm }"><g:link action="show" id="${taxonConcept?.id}">
								${fieldValue(bean: taxonConcept, field: "name")}
							</g:link>
						</td>
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
