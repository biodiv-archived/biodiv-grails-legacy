<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.participation.Recommendation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>

<div class="">
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
						model="['observationInstanceList':observationInstanceList, 'observationInstanceTotal':observationInstanceTotal, 'queryParams':queryParams, resultType:'observation']" />
						
				</div>
				<div style="clear: both;"></div>
				
				
				<!-- needs to be fixed -->
				<g:if test="${!isSearch}">
					<div id="map_view_bttn" class="btn-group" style="display:none;">
						<a class="btn btn-success dropdown-toggle" data-toggle="dropdown"
							href="#">
							Map view <span class="caret"></span> </a>
					</div>
				</g:if>
				<div class="btn-group pull-left" style="z-index: 10">
					<button id="selected_sort" class="btn dropdown-toggle"
						data-toggle="dropdown" href="#" rel="tooltip"
						data-original-title="Sort by">

						<g:if test="${params.sort == 'visitCount'}">
                                               Most Viewed
                                            </g:if>
						<g:elseif test="${params.sort == 'createdOn'}">
                                                Latest
                                            </g:elseif>
						<g:elseif test="${params.sort == 'score'}">
                                                Relevancy
                                            </g:elseif>
						<g:else>
                                                Last Updated
                                            </g:else>
						<span class="caret"></span>
					</button>
					<ul id="sortFilter" class="dropdown-menu" style="width: auto;">
						<li class="group_option"><a class=" sort_filter_label"
							value="createdOn"> Latest </a></li>
						<li class="group_option"><a class=" sort_filter_label"
							value="lastRevised"> Last Updated </a></li>
						<g:if test="${isSearch}">
							<li class="group_option"><a class=" sort_filter_label"
								value="score"> Relevancy </a></li>
						</g:if>
						<g:else>
							<li class="group_option"><a class=" sort_filter_label"
								value="visitCount"> Most Viewed </a></li>
						</g:else>
					</ul>


				</div>
				
				<obv:identificationByEmail
					model="['source':'observationList', 'requestObject':request, autofillUsersId:'shareUsers']" />
				
				<obv:download
					model="['source':'Observations', 'requestObject':request, 'downloadTypes':[DownloadType.CSV, DownloadType.KML] ]" />

			</div>
                        <div class="span8 right-shadow-box" style="margin:0px;clear:both;">
                            <obv:showObservationsList  model="['totalObservationInstanceList':totalObservationInstanceList, 'observationInstanceList':observationInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters, 'userGroup':userGroup, canPullResource:canPullResource]"  />
                        </div>
                        <div class="span4" style="position:relative;top:20px">
                 
                 <uGroup:objectPostToGroupsWrapper 
						model="[canPullResource:canPullResource, 'objectType':Observation.class.canonicalName]" />
                        
				<div id="observations_list_map" class="observation sidebar_section"
                                    style="clear:both;overflow:hidden;display:none;">
                    <h5>Species Distribution</h5>
					<obv:showObservationsLocation
						model="['observationInstanceList':totalObservationInstanceList, 'userGroup':userGroup]">
					</obv:showObservationsLocation>
                                        <a id="refreshListForBounds" data-toggle="dropdown"
                                            href="#"><i class="icon-refresh"></i>
							Refresh list to map bounds</a>

                                        <input id="isMapView" name="isMapView" value="${params.isMapView}" type="hidden"/>
                                        <input id="bounds" name="bounds" value="${activeFilters?.bounds}" type="hidden"/>
                                        <input id="tag" name="tag" value="${params.tag}" type="hidden"/>
				</div>
                                <div id="obvPerGroupChart" class="sidebar_section" style="clear:both;overflow:hidden;">
                                    <chart:showStats model="['title':'Species Groups', columns:speciesGroupCountList.columns, data:speciesGroupCountList.data, width:300, height:270, 'hideTable':true, dynamicLoading:true]"/>
                                </div>
                                <g:render template="/observation/distinctRecoTableTemplate" model="[distinctRecoList:distinctRecoList, totalCount:totalCount]"/>
                                
                        </div>
		</div>
	</div>

	<!-- main_content end -->
</div>
<g:javascript>
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
        refreshList(getSelectedBounds());
    });

});
</g:javascript>
