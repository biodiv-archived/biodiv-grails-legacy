<style>
.dropdown-menu{position:relative;float:none;width:auto;}
</style>
<div class="trait" data-toggle="buttons-radio">
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
	<g:if test="${fromObservationCreate}">
		<a class="dropdown-toggle btn" data-toggle="dropdown">${trait.name} <b class="caret"></b></a>
		<ul class="dropdown-menu" style="width:auto;overflow-x:hidden;overflow-y:auto;">
			<li style="text-align:center;overflow-x:hidden; overflow-y:auto;">
				<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny, 'observationCreate':true, fromSpeciesShow:false]"/>
			</li>
		</ul>	
	</g:if>

   	<g:else>
		<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}"><h6>${trait.name}</h6></a>
  		<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny]"/>
	</g:else>

	<g:if test="${fromObservationShow=='show'}">
		<div id="editFactPanel" class="trait">
			<input id="traits" name="traits" type="hidden"/>
			<input id="observation" name="observation" type="hidden" value="${observationInstance.id}" />
			<input id="facts" name="facts" type="hidden" value="${factInstance['fact']}"/>
			<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':trait.values(),'displayAny':displayAny, fromSpeciesShow:false]"/>
		</div>
			<a class="btn btn-small btn-primary" id="editFact" style="float:right;">Edit</a>
			<a class="btn btn-small btn-primary" id="cancelFact" style="float:right;">Cancel</a>
			<input type="submit" class="btn btn-small btn-primary" id="submitFact" style="float:right;" value="submit" />
	</g:if>
</div>
<asset:script>
	$(document).ready(function(){
	$('#editFactPanel').hide();
	$('#submitFact').hide();
	$('#cancelFact').hide();

	$('#editFact').click(function(){
	$('#editFactPanel').show();
	$('#submitFact').show();
	$('#cancelFact').show();
	$(this).hide();
	});

	$('#cancelFact').click(function(){
	$('#editFactPanel').hide();
	$('#submitFact').hide();
	$('#cancelFact').hide();
	$('#editFact').show();
	});

	$('#submitFact').click(function(){
	var hbt = '',trait='',selTrait={}; 
		$('.traitFilter button, .traitFilter .none, .traitFilter .any, .trait button, .trait .none, .trait .any').each(function(){
		if($(this).hasClass('active')) {
		trait = $(this).attr('data-tid');
		selTrait[trait] = $(this).attr('value');
		}
		});
		var traits = selTrait;
		var traitsStr = '';
		for(var m in traits) {
		traitsStr += m+':'+traits[m]+';';
		}
		$('#traits').val(traitsStr);
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
