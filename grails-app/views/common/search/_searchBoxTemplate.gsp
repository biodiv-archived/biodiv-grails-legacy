<div class="input-append">
	<form method="get"
		action="${uGroup.createLink(controller:params.controller, action:'search') }"
		id="searchbox" class="navbar-search" style="float: none;">

		<input type="text" name="query" id="searchTextField"
			value="${(queryParams?.query)?:((queryParams?.q)?:params.query)}"
			class="search-query span3" placeholder="Search" />


		<g:hiddenField name="offset" value="0" />
		<g:hiddenField name="max" value="10" />
		<g:hiddenField id="searchBoxSort" name="sort" value="score" />
		<input type="hidden" name="fl" value="id" />
		<g:hiddenField name="category" value="${params.controller}" />

		<select id="userGroupSelectFilter" class="btn" name="uGroup">	
			<option value="ALL"> Search in all groups </option>
			<option value="THIS_GROUP"> Search within this group </option>
		</select>


		<!-- 
		<g:hiddenField name="hl" value="true" />
		<g:hiddenField name="hl.fl" value="message" />
		<g:hiddenField name="hl.snippets" value="3" />
		 -->

	</form>
	<div id="nameSuggestionsMain" class="dropdown">
		<a class="dropdown-toggle" role="button" data-toggle="dropdown"
			data-target="#" href="#"></a>
	</div>
</div>
<r:script>

$(document).ready(function(){

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

				lastXhr = $.getJSON( "${uGroup.createLink(controller:'search', action: 'nameTerms')}", request, function( data, status, xhr ) {
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
				if( ui.item.category == 'Names' && ui.item.value != 'null') {
					if(ui.item.value != 'null') {
						$( "#searchTextField" ).val( 'canonical_name:"'+ui.item.value+'" '+ui.item.label.replace(/<.*?>/g,'') );
					}
				} else {
					$( "#searchTextField" ).val( ui.item.label.replace(/<.*?>/g,'') );
				}
				
				if(ui.item.category == 'Species Pages') {
					$("#category").val('species');
				} else if(ui.item.category == 'Observations') {
					$("#category").val('observation');
				} else if(ui.item.category == 'Groups') {
					$("#category").val('group');
				} else if(ui.item.category == 'Members') {
					$("#category").val('SUser');
				} else if(ui.item.category == 'Pages') {
					$("#category").val('newsletter');
				} else {
					$("#category").val('species');
				}
				$( "#canName" ).val( ui.item.value );

				//$( "#name-description" ).html( ui.item.value ? ui.item.label.replace(/<.*?>/g,"")+" ("+ui.item.value+")" : "" );
				//ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
				$( "#search" ).click();
				return false;
			},open: function(event, ui) {
				$("#nameSuggestionsMain ul").removeAttr('style').addClass('dropdown-menu');
				$("#nameSuggestionsMain .dropdown-toggle").dropdown('toggle');				
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
	$("#userGroupSelectFilter").val("${queryParams.uGroup?:(params.webaddress?'THIS_GROUP':'ALL')}");

</r:script>