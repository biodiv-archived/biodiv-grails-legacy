<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<div class="">

	<!-- main_content -->
	<div class="list" style="margin-left:0px;clear:both">
		<div class="observation thumbwrap">
			<div class="observation">
				<div>
					<obv:showObservationFilterMessage
						model="['observationInstanceList':instanceList, 'observationInstanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'trait']" />
				</div>
			</div>

            <div class="span8 right-shadow-box" style="margin:0px;clear:both;">
                <div class="filters" style="position: relative;">
                    <g:render template="showTraitListTemplate"/>
                </div>
            </div>
            <div class="span4" style="position:relative;top:20px">
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
                <g:render template="/trait/matchingSpeciesTableTemplate" model="[matchingSpeciesList:matchingSpeciesList, totalCount:totalCount]"/>
            </div>
        </div>
    </div>

	<!-- main_content end -->
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

        $('.list').on('updatedGallery', function() {
            $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	

            $('.trait button, .trait .all').on('click', function(){
                console.log($(this));
                if($(this).hasClass('active')){
                return false;
                }
                $(this).parent().parent().find('button, .all').removeClass('active btn-success');
                $(this).addClass('active btn-success');

                updateMatchingSpeciesTable();
                return false;
            });
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
