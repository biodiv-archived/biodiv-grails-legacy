<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@ page import="species.Species"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.TaxonomyDefinition.TaxonomyRank"%>

<div class="taxonomyBrowser sidebar_section" style="position: relative;" data-name="classification" data-speciesid="${speciesInstance?.id}">
    <h5>Classifications</h5>	

    <div id="taxaHierarchy">
        <g:set var="classifications" value="${speciesInstance?.classifications()}" />
        <g:if test="${speciesInstance && classifications}">
        <select name="taxaHierarchy" class="value ui-corner-all" style="margin-bottom:0px;width:100%;background-color:whitesmoke;">
            <g:each in="${classifications}" var="classification">
            <option value="${classification[0]}">
                ${classification[1].name} ${classification[2].toString()}
            </option>
            </g:each>
        </select>
        <div class="attributionBlock">
            <span class="ui-icon-info ui-icon-control " title="Show details"
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
       <table id="taxonHierarchy" class="emptyField"></table>
        </g:else>

    </div>
    <form id="taxonHierarchyForm" class="form-horizontal editableform hide">
        <div class="control-group">
            <div>
                <div class="editable-input">
                    <g:each in="${TaxonomyRank.list()}" var="taxonRank">
                    <g:if test="${taxonRank.ordinal() == speciesInstance?.taxonConcept?.rank}">
                    <input type="hidden"  data-rank ="${taxonRank.ordinal()}"
                    type="text" name="taxonRegistry.${taxonRank.ordinal()}" 
                    value="${speciesInstance?.taxonConcept?.name}"
                    placeholder="Add ${taxonRank.value()}" readonly/>
                    </g:if>
                    <g:elseif test="${taxonRank.ordinal() < speciesInstance?.taxonConcept?.rank}">
                    <div class="input-prepend">
                        <span class="add-on"> ${taxonRank.value()}</span>
                        <input data-provide="typeahead" data-rank ="${taxonRank.ordinal()}"
                        type="text" class="input-block-level taxonRank" name="taxonRegistry.${taxonRank.ordinal()}" value=""
                        placeholder="Add ${taxonRank.value()}" />

                    </div>

                    </g:elseif>
                    </g:each>
                    <input class='classification' type="hidden" name="classification" value="${Classification.findByName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY).id}" readonly/>
                </div>
                <div class="editable-buttons editable-buttons-bottom pull-right">
                    <button type="submit" class="btn btn-primary editable-submit"><i class="icon-ok icon-white"></i>Save</button>
                    <button type="button" class="btn editable-cancel"><i class="icon-remove"></i>Cancel</button>
                </div>
            </div>
        </div> 
    </form>  
    <div class="editable-error-block"></div>
</div>
