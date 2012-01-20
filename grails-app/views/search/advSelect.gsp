<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />

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

var cache = {},
		lastXhr;
	$("#searchTextField").catcomplete({
	 	 appendTo: '#mainSearchForm',
		 source:function( request, response ) {
				var term = request.term;
				if ( term in cache ) {
					response( cache[ term ] );
					return;
				}

				lastXhr = $.getJSON( "${createLink(action: 'nameTerms', controller:'search')}", request, function( data, status, xhr ) {
					cache[ term ] = data;
					if ( xhr === lastXhr ) {
						response( data );
					}
				});
			},focus: function( event, ui ) {
				$("#canName").val("");
				$( "#searchTextField" ).val( ui.item.label.replace(/<.*?>/g,"") );
				return false;
			},
			select: function( event, ui ) {
				$( "#searchTextField" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$( "#canName" ).val( ui.item.value );
				//$( "#name-description" ).html( ui.item.value ? ui.item.label.replace(/<.*?>/g,"")+" ("+ui.item.value+")" : "" );
				//ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
				return false;
			}
	}).data( "catcomplete" )._renderItem = function( ul, item ) {
			if(item.category == "General") {
				return $( "<li class='grid_4'  style='list-style:none;'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a>" + item.label + "</a>" )
					.appendTo( ul );
			} else {
				if(!item.icon) {
					item.icon =  "${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				}  
				return $( "<li class='grid_4' style='list-style:none;'></li>" )
					.data( "item.autocomplete", item )
					.append( "<img src='" + item.icon+"' class='ui-state-default icon' style='float:left' /><a>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
					.appendTo( ul );
			}
		};	
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
								name="name" id="searchTextField" class="text ui-widget-content ui-corner-all"
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

				<span class="button"> <input id="advSearch" type="submit"
					value="Search"> </span>

			</form>
		</div>
	</div>
</body>
</html>
