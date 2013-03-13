<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
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
		<div class="observations thumbwrap">
			<div class="observation">
				<div>
					<obv:showObservationFilterMessage
						model="['observationInstanceList':observationInstanceList, 'observationInstanceTotal':observationInstanceTotal, 'queryParams':queryParams]" />
						
				</div>
				<div style="clear: both;"></div>
				
				
				<!-- needs to be fixed -->
				<g:if test="${!isSearch}">
					<div id="map_view_bttn" class="btn-group">
						<a class="btn btn-success dropdown-toggle" data-toggle="dropdown"
							href="#"
							onclick="$(this).parent().css('background-color', '#9acc57'); showMapView(); return false;">
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
					
				<div id="observations_list_map" class="observation"
					style="clear: both; display: none;">
					<obv:showObservationsLocation
						model="['observationInstanceList':totalObservationInstanceList, 'userGroup':userGroup]">
					</obv:showObservationsLocation>
				</div>
			</div>
			<obv:showObservationsList  model="['totalObservationInstanceList':totalObservationInstanceList, 'observationInstanceList':observationInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters, 'userGroup':userGroup]"  />
		</div>
	</div>

	<!-- main_content end -->
</div>
<g:javascript>
$(document).ready(function() {
	window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}"
	
});
</g:javascript>

