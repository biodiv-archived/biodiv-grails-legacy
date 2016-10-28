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
	<div class="list span12 namelist_wrapper" style="margin-left:0px;clear:both">
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
            <div class="span9 right-shadow-box" style="position: relative;height:388px;overflow-y: scroll;overflow-x: hidden;">
                <g:render template="showTraitListTemplate"/>
            </div>
        </div>
    </div>
</div>
<div class="row-fluid">
    <g:render template="/trait/matchingSpeciesTableTemplate" model="[matchingSpeciesList:matchingSpeciesList, totalCount:totalCount]"/>
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
        
        $(document).on('click', '.trait button, .trait .all, .trait .any, .trait .none', function(){
            if($(this).hasClass('active')){
            return false;
            }
            $(this).parent().parent().find('button, .all, .any, .none').removeClass('active btn-success');
            $(this).addClass('active btn-success');


            updateMatchingSpeciesTable();
            return false;
        });
        $('.list').on('updatedGallery', function() {
            updateMatchingSpeciesTable();
        });
    });
</script>
<asset:script type="text/javascript">
$(document).ready(function() {
	$(".trait button").button();
	$(".trait button").tooltip({placement:'bottom'});
    <g:each in="${params.trait}" var="t">
        $('.trait button[data-tvid="${t.value}"][data-tid="${t.key}"]').addClass('active btn-success');
    </g:each>
});
</asset:script>
