<!-- TODO change this r:script which is used by resources framework for script not to be repeated multiple times -->
<g:javascript>

$(document).ready(function() {

	$.widget( "custom.catcomplete", $.ui.autocomplete, {
		_renderMenu: function( ul, items ) {
			var self = this,
				currentCategory = "";
			$.each( items, function( index, item ) {
				if ( item.category != currentCategory ) {
					ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
					currentCategory = item.category;
				}
				self._renderItem( ul, item );
			});
		}
	});
	
	
	var cache = {},
		lastXhr;
	$("#name").catcomplete({
			appendTo:"#nameContainer",
		 	source:function( request, response ) {
				var term = request.term;
				if ( term in cache ) {
					response( cache[ term ] );
					return;
				}

				lastXhr = $.getJSON( "${createLink(controller:'recommendation', action: 'suggest')}", request, function( data, status, xhr ) {
					cache[ term ] = data;
					if ( xhr === lastXhr ) {
						response( data );
					}
				});
			},focus: function( event, ui ) {
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				return false;
			},
			select: function( event, ui ) {
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$( "#name-id" ).val( ui.item.value );
				$( "#name-description" ).html( ui.item.desc ? ui.item.desc : "" );
				ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
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
					item.icon =  "${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				}  
				return $( "<li class='grid_4' style='list-style:none;'></li>" )
					.data( "item.autocomplete", item )
					.append( "<img src='" + item.icon+"' class='ui-state-default icon' style='float:left' /><a>" + item.label + "<br>(" + item.desc + ")</a>" )
					.appendTo( ul );
			}
		};
	
});

	
</g:javascript>

<div class="recommendation">
	<div
		class="value ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')}">
	</div>
	<div>
		<div id="nameContainer" class="grid_4 prop">
			<label class="name">Suggest Name </label> <input type="text"
				name="query" name="name" id="name"
				value="${recommendationInstance?.name}" size="40"
				class="grid_4 value text ui-widget-content ui-corner-all" /> <input
				type="hidden" id="name-id" />
		</div>
		<br />
		<div class="grid_16">
			<img id="name-icon" src="" class="ui-state-default icon"
				style="float: left; display: none;" />
			<p id="name-description"></p>
		</div>
	</div>
</div>