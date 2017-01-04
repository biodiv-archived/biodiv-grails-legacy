<%@page import="species.trait.Trait.TraitTypes"%>
<div class="trait  ${filterable==false?'':'filterable'}" data-id="${trait.id}">
	<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}">
        <h6>${trait.name}
            <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${trait.description}"></i>
        </h6>
    </a>

    <div class="alert alert-error" style="display:none;"></div>
  	<g:render template="/trait/showTraitValuesListTemplate" model="['trait':trait, 'traitValues':factInstance?factInstance[trait.id]:(editable?null:trait.values()), 'displayAny':displayAny, 'traitTypes':trait.traitTypes, 'queryParams':queryParams]"/>
    <g:if test="${editable}">
    <g:if test="${ifOwns || trait.isParticipatory}">
        <div style="position:absolute;float: right;right: 0px;top:2px;">
            <a class="btn btn-small btn-primary editFact" data-id="${trait.id}" style="float:right;display: block;">Edit</a>
            <a class="btn btn-small btn-primary cancelFact" data-id="${trait.id}" style="float:right;display:none;" >Cancel</a>
            <input type="submit" class="btn btn-small btn-primary submitFact" data-id="${trait.id}" data-objectId = "${object?.id}" data-objectType="${object?.class?.getCanonicalName()}" style="float:right;display:none" value="Submit" />
        </div>
        <div class="editFactPanel trait" style="display:none;">
            <g:render template="/trait/showTraitValuesListTemplate" model="['trait':trait, 'traitValues':trait.values(),'factInstance':factInstance, 'displayAny':displayAny, fromSpeciesShow:false, 'traitTypes':trait.traitTypes, 'queryParams':queryParams]"/>
        </div>
    </g:if>
    </g:if>
</div>

