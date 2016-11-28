<style>
.dropdown-menu{position:relative;float:none;width:auto;}
</style>
<div class="trait" id="trait_${trait.id}" data-toggle="buttons-radio">
	<%
		def traitValue
		def displayAny
			if(fromSpeciesShow){
				traitValue = factInstance[trait]
				displayAny = true
			}
			else{
				traitValue = trait.values()
				displayAny = false
			}
	 %>
 <% /*<g:if test="${fromList || params.action == 'show'}"> */ %>
	<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}"><h6>${trait.name}</h6></a>
  		<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny]"/>
<% /*</g:if> */%>

<% /*   	<g:else>
		<a class="dropdown-toggle btn" data-toggle="dropdown">${trait.name} <b class="caret"></b></a>
		<ul class="dropdown-menu" style="width:auto;overflow-x:hidden;overflow-y:auto;">
			<li style="text-align:center;overflow-x:hidden; overflow-y:auto;">
				<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny, fromSpeciesShow:false]"/>
			</li>
		</ul>	
	</g:else> */ %>
</div>
	<g:if test="${fromObservationShow=='show'}">
	<div class="edit_btn"  style="position:absolute;float: right;right: 0px;top:2px;">
	<a class="btn btn-small btn-primary editFact" data-id="${trait.id}" id="editFact_${trait.id}" style="float:right;display: block;">Edit</a>
			<a class="btn btn-small btn-primary cancelFact" data-id="${trait.id}" id="cancelFact_${trait.id}" style="float:right;" >Cancel</a>
			<input type="submit" class="btn btn-small btn-primary submitFact" data-id="${trait.id}" id="submitFact_${trait.id}" style="float:right;" value="Submit" />
			</div>
		<div id="editFactPanel_${trait.id}" class="editFactPanel trait">
			<input id="traits_${trait.id}" name="traits" type="hidden"/>
			<input id="observation" name="observation" type="hidden" value="${observationInstance.id}" />
			<input id="facts" name="facts" type="hidden" value="${factInstance['fact']}"/>
			<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':trait.values(),'displayAny':displayAny, fromSpeciesShow:false]"/>
		</div>
	</g:if>

<asset:script>
	$(document).ready(function(){
		var id;
	$('.editFactPanel').hide();
	$('.submitFact').hide();
	$('.cancelFact').hide();

	$('.editFact').click(function(){
	id=$(this).data("id")
	$('#editFactPanel_'+id).show();
	$('#submitFact_'+id).show();
	$('#cancelFact_'+id).show();
	$('#trait_'+id).hide();
	$(this).hide();
	});

	$('.cancelFact').click(function(){
	id=$(this).data("id")
	$('#editFactPanel_'+id).hide();
	$('#submitFact_'+id).hide();
	$('#cancelFact_'+id).hide();
	$('#editFact_'+id).show();
	$('#trait_'+id).show();
	});

	$('.submitFact').click(function(){
		id=$(this).data("id")
		var hbt = '',trait='',value='', selTrait={}; 
		var selectTrait;
			$('.traitFilter button, .traitFilter .none, .traitFilter .any, .trait button, .trait .none, .trait .any').each(function(){				
			if($(this).hasClass('active')) {
				selectedTrait = this;
				trait = $(this).attr('data-tid');
				tValue = $(this).attr('data-tvid');
			selTrait[trait] = $(this).attr('value');
			}
			});
		var traits = selTrait;
		var traitsStr = '';
		for(var m in traits) {
		traitsStr += m+':'+traits[m]+';';
		}
		$('#traits_'+id).val(traitsStr);
			var factId=$('#facts').val();
			var traits=traitsStr;
			observationId=$('#observation').val();

		$.ajax({ 
			url:'${uGroup.createLink(controller:'observation', action:'updateFact', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}',
				data:{factId:factId,  traits:traitsStr, observation:observationId},
				success: function(data, statusText, xhr, form) {

				},
			error:function (xhr, ajaxOptions, thrownError){
			console.log('error');
		}
		});

		
		$('#trait_'+id).show();
		//$('#value_btn_'+tValue).show();
		$('#editFactPanel_'+id).hide();
		$(this).hide();
		$('#cancelFact_'+id).hide();
		$('#editFact_'+id).show();
		//updateGallery();
	});
		$(document).on('click', '.trait button, .trait .all, .trait .any, .trait .none', function(){
			if($(this).hasClass('active')){
			return false;
			}
			$(this).parent().parent().find('button, .all, .any, .none').removeClass('active btn-success');
			$(this).addClass('active btn-success');
				return false;
		});
});
</asset:script>
