<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>

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
            <div class="span4" style="position:relative;top:20px">

                <uGroup:objectPostToGroupsWrapper 
                model="[canPullResource:canPullResource, 'objectType':Observation.class.canonicalName, 'userGroup':userGroup]" />
                <div id="taxonBrowser">
                    <div class="taxonomyBrowser sidebar_section" data-name="classification" data-speciesid="${speciesInstance?.id}" style="position:relative">
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
               <!-- 
               <g:render template="/observation/summaryTemplate" model="['speciesCount':speciesCount, 'subSpeciesCount':subSpeciesCount]"/>
               -->
                <div id="observations_list_map" class="observation sidebar_section"
                    style="clear:both;overflow:hidden;display:none;">
                    <h5><g:message code="default.species.distribution.label" /></h5>
                    <obv:showObservationsLocation
                    model="['observationInstanceList':totalObservationInstanceList, 'userGroup':userGroup]">
                    </obv:showObservationsLocation>
                    <a id="refreshListForBounds" data-toggle="dropdown"
                        href="#"><i class="icon-refresh"></i>
                        <g:message code="button.refresh.list" /></a>
                    <a id="resetMap" data-toggle="dropdown"
                        href="#"><i class="icon-refresh"></i>
                        <g:message code="button.reset" /></a>

                    <div><i class="icon-info"></i><g:message code="map.limit.info" /></div>
                    <input id="isMapView" name="isMapView" value="${params.isMapView}" type="hidden"/>
                    <input id="bounds" name="bounds" value="${activeFilters?.bounds}" type="hidden"/>
                    <input id="tag" name="tag" value="${params.tag}" type="hidden"/>
                </div>
                <div class="sidebar_section" style="clear:both;overflow:hidden;">
                    <h5> <g:message code="default.species.groups.label" /> </h5>
                    <div id="speciesGroupCountList"></div>
                </div>
                <g:render template="/observation/distinctRecoTableTemplate" model="[distinctRecoList:distinctRecoList, totalCount:totalCount]"/>

            </div>
        </div>
    </div>

	<!-- main_content end -->
</div>
<r:script type="text/javascript">
$(document).ready(function() {
    window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}"

    $("#map_view_bttn a").click(function(){
        $(this).parent().css('background-color', '#9acc57');
        $('#observations_list_map').slideToggle(mapViewSlideToggleHandler);
    });
    <g:if test="${params.isMapView?.equalsIgnoreCase('true') || params.bounds}">
    </g:if>
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

});
</r:script>
<script>
    var taxonRanks = [];
    <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
    </g:each>

    $(document).ready (function() {
    $('.list').on('updatedGallery', function() {
    	loadSpeciesGroupCount();
        //loadDistinctRecoList();
        updateDistinctRecoTable();
        });
    });

</script>
<r:script>
$(document).ready(function() {
        var taxonBrowserOptions = {
            expandAll:false,
            controller:"${params.controller?:'observation'}",
            action:"${params.action?:'list'}",
            expandTaxon:"${params.taxon?true:false}"
        }
        if(${params.taxon?:false}){
        taxonBrowserOptions['taxonId'] = "${params.taxon}";
        }
        var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	
});
</r:script>
