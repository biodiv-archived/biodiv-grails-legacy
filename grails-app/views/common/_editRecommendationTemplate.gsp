<%@ page import="species.participation.RecommendationVote.ConfidenceType" %>
<!-- TODO change this r:script which is used by resources framework for script not to be repeated multiple times -->
<g:javascript>

$(document).ready(function() {

	//TODO : global variables ... may be problematic
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
				$("#canName").val("");
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				return false;
			},
			select: function( event, ui ) {
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$( "#canName" ).val( ui.item.value );
				//$( "#name-description" ).html( ui.item.value ? ui.item.label.replace(/<.*?>/g,"")+" ("+ui.item.value+")" : "" );
				//ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
				return false;
			}
	}).data( "catcomplete" )._renderItem = function( ul, item ) {
			if(item.category == "General") {
				return $( "<li class='grid_4'  style='display:inline-block;width:220px;clear:none;padding:5px;'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a style='height:50px;padding-left:60px;border:0;'>" + item.label + "</a>" )
					.appendTo( ul );
			} else {
				if(!item.icon) {
					item.icon =  "${resource(dir:'images',file:'no-image.jpg', absolute:true)}"
                                            //${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				}  
				return $( "<li class='grid_4' style='display:inline-block;width:220px;clear:none;padding:5px;'></li>" )
					.data( "item.autocomplete", item )
					.append( "<img src='" + item.icon+"' class='ui-state-default' style='float:left' /><a style='height:50px;padding-left:60px;border:0;'>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
					.appendTo( ul );
			}
		};
		
		//$("#name").ajaxStart(function(){
		//	var offset = $(this).offset();  				
   		//	$("#spinner").css({left:offset.left+$(this).width(), top:offset.top-6}).show();
   		//	return false;
 		//});	
	
});

	
</g:javascript>

<style>
#nameContainer ul {
width:690px;
}
</style>

<div
	id="nameContainer" class="recommendation">
	<%
		def species_name = ""
		//showing vote added by creator of the observation
		if(params.action == 'edit' || params.action == 'update'){
			species_name = observationInstance?.fetchOwnerRecoVote()?.recommendation?.name
		}else{
			//showing identified species name based on max vote
			species_name = observationInstance?.maxVotedSpeciesName
		}
	%>
	<input type="text" name="recoName" id="name"
		value="${species_name}"
		class="value text ui-widget-content ui-corner-all ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" /> 
	
	<input type="hidden" name="canName" id="canName" />
</div>
