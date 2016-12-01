<%@page import="species.trait.Trait.TraitTypes"%>
<style>
.dropdown-menu{position:relative;float:none;width:auto;}
.tooltip{position:fixed; z-index:9999999999;}

</style>
<div class="trait" id="trait_${trait.id}" data-toggle="buttons-radio">
	<%
		def traitValue
		def displayAny
			if(fromSpeciesShow){
				traitValue = factInstance[trait.id]
				displayAny = true
			}
			else{
				traitValue = trait.values()
				displayAny = false
			}
	 %>
	<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}">
        <h6>${trait.name}
            <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${trait.description}"></i>
        </h6>
    </a>

  	<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny, 'traitTypes':trait.traitTypes]"/>

    <g:if test="${fromObservationShow == 'show'}">
        <div id="edit_btn_${trait.id}"  style="position:absolute;float: right;right: 0px;top:2px;">
            <a class="btn btn-small btn-primary editFact" data-id="${trait.id}" id="editFact_${trait.id}" style="float:right;display: block;">Edit</a>
            <a class="btn btn-small btn-primary cancelFact" data-id="${trait.id}" id="cancelFact_${trait.id}" style="float:right;display:none;" >Cancel</a>
            <input type="submit" class="btn btn-small btn-primary submitFact" data-id="${trait.id}" id="submitFact_${trait.id}" style="float:right;display:none" value="Submit" />
        </div>
        <div id="editFactPanel_${trait.id}" class="editFactPanel trait" style="display:none;">
            <g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':trait.values(),'factInstance':factInstance[trait.id], 'displayAny':displayAny, fromSpeciesShow:false, 'traitTypes':trait.traitTypes]"/>
        </div>
    </g:if>
</div>

