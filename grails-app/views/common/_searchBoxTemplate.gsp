<g:javascript>

$(document).ready(function(){
	$("#searchTextField").val('${responseHeader?.params?.q}')
	
	var cache = {},
		lastXhr;
	$("#searchTextField").autocomplete({
		 source:function( request, response ) {
				var term = request.term;
				if ( term in cache ) {
					response( cache[ term ] );
					return;
				}

				lastXhr = $.getJSON( "${createLink(action: 'terms', controller:'search')}", request, function( data, status, xhr ) {
					cache[ term ] = data;
					if ( xhr === lastXhr ) {
						response( data );
					}
				});
			}
	});
	
	$( "#search" ).click(function() {
			$( "#searchbox" ).submit();
	});
});
</g:javascript>
	<form method="get"
		action="${createLink(controller:'search', action:'select') }"
		id="searchbox" class="searchbox">
		<input type="text" name="query" id="searchTextField" value=""
			size="40" class="text ui-widget-content ui-corner-all"
			title="Enter your searck key" />

		<g:hiddenField name="start" value="0" />
		<g:hiddenField name="rows" value="10" />
		<g:hiddenField name="sort" value="score desc" />
		<g:hiddenField name="fl" value="id, name" />

		<!-- 
		<g:hiddenField name="hl" value="true" />
		<g:hiddenField name="hl.fl" value="message" />
		<g:hiddenField name="hl.snippets" value="3" />
		 -->
		<span> <input id="search"  class="searchButton" type="submit"
			value="">
			<!-- a href="${createLink(controller:'search', action:'advSelect') }" class="search">Advanced Search</a--> </span>

	</form>
