<r:script>

$(document).ready(function(){

	if('${responseHeader?.params?.q}'){
		$("#searchTextField").val('${responseHeader?.params?.q}')
	} else {
		$("#searchTextField").val('${params.query}');
	}
	
	var cache = {},
		lastXhr;
	$("#searchTextField").catcomplete({
	 	 appendTo: '#nameSuggestionsMain',
		 source:function( request, response ) {
				var term = request.term;
				if ( term in cache ) {
					response( cache[ term ] );
					return;
				}

				lastXhr = $.getJSON( "${createLink(action: 'nameTerms')}", request, function( data, status, xhr ) {
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
				if( ui.item.category == 'Names') {
					$( "#searchTextField" ).val( 'canonical_name:"'+ui.item.value+'" '+ui.item.label.replace(/<.*?>/g,'') );
				} else {
					$( "#searchTextField" ).val( ui.item.label.replace(/<.*?>/g,'') );
				}
				$( "#canName" ).val( ui.item.value );
				//$( "#name-description" ).html( ui.item.value ? ui.item.label.replace(/<.*?>/g,"")+" ("+ui.item.value+")" : "" );
				//ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
				$( "#search" ).click();
				return false;
			},open: function(event, ui) {
				$("#nameSuggestionsMain ul").removeAttr('style').css({'display': 'block'}); 
			}
	}).data( "catcomplete" )._renderItem = function( ul, item ) {
			ul.removeClass().addClass("dropdown-menu")
			if(item.category != "Names") {
				return $( "<li class='span3'  style='list-style:none;'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a>" + item.label + "</a>" )
					.appendTo( ul );
			} else {
				if(!item.icon) {
					item.icon =  "${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				}  
				return $( "<li class='span3' style='list-style:none;'></li>" )
					.data( "item.autocomplete", item )
					//.append( "<img class='group_icon' style='float:left; background:url(" + item.icon+" no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;' class='ui-state-default icon'/><a>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
					.append( "<a title='"+item.label.replace(/<.*?>/g,"")+"'><img src='" + item.icon+"' class='group_icon' style='float:left; background:url(" + item.icon+" no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;'/>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
					.appendTo( ul );
			}
		};;
});
$( "#search" ).click(function() {
	$( "#searchbox" ).submit();
});
</r:script>
<div id="mainSearchForm" class="dropdown pull-left">
	<form method="get"
		action="${createLink(action:'search') }"
		id="searchbox" class="navbar-search"  style="margin-top:0px;">
		<div class="input-append">
			<input type="text" name="query" id="searchTextField" value=""
				class="search-query span3"
				placeholder="Search" />
            <input id="search"  class="btn btn-default" type="submit"
				value="Search" style="display:none;"/></div>

		<g:hiddenField name="start" value="0" />
		<g:hiddenField name="rows" value="10" />
		<g:hiddenField id="searchBoxSort"  name="sort" value="score" />
		<g:hiddenField name="fl" value="id,name" />

		<!-- 
		<g:hiddenField name="hl" value="true" />
		<g:hiddenField name="hl.fl" value="message" />
		<g:hiddenField name="hl.snippets" value="3" />
		 -->
		
	</form>
	<div id="nameSuggestionsMain" style="display:block;"></div>
</div>
