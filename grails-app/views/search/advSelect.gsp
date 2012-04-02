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
		if(field != 'name') {
			$(this).autocomplete({			
				appentTO:"#advSearchForm",
			 	source:'${createLink(action: 'terms', controller:'search')}'+'?field='+field
			});
		}
	});

var cache = {},
		lastXhr;
	$("#advSearchTextField").catcomplete({
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
				$( "#advSearchTextField" ).val( ui.item.label.replace(/<.*?>/g,"") );
				return false;
			},
			select: function( event, ui ) {
				$( "#advSearchTextField" ).val(  'canonical_name:"'+ui.item.value+'" '+ui.item.label.replace(/<.*?>/g,'' ));
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

	<div class="container outer-wrapper">

		<div id="advSearchForm">

			<div class="page-header" style="overflow: auto;">
				<div class="span8">
					<h1>Advanced Search</h1>
				</div>
			</div>
			
			
			<form method="get" class="form-horizontal"
				action="${createLink(controller:'search', action:'advSelect') }"
				title="Advanced Search" id="advSearchBox" class="searchbox dialog">

				<div class="control-group">
					<label class="control-label" for="name">Name</label>
					<div class="controls">
						<input type="text" class="input-xlarge" name="name"
							id="advSearchTextField" placeholder="Search using species name" />
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="taxon">Taxon Hierarchy</label>
					<div class="controls">
					<input type="text" class="input-xlarge"
								name="taxon" value=""
								placeholder="Search using taxon hierarchy" />
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="author">Species Author</label>
					<div class="controls">
					<input type="text"
								name="author" class="input-xlarge"
								placeholder="Search using species author or basionym author" />
					
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="year">Year</label>
					<div class="controls">
					<input type="text" class="input-xlarge"
								name="year" 
								placeholder="Search using year of finding the species and basionym year" />
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="text">Content</label>
					<div class="controls">
					<input type="text" class="input-xlarge"
								name="text" value=""
								placeholder="Search all text content" />
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="contributor">Contributor</label>
					<div class="controls">
					<input type="text" class="input-xlarge"
								name="contributor" value=""
								placeholder="Field to search all contributors" />
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="attribution">Attributions</label>
					<div class="controls">
					<input type="text" class="input-xlarge"
								name="attribution" value=""
								placeholder="Field to search all attributions" />
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="reference">References</label>
					<div class="controls">
					<input type="text" class="input-xlarge"
								name="reference" value=""
								placeholder="Field to search all references" />
					</div>
				</div>

				<div class="form-actions">
					<button type="submit"  id="advSearch" class="btn btn-primary">Search</button>
				</div>
				<g:hiddenField name="start" value="0" />
				<g:hiddenField name="rows" value="10" />
				<g:hiddenField name="sort" value="score desc" />
				<g:hiddenField name="fl" value="id, name" />

			</form>
		</div>
	</div>
</body>
</html>
