<%@ page import="species.Species"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.TaxonomyDefinition.TaxonomyRank"%>



        <div id="taxonHierachy" class="control-group" style="display:none;">
            <label
                class="control-label span3">Taxon Hierarchy</label> 
            <div class = "span8">
                <div id="existingHierarchies"><div>Following hierarchies already exist for the given species name.</div></div>
                <g:each in="${TaxonomyRank.list()}" var="taxonRank">
                <g:if test="${taxonRank.ordinal() != TaxonomyRank.SPECIES.ordinal()}">
                <input data-provide="typeahead" data-rank ="${taxonRank.ordinal()}"
                type="text" class="input-block-level taxonRank" name="taxonRegistry.${taxonRank.ordinal()}" value=""
                placeholder="Add ${taxonRank.value()}" />
                </g:if>
                </g:each>
            </div>
        </div>

