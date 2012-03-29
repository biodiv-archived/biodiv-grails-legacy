
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css', absolute:true)}"
	type="text/css" media="all" />
<g:javascript src="tagit.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="jquery.autopager-1.0.0.js"
	base="${grailsApplication.config.grails.serverURL+'/js/jquery/'}"></g:javascript>
</head>
<body>
	<div class="container_16">
		<div class="grid_16 big_wrapper">
			<h1>
				<g:message code="default.list.label" args="[entityName]" />
			</h1>
			
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
	
                        <div class="filters">
                            <div class="grid_8" style="margin:0;">
                                <obv:showGroupFilter
                                        model="['observationInstance':observationInstance]" />
                            </div>
                        </div>
				
			<div style="clear:both"></div>
			<div class="list">
				<div class="observations thumbwrap">
					<div class="observation grid_11">
					
					<div id="sortFilter" class="filterBar"  style="clear: both">
						<input type="radio" name="sortFilter" id="sortFilter1" value="createdOn" style="display: none" />
						<label for="sortFilter1" value="createdOn">Latest</label>
		
						<input type="radio" name="sortFilter" id="sortFilter2" value="lastUpdated" style="display: none" />
						<label for="sortFilter2" value="lastUpdated">Last Updated</label>
		
						<input type="radio" name="sortFilter" id="sortFilter3" value="visitCount" style="display: none" />
						<label for="sortFilter3" value="visitCount">Most Viewed</label>
					</div>
					<div id="speciesNameFilter" class="filterBar"  style="float:right">
						<input type="radio" name="speciesNameFilter" id="speciesNameFilter1" value="All" style="display: none" />
						<label for="speciesNameFilter1" value="All">All</label>
		
						<input type="radio" name="speciesNameFilter" id="speciesNameFilter2" value="Unknown" style="display: none" />
						<label for="speciesNameFilter2" value="Unknown">Unidentified</label>
					</div>
					
					
					
											<div class="info-message" style="clear: both" >
												Showing <span class="highlight">${observationInstanceTotal} observation<g:if test="${observationInstanceTotal>1}">s</g:if></span>
												<g:if test="${queryParams?.groupId}">
													of <span class="highlight">${SpeciesGroup.get(queryParams.groupId).name.toLowerCase()}</span> group
												</g:if>
												<g:if test="${queryParams?.habitat}">
													in <span class="highlight">${Habitat.get(queryParams.habitat).name.toLowerCase()}</span> habitat
												</g:if>
												<g:if test="${queryParams?.tag}">
													tagged <span class="highlight">${queryParams.tag}</span>
												</g:if>
											</div>
                                           <div id="map_view_bttn" class="map_view_button"></div>
                                        </div>
                                        
					<div id="observations_list_map" class="observation grid_11" style="display:none;">
                                            <obv:showObservationsLocation model="['observationInstanceList':totalObservationInstanceList]"></obv:showObservationsLocation>
                                        </div>
				        
                                        <obv:showObservationsList/>

					<div class="tags_section grid_4">
						<obv:showAllTags/>
						<obv:showGroupList/>
					</div>
				        
					<div class="paginateButtons" style="visibility:hidden; clear: both">
						<g:paginate total="${observationInstanceTotal}" max="2" params="${activeFilters}"/>
					</div>
				</div>
			</div>
			
		</div>
	</div>
	<g:javascript>	
	$(document).ready(function(){
		$( "#sortFilter" ).buttonset();
		$('#sortFilter label[value$="${params.sort}"]').each (function() {
			$(this).attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active');
		});
		
		$( "#speciesNameFilter" ).buttonset();
		$('#speciesNameFilter label[value$="${params.speciesName}"]').each (function() {
			$(this).attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active');
		});
		
		function getSelectedGroup() {
			var grp = ''; 
			$('#speciesGroupFilter label').each (function() {
				if($(this).attr('aria-pressed') === 'true') {
					grp += $(this).attr('value') + ',';
				}
			});
			
			grp = grp.replace(/\s*\,\s*$/,'');
			return grp;	
		} 
		
		function getSelectedHabitat() {
			var hbt = ''; 
			$('#habitatFilter label').each (function() {
				if($(this).attr('aria-pressed') === 'true') {
					hbt += $(this).attr('value') + ',';
				}
			});
			
			hbt = hbt.replace(/\s*\,\s*$/,'');
			return hbt;	
		} 
		
		function getSelectedSortBy() {
			var sortBy = ''; 
			$('#sortFilter label').each (function() {
				if($(this).attr('aria-pressed') === 'true') {
					sortBy += $(this).attr('value') + ',';
				}
			});
			
			sortBy = sortBy.replace(/\s*\,\s*$/,'');
			return sortBy;	
		} 
		
		function getSelectedSpeciesName() {
			var sName = ''; 
			$('#speciesNameFilter label').each (function() {
				if($(this).attr('aria-pressed') === 'true') {
					sName += $(this).attr('value') + ',';
				}
			});
			
			sName = sName.replace(/\s*\,\s*$/,'');
			return sName;	
		} 

                function getSelectedBounds() {
                    var bounds = '';
                    /*
                    var swLat = map.getBounds().getSouthWest().lat();
                    var swLng = map.getBounds().getSouthWest().lng();
                    var neLat = map.getBounds().getNorthEast().lat();
                    var neLng = map.getBounds().getNorthEast().lng();

                    bounds = [swLat, swLng, neLat, neLng].join()
                    */ 
                    return bounds;    

                }
	
		function getFilterParameters(url, limit, offset) {
			
			var params = url.param();
			
			var sortBy = getSelectedSortBy();
			if(sortBy) {
				params['sort'] = sortBy;
			}
			
			var sName = getSelectedSpeciesName();
			if(sName) {
				params['speciesName'] = sName;
			}
			
			var grp = getSelectedGroup();
			if(grp) {
				params['sGroup'] = grp;
			}
			
			var habitat = getSelectedHabitat();
			if(habitat) {
				params['habitat'] = habitat;
			}

                        var bounds = getSelectedBounds();
                        if (bounds) {
				params['bounds'] = bounds;
                        }
			
			if(limit != undefined) {
				params['max'] = limit.toString();
			}
			
			if(offset != undefined) {
				params['offset'] = offset.toString();
			}

			return params;
		}	
		
		
		function updateGallery(target, limit, offset) {
			if(target === undefined) {
				target = window.location.pathname + window.location.search;
			}
			
			var a = $('<a href="'+target+'"></a>');
			var url = a.url();
			var href = url.attr('path');
			var params = getFilterParameters(url, limit, offset);
			var recursiveDecoded = decodeURIComponent($.param(params));
			window.location = href+'?'+recursiveDecoded;
			//var doc_url = href+'?'+recursiveDecoded;
		        //$(".observations").load(doc_url+" .observations")
                        //window.history.pushState(null, "", doc_url);

			//var carousel = jQuery('#carousel_${carousel_id}').data('jcarousel');
			//reloadCarousel(carousel, "speciesGroup", params['sGroupId']);
		}
		
		$('#speciesGroupFilter input').change(function(){
			updateGallery(undefined, ${queryParams.max}, 0);
			return false;
		});
		
		$('#habitatFilter input').change(function(){
			updateGallery(undefined, ${queryParams.max}, 0);
			return false;
		});
		
		$('#sortFilter input').change(function(){
			updateGallery(undefined, ${queryParams.max}, 0);
			return false;
		});
		
		$('#speciesNameFilter input').change(function(){
			updateGallery(undefined, ${queryParams.max}, 0);
			return false;
		});
	
		
		$(".paginateButtons a").click(function() {
			updateGallery($(this).attr('href'));
			return false;
		});
		
		$("ul[name='tags']").tagit({select:true,  tagSource: "${g.createLink(action: 'tags')}"});
         
		$('#map_view_bttn').click(function(){
			$('#observations_list_map').toggle(function(){
                            
                            if ($(this).is(':hidden')){
                                $('div.observations > div.observations_list').show();
                            } else {       
                                $('div.observations > div.observations_list').hide();
                            }
                        });
                        //$(this).addClass('active');
			//$('#list_view_bttn').removeClass('active');
			//$('#grid_view_bttn').removeClass('active');
			//$.cookie("observation_listing", "map");

                });
                
	});
	</g:javascript>

        <script type="text/javascript"
                src="http://maps.google.com/maps/api/js?sensor=true"></script>
        <g:javascript src="markerclusterer.js"
                base="${grailsApplication.config.grails.serverURL+'/js/location/google/'}"></g:javascript>
        <script>
                var markers = [];
                
                $(document).ready(function() {
                  var latlng = new google.maps.LatLng('22.77', '77.22');
                  var options = {
                    zoom: 4,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                  };
                  map = new google.maps.Map(document.getElementById("map_canvas"), options);

                  var bounds = "${activeFilters.bounds}";

                  if (bounds) {
                    var data = bounds.split(",");
                    var bnd = new google.maps.LatLngBounds(new google.maps.LatLng(parseFloat(data[0]), parseFloat(data[1])), new google.maps.LatLng(parseFloat(data[2]), parseFloat(data[3]))); 
                    map.fitBounds(bnd);
                  }       

                  <g:each in="${totalObservationInstanceList}" status="i"
						var="observationInstance">
		                var latlng = new google.maps.LatLng(
		                		${observationInstance.latitude}, ${observationInstance.longitude});
						var marker = new google.maps.Marker({
							position: latlng,
							map: map,
							draggable: false
						});
	                    markers.push(marker);

                            var infowindow = new google.maps.InfoWindow({
                                content: 'An InfoWindow'
                            });

	                    google.maps.event.addListener(marker, 'click', function() {
	                        map.setZoom(8);
	                        map.setCenter(marker.getPosition());
                                infowindow.open(map, marker);
	                    });

		    </g:each>	
				  
		    var markerCluster = new MarkerClusterer(map, markers);
                
                });
        </script>

</body>
</html>
