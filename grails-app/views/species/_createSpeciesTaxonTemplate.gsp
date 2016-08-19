<%
def tRank=(isPopup)?TaxonomyRank.list().reverse():TaxonomyRank.list().reverse()[0..1];
%>
<div class="control-group">
    <label class="control-label span3" for="name">
    <g:if test="${!isPopup}">
        <g:message code="default.add.page.label" /> 
    </g:if>
    </label> 
    <div class="pull-left" style="width:700px;margin-left:20px;"> 
        <div class="input-prepend">        
            <select id="rank" name="rank" class="add-on">
            <g:each in="${tRank}" var="rank">
                <option value="${rank.ordinal()}" ${(requestParams?requestParams.rank:-1) == rank?'selected':''}><g:message error="${rank}"/></option>
            </g:each>
        </select>
        <div id="validateSpeciesSubmit" class="btn ${validate?'btn-mini btn-success disabled':'btn-primary'}"            
            style="float: right; margin-left: 5px;"> 
            <g:if test="${validate}">
                <g:message code="button.validated" />
            </g:if>
            <g:else>
                <g:message code="button.validate" />
            </g:else>
        </div>
        <input id="page" 
        data-provide="typeahead" type="text" class="taxonRank" onchange="enableValidButton($(this).parent(),true);" style=""
        name="page" value="${requestParams?requestParams.speciesName:''}" data-rank="${requestParams?requestParams.rank:TaxonomyRank.SPECIES.ordinal()}"
        placeholder="${g.message(code:'placeholder.create.add')}" />
        <input type="hidden" name="canName" id="canName" value=""/>
        <div id="nameSuggestions" style="display: block;position:relative;"></div>
        <input type="hidden" name="lang" value="${lang?:params.lang}"/>
        <input type="hidden" name="colId" value="${params.colId}"/>
    </div>
    <div id="parserInfo" style="margin-top:10px; ${(!isPopup)? 'display:none;':''}">
        <label style="float:left;">Canonical Name :</label> <div  class="canonicalName"> </div>
        <label style="clear:both; float:left;">Author Year:</label> <div class="authorYear"> </div>
    </div>
    <div id="errorMsg" class="alert hide" style="clear:both;"></div>
    </div>
</div>
<div class="genusSelector control-group hide" style="clear:both;margin-bottom:10px;">
    <label class="control-label span3">Genus Selector</label>
     <div class="genusItemList span8">
     </div>
</div>
  
<g:render template="/common/createTaxonRegistryTemplate" model='[requestParams:requestParams,isPopup:true, errors:errors]'/>    

  