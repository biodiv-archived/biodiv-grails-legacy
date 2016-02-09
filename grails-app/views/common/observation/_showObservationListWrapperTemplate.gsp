<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<style>
    .observation .prop .value {
        margin-left:260px;
    }
</style>
<div class="">


 	<g:render template="/observation/browseObservationMenu" model="[]"/>

	<!-- main_content -->
	<div class="list" style="margin-left:0px;clear:both">
		<div class="filters" style="position: relative;">
			<obv:showGroupFilter
				model="['observationInstance':observationInstance, forObservations:true]" />
		</div>
		<div class="observation thumbwrap">
			<div class="observation">
				<div>
					<obv:showObservationFilterMessage
						model="['observationInstanceList':observationInstanceList, 'observationInstanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'observation']" />
						
				</div>
				<div style="clear: both;"></div>
				
				
				<!-- needs to be fixed -->
				<g:if test="${!isSearch}">
					<div id="map_view_bttn" class="btn-group" style="display:none;">
						<a class="btn btn-success dropdown-toggle" data-toggle="dropdown"
							href="#">
							<g:message code="button.map.view" /> <span class="caret"></span> </a>
					</div>
				</g:if>
				<div class="btn-group pull-left" style="z-index: 10">
					<button id="selected_sort" class="btn dropdown-toggle"
						data-toggle="dropdown" href="#" rel="tooltip"
						data-original-title="${g.message(code:'showobservationlistwrapertemp.sort')}">

						<g:if test="${params.sort == 'visitCount'}">
                                             <g:message code="button.most.viewed" />  
                                            </g:if>
						<g:elseif test="${params.sort == 'createdOn'}">
                                                <g:message code="button.latest" />
                                            </g:elseif>
						<g:elseif test="${params.sort == 'score'}">
                                               <g:message code="button.relevancy" /> 
                                            </g:elseif>
						<g:else>
                                               <g:message code="button.last.updated" />
                                            </g:else>
						<span class="caret"></span>
					</button>
					<ul id="sortFilter" class="dropdown-menu" style="width: auto;">
						<li class="group_option"><a class=" sort_filter_label"
							value="createdOn"> <g:message code="button.latest" /> </a></li>
						<li class="group_option"><a class=" sort_filter_label"
							value="lastRevised"> <g:message code="button.last.updated" /> </a></li>
						<g:if test="${isSearch}">
							<li class="group_option"><a class=" sort_filter_label"
								value="score"> <g:message code="button.relevancy" /> </a></li>
						</g:if>
						<g:else>
							<li class="group_option"><a class=" sort_filter_label"
								value="visitCount"> <g:message code="button.most.viewed" /> </a></li>
						</g:else>
					</ul>


				</div>

				
				<obv:identificationByEmail
					model="['source':'observationList', 'requestObject':request, autofillUsersId:'shareUsers',title:g.message(code:'button.share')]" />
				
				<obv:download
					model="['source':'Observations', 'requestObject':request, 'downloadTypes':[DownloadType.CSV, DownloadType.KML, DownloadType.DWCA], 'onlyIcon': 'false', 'downloadFrom' : 'obvList']" />

			</div>
            <div class="span8 right-shadow-box" style="margin:0px;clear:both;">
                <obv:showObservationsList />
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
        $('.list').on('updatedGallery', function() {
            //loadSpeciesGroupCount();
            //updateDistinctRecoTable();
        });
    });

</script>
<asset:script type="text/javascript">
$(document).ready(function() {
    window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}"

    $("#map_view_bttn a").click(function(){
        $(this).parent().css('background-color', '#9acc57');
        $('#observations_list_map').slideToggle(mapViewSlideToggleHandler);
    });
    
    $("#map_view_bttn a").click();

    $('#big_map_canvas').on('maploaded', function(){
        /*map.on('viewreset', function() {
            refreshList(getSelectedBounds());
        });*/
    });
    
    $("#refreshListForBounds").click(function() {
        var mapLocationPicker = $('#big_map_canvas').data('maplocationpicker');
        refreshList(mapLocationPicker.getSelectedBounds());
    });

    $("#resetMap").click(function() {
        var mapLocationPicker = $('#big_map_canvas').data('maplocationpicker');
        //refreshList(mapLocationPicker.getSelectedBounds());
        $("#bounds").val('');
        refreshMapBounds(mapLocationPicker);
    });

    var taxonBrowserOptions = {
        expandAll:false,
        controller:"${params.controller?:'observation'}",
        action:"${params.action?:'list'}",
        expandTaxon:"${params.taxon?true:false}"
    }
    if(${params.taxon?:false}){
        taxonBrowserOptions['taxonId'] = "${params.taxon}";
    }
    //$('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	
});
</asset:script>
