<%@ page import="species.Species"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.TaxonomyDefinition.TaxonomyRank"%>

<div id="taxonHierachyInput" class="control-group">
    <label
        class="control-label span3">Taxon Hierarchy</label> 
    <div class = "span8">
        <g:each in="${TaxonomyRank.list()}" var="taxonRank">
        <div class="input-prepend input-block-level">
            <span class="add-on"> ${taxonRank.value()}</span>
            <input data-provide="typeahead" data-rank ="${taxonRank.ordinal()}"
            type="text" class="input-block-level taxonRank" name="taxonRegistry.${taxonRank.ordinal()}" value=""
            placeholder="Add ${taxonRank.value()}" />
        </div>
        </g:each>
        <div id="existingHierarchies" class="hide" ><div>Following hierarchies already exist for the given name.</div></div>
    </div>
</div>

