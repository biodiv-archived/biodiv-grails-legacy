<%@ page
	import="species.participation.RecommendationVote.ConfidenceType"%>
<!-- TODO change this r:script which is used by resources framework for script not to be repeated multiple times -->
<g:javascript>

$(document).ready(function() {

	//TODO : global variables ... may be problematic
	var cache = {},
		lastXhr;
	$("#name").catcomplete({
			appendTo:"#nameSuggestions",
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
				$("#nameSuggestions li a").css('border', 0);
				return false;
			},
			select: function( event, ui ) {
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				$( "#canName" ).val( ui.item.value );
				//$( "#name-description" ).html( ui.item.value ? ui.item.label.replace(/<.*?>/g,"")+" ("+ui.item.value+")" : "" );
				//ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
				return false;
			},open: function(event, ui) {
				$("#nameSuggestions ul").removeAttr('style').css({'display': 'block'}); 
			}
	}).data( "catcomplete" )._renderItem = function( ul, item ) {
			ul.removeClass().addClass("dropdown-menu")
			if(item.category == "General") {
				return $( "<li class='span3'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a>" + item.label + "</a>" )
					.appendTo( ul );
			} else {
				if(!item.icon) {
					item.icon =  "${resource(dir:'images',file:'no-image.jpg', absolute:true)}"
                                            //${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				}  
				return $( "<li class='span3'></li>" )
					.data( "item.autocomplete", item )
					.append( "<a title='"+item.label.replace(/<.*?>/g,"")+"'><img src='" + item.icon+"' class='group_icon' style='float:left; background:url(" + item.icon+" no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;'/>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
					.appendTo( ul );
			}
		};
});
</g:javascript>


<div class="btn-group">
	<%
		def species_name = ""
		//showing vote added by creator of the observation
		if(params.action == 'edit' || params.action == 'update'){
			species_name = observationInstance?.fetchOwnerRecoVote()?.recommendation?.name
		}else{
			//showing identified species name based on max vote
			//species_name = observationInstance?.maxVotedSpeciesName
		}
	%>
	<input type="text" name="recoName" id="name" value="${species_name}"
		placeholder='Suggest a species name'
		class="input-xlarge ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" />

	<input type="hidden" name="canName" id="canName" />

	<div id="nameSuggestions" style="display: block;"></div>
	<div>
		<a id="reco-action" data-toggle="dropdown" href="#">Comment</a>

		<div id="reco-options" style="display: none">
			<input type="text" name="recoComment" id="recoComment"
				placeholder="Write comment" style="width: 80%"></input><br /> <input
				class="btn btn-mini" style="top:5px;" type="button" value="cancel"
				onclick="cancelRecoComment();return false;"></input>
		</div>
	</div>
</div>
<script>
	$(document).ready(function() {
		$('#recoComment').val('');

		$('#reco-action').click(function() {
			$('#reco-options').show();
			$('#reco-action').hide();
		});
	});

	function cancelRecoComment(){
		$('#recoComment').val('');
		$('#reco-options').hide();
		$('#reco-action').show();
	}
	
</script>
<style>
#reco-options {
	background-clip: padding-box;
	background-color: #FFFFFF;
	border-color: rgba(0, 0, 0, 0.2);
	border-radius: 0 0 5px 5px;
	border-style: solid;
	border-width: 1px;
	box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
	display: none;
	float: left;
	left: 0;
	list-style: none outside none;
	margin: 0;
	min-width: 400px;
	max-width: 400px;
	width : 400px;
	padding: 10px;
	top: 100%;
	z-index: 1000;
	color: #000000;
}

#reco-close {
	position: absolute;
	top: 0;
	right: 0;
}
</style>
