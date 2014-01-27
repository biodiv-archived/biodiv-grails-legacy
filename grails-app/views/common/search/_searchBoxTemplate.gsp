<r:script>

$(document).ready(function(){

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
		'isFlagged':"${params.isFlagged?.toBoolean()?.toString()}",
		'nameTermsUrl': "${uGroup.createLink(controller:'search', action: 'nameTerms')}",
		'noImageUrl' : "${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}",
		'dropDownIconUrl' : "${createLinkTo(file:"dropdown_active.gif", base:grailsApplication.config.speciesPortal.resources.serverURL)}",
		'IBPDomainUrl':"${Utils.getIBPServerDomain()}",
		'searchController' : "${controller}",
		'carousel':{maxHeight:150, maxWidth:150},
                'imagesPath': "${resource(dir:'images', absolute:true)}",
                'locationsUrl': "${uGroup.createLink(controller:'observation', action: 'locations')}",
                'defaultMarkerIcon':"${resource(dir:'js/Leaflet/dist/images', file:'')}",
                'isChecklistOnly':"${params.isChecklistOnly?.toBoolean()?.toString()}",
                'species':{
                    'url':"${uGroup.createLink('controller':'species', action:'show', 'userGroup':userGroupInstance)}"
                },
                'downloadFile': "${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance)}",
                'content':{
                    'url':"${uGroup.createLink('controller':'content')}"
                },
                'observation':{
                    listUrl:"${uGroup.createLink(controller:'observation', action: 'listJSON', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
                    occurrencesUrl:"${uGroup.createLink(controller:'observation', action: 'occurrences', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
                    relatedObservationsUrl:"${uGroup.createLink(controller:'observation', action: 'related', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}",
                    uploadUrl:"${g.createLink(controller:'observation', action:'upload_resource')}",
                    distinctRecoListUrl:"${uGroup.createLink(controller:'observation', action: 'distinctReco', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}",
                    speciesGroupCountListUrl:"${uGroup.createLink(controller:'observation', action: 'speciesGroupCount', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}",

                },
                'recommendation': {
                    'getRecos' : "${uGroup.createLink(controller:'recommendation', action:'getRecos', userGroup:userGroupInstance)}",
                    'suggest' : "${uGroup.createLink(controller:'recommendation', action: 'suggest', userGroup:userGroupInstance)}"
                },
                'action': {
                    'inGroupsUrl':"${uGroup.createLink(controller:'action', action: 'inGroups', userGroup:userGroupInstance)}"
                }
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
	<div id="mainSearchForm" style="position:relative">
	<form method="get"
		action="${createLink(action:'search') }"
		id="searchbox" class="form-horizontal">
		<div class="input-append">
			<input type="text" name="query" id="searchTextField" value=""
			size="26"
			placeholder="Enter your search key" />
                        <input id="search"  class="btn btn-default" type="submit"
			value="Search"/></div>

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
