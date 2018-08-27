<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<div class="row-fluid">

	<!-- main_content -->
	<div class="observation thumbwrap">
			<obv:showObservationFilterMessage
						model="['observationInstanceList':instanceList, 'observationInstanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'trait']" />

           <div class="span3" style="position:relative;margin-left:0px;">
               <div id="taxonBrowser">
                    <div class="taxonomyBrowser sidebar_section" style="position:relative">
                        <h5><g:message code="button.taxon.browser" /></h5> 
                        <div id="taxaHierarchy">

                            <%
                            def classifications = [];
                            Classification.list().each {
                            classifications.add([it.id, it, null]);
                            }
                            classifications = classifications.sort {return it[1].name}; 
                            %>

                            <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, 'expandAll':false, 'queryParams':queryParams, selectedClassification:queryParams.classification]"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="span9 right-shadow-box" style="position: relative;height:388px;overflow-y: scroll;">
                <g:render template="showTraitListTemplate" model="['displayAny':true, 'editable':false]"/>
            </div>
        </div>
    </div>
</div>
<div class="row-fluid">
    <g:render template="/trait/matchingSpeciesTableTemplate" model="[matchingSpeciesList:matchingSpeciesList, totalCount:totalCount, resultType:'species', 'matchingAction':'matchingSpecies', loadListAction:'loadMatchingSpeciesList']"/>
</div>
<script>
    var taxonRanks = [];
    <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
    </g:each>

    $(document).ready (function() {
        var taxonBrowserOptions = {
            expandAll:false,
            controller:"${params.controller?:'observation'}",
            action:"${params.action?:'list'}",
            expandTaxon:"${params.taxon?true:false}"
        }

        if(${params.taxon?:false}){
            taxonBrowserOptions['taxonId'] = "${params.taxon}";
        }
        
        $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	

        $('.list').on('updatedGallery', function() {
                initTraits();
        });

        initTraits();
    });

$(document).ready(function() {
	$(".trait button").button();
	$(".trait button").tooltip({placement:'bottom', 'container':'body'});
    <g:each in="${params.trait}" var="t">
        <g:each in="${t.value.split(',')}" var="tv">
            $('.trait button[data-tvid="${tv}"][data-tid="${t.key}"]').addClass('active btn-success');
        </g:each>
    </g:each>
});
</script>
