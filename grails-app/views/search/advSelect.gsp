<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require module="jquery-ui"/>
<title>Advance Search</title>

<g:javascript>

$(document).ready(function(){
	$( "#advSearch" ).button().click(function() {
			$( "#advSearchBox" ).submit();
	});
	
	$('#advSearchBox :input').each(function(index, ele) {
		var field = $(this).attr('name');
		$(this).autocomplete({			
			appentTO:"#advSearchForm",
		 	source:'${createLink(action: 'terms', controller:'search')}'+'?field='+field
		});
	});
	
});
</g:javascript>
</head>
<body>

	<div class="container_12">

		<div class="grid_12" id="advSearchForm">

			<form method="get"
				action="${createLink(controller:'search', action:'advSelect') }"
				title="Advanced Search" id="advSearchBox" class="searchbox dialog">
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name">Name</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="name" class="text ui-widget-content ui-corner-all"
								title="Field for searching using a taxon name" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Taxon Hierarchy</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="taxon" value=""
								class="text ui-widget-content ui-corner-all"
								title="Field for searching taxon hierarchy" /></td>
						</tr>
						<tr class="prop indent">
							<td valign="top" class="name">Species Author</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="author" class="text ui-widget-content ui-corner-all"
								title="Field for searching using species author and basionym author" />
							</td>
						</tr>
						<tr class="prop indent">
							<td valign="top" class="name">Year</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="year" class="text ui-widget-content ui-corner-all"
								title="Field for searching using year of finding the species and basionym year" />
							</td>
						</tr>						
						<tr class="prop">
							<td valign="top" class="name">Content</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="text" value=""
								class="text ui-widget-content ui-corner-all"
								title="Field to search all text content" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Contributor</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="contributor" value=""
								class="text ui-widget-content ui-corner-all"
								title="Field to search all contributors" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Attributions</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="attribution" value=""
								class="text ui-widget-content ui-corner-all"
								title="Field to search all attributions" /></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">References</td>
							<td valign="top" class="value"><input type="text" size="40"
								name="reference" value=""
								class="text ui-widget-content ui-corner-all"
								title="Field to search all references" /></td>
						</tr>
					</tbody>
				</table>

				<g:hiddenField name="start" value="0" />
				<g:hiddenField name="rows" value="10" />
				<g:hiddenField name="sort" value="score desc" />
				<g:hiddenField name="fl" value="id, name" />

				<div style="float: right"> <input id="advSearch" type="submit"
					value="Search"> </div>

			</form>
		</div>
	</div>
</body>
</html>
