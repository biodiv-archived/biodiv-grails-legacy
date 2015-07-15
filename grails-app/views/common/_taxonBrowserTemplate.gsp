<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@ page import="species.Species"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<g:if test="${classifications}">
<select name="taxaHierarchy" class="value ui-corner-all" style="margin-bottom:0px;width:100%;background-color:whitesmoke;">
    <g:each in="${classifications}" var="classification">
    <option value="${classification[0]}" ${(classification[1].id==selectedClassification || (!selectedClassification && (grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY).equalsIgnoreCase(classification[1].name)))?'selected':''}>
    ${classification[1].name} ${classification[2]?classification[2].toString():''}
    </option>
    </g:each>
</select>
<div class="attributionBlock">
    <span class="ui-icon-info ui-icon-control " title="${g.message(code:'title.show.details')}"
        style="position: absolute; top: 0; right: 0; margin: 10px;"></span>
    <div class="ui-corner-all toolbarIconContent attribution"
        style="display: none;">
        <a class="ui-icon ui-icon-close" style="float: right;"></a> <span
            id="cInfo"></span>
        <g:each in="${classifications}" var="classification">
        <p id="c-${classification[1].id}" style="display: none;">
        ${classification[1].citation?:classification[2].toString()}

        </p>
        </g:each>

    </div>
</div>
 <table id="taxonHierarchy" class="emptyField"></table>
 </g:if>
<g:else>
<table id="taxonHierarchy" class="emptyField" style="width: 938px;"></table>
</g:else>
<g:if test="${queryParams && queryParams.taxon instanceof TaxonomyDefinition}">
<input type="hidden" id="taxon" name="taxon" value="${queryParams.taxon.id}"></input>
</g:if>
<g:elseif test="${queryParams && queryParams.taxon instanceof Long}">
<input type="hidden" id="taxon" name="taxon" value="${queryParams.taxon}"></input>
</g:elseif>
<g:else>
<input type="hidden" id="taxon" name="taxon" value=""></input>
</g:else>

