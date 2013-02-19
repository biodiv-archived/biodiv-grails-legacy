<%@page import="species.utils.Utils"%>
<button id="searchToggle" class="btn btn-link" type="button" style="${((queryParams?.query)?:((queryParams?.q)?:params.query))?'display:none;':''}"><i class="icon-search"></i></button>

<div id='searchToggleBox' class="input-append" style="${((queryParams?.query)?:((queryParams?.q)?:params.query))?'':'display:none;'}">
	<form method="get"
		action="${uGroup.createLink(controller:controller, action:'search') }"
		id="searchbox" class="navbar-search" style="float: none;">
		<select id="userGroupSelectFilter" class="btn" name="uGroup" style="display:none;">	
			<option value="ALL"> Search in all groups </option>
			<g:if test="${params.webaddress }">
				<option value="THIS_GROUP"> Search within this group </option>
			</g:if>
		</select>
		
		<input type="text" name="query" id="searchTextField"
			value="${((queryParams?.query)?:((queryParams?.q)?:params.query))?.encodeAsHTML()}"
			class="search-query span3" placeholder="Search" />
		
		<button id="search" class="btn btn-link" type="button"><i class="icon-search"></i></button>
		<input type="hidden" name="fl" value="id" />
		<g:hiddenField name="category" value="${controller}" />
		
<%--		<g:hiddenField name="offset" value="0" />--%>
<%--		<g:hiddenField name="max" value="10" />--%>
<%--		<g:hiddenField id="searchBoxSort" name="sort" value="score" />--%>
<%--		<g:hiddenField name="hl" value="true" />--%>
<%--		<g:hiddenField name="hl.fl" value="message" />--%>
<%--		<g:hiddenField name="hl.snippets" value="3" />--%>

	</form>
<div id="nameSuggestionsMain" class="dropdown span3" style="left:-20px;">
			<a class="dropdown-toggle" role="button" data-toggle="dropdown"
			data-target="#" href="#"></a>
		</div>	
</div>
<g:javascript>
$(document).ready(function() {
	window.params = {
		'offset':"${params.offset}",
		'isGalleryUpdate':'true',	
		"queryParamsMax":"${queryParams?.max}",
		'speciesName':"${params.speciesName }",
		'isFlagged':"${params.isFlagged }"
	}
});

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
				/*if( ui.item.category == 'Names' && ui.item.value != null) {
					console.log(ui.item.value);
					if(ui.item.value != 'null') {
						$( "#searchTextField" ).val( 'canonical_name:"'+ui.item.value+'" '+ui.item.label.replace(/<.*?>/g,'') );
					}
				} else {*/
					$( "#searchTextField" ).val( ui.item.label.replace(/<.*?>/g,'') );
				//}
				
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
		};


	$("#search").click(function() {
		if($('#searchTextField').val()) {
			$( "#searchbox" ).submit();
		}
	});
	$( "#searchbox" ).submit(function() {
		var action = $( "#searchbox" ).attr('action');
		var category = $("#category").val();
		if(category) {
			action = action.replace("${controller}", category);
		}
		
		if($("#userGroupSelectFilter").val() == 'ALL') {
			action = "${Utils.getIBPServerDomain()}"+action;
		}
		
		updateGallery(action, undefined, undefined, undefined, false,undefined,undefined,true);
    	return false;
	});
	$("#userGroupSelectFilter").val("${(queryParams && queryParams.uGroup)?queryParams.uGroup:(params.webaddress?'THIS_GROUP':'ALL')}");
	
	$("#searchToggle").click(function() {
		$(this).hide();		
		$('#searchToggleBox').slideToggle();
	});
});
</g:javascript>