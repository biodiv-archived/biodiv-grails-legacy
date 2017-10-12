<%@page import="species.trait.Trait.TraitTypes"%>
<div class="trait  ${filterable==false?'':'filterable'}" data-id="${traitInstance.id}">
	<a href="${uGroup.createLink(action:'show', controller:'trait', id:traitInstance.id)}">
        <h6>${traitInstance.name}
            <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${traitInstance.description}"></i>
        </h6>
    </a>
    <div class="alert alert-error" style="display:none;"></div>
  	<g:render template="/trait/showTraitValuesListTemplate" model="['traitInstance':traitInstance, 'traitValues':factInstance?factInstance[traitInstance.id]:(editable?null:traitInstance.values()), 'displayAny':displayAny, 'traitTypes':traitInstance.traitTypes, 'queryParams':queryParams, 'numericTraitMinMax':numericTraitMinMax.find{it.id == traitInstance.id}]"/>
    <g:if test="${editable}">
    <g:if test="${ifOwns || traitInstance.isParticipatory}">
        <div style="position:absolute;float: right;right: 0px;top:2px;">
            <a class="btn btn-small btn-primary editFact" data-id="${traitInstance.id}" style="float:right;display: block;">Edit</a>
            <a class="btn btn-small btn-primary cancelFact" data-id="${traitInstance.id}" style="float:right;display:none;" >Cancel</a>
            <input type="submit" class="btn btn-small btn-primary submitFact" data-id="${traitInstance.id}" data-objectId = "${object?.id}" data-objectType="${object?.class?.getCanonicalName()}" style="float:right;display:none" value="Submit" />
        </div>
        <div class="editFactPanel trait" style="display:none;">
            <g:render template="/trait/showTraitValuesListTemplate" model="['traitInstance':traitInstance, 'traitValues':traitInstance.values(),'factInstance':factInstance, 'displayAny':displayAny, fromSpeciesShow:false, 'traitTypes':traitInstance.traitTypes, 'queryParams':queryParams,  'numericTraitMinMax':numericTraitMinMax.find{it.id == traitInstance.id}]"/>
        </div>
    </g:if>
    </g:if>
</div>

