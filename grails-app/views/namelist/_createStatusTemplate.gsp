<%@page import="species.ScientificName.TaxonomyRank"%>
<div class="control-group">
    <label class="control-label ${isPopup?'span3':''}">Status
    
<i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.status.info')}"></i>
    </label>
    <select id="statusDropDown" name="status" class="status ${isPopup?'span3':''}">
        <option value="chooseNameStatus">Choose Name Status</option>
        <g:each in="${NameStatus.list()}" var="ns">
        <g:if test="${ns != NameStatus.PROV_ACCEPTED && ns != NameStatus.COMMON}">
        <option value="${ns.toString().toLowerCase()}">${ns.value()}</option>
        </g:if>
        </g:each>
    </select>
</div>

<g:if test="${isPopup}">
    <div class="synToAccWrap control-group" style="display:none;">
    <label class="control-label"> New accepted Name </label>
    <input id="page" 
        data-provide="typeahead" type="text" class="taxonRank" style=""
        name="newpage" value="${requestParams?requestParams.speciesName:''}" data-rank="${requestParams?requestParams.rank:TaxonomyRank.SPECIES.ordinal()}"
        placeholder="${g.message(code:'placeholder.namelist.add.target')}" />
        <div id="nameSuggestions" style="display: block;position:relative;"></div>
        <input class="colId" type="hidden" name="colId" value="${params.colId}"/> 
        <input class="recoId" type="hidden" name="recoId" value="${params.recoId}"/>           
    </div>
</g:if>