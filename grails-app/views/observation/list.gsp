
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
<script type="text/javascript"
	src="http://maps.google.com/maps/api/js?sensor=true"></script>
<g:javascript src="markerclusterer.js"
	base="${grailsApplication.config.grails.serverURL+'/js/location/google/'}"></g:javascript>

<g:javascript src="tagit.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="jquery.autopager-1.0.0.js"
	base="${grailsApplication.config.grails.serverURL+'/js/jquery/'}"></g:javascript>
</head>
<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header">
					<h1>
						<g:message code="default.observation.heading" args="[entityName]" />
					</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<div class="filters" style="position: relative;">
					<obv:showGroupFilter
						model="['observationInstance':observationInstance]" />
					<div id="speciesNameFilter" class="btn-group"
						style="float: right; margin-right: 5px; z-index: 10; position: absolute; margin-top: -65px; right: 0;">
						<!-- input type="radio" name="speciesNameFilter"
												id="speciesNameFilter1" value="All" style="display: none" />
											<label for="speciesNameFilter1" value="All">All</label> <input
												type="radio" name="speciesNameFilter"
												id="speciesNameFilter2" value="Unknown"
												style="display: none" /> <label for="speciesNameFilter2"
												value="Unknown">Show Unidentified Only</label-->
						<input type="text" id="speciesNameFilter"
							value="${params.speciesName}" style="display: none" />
						<button id="speciesNameFilterButton" class="btn"
							data-toggle="button">Show Unidentified Only</button>
					</div>
				</div>
				<div class="tags_section span3" style="float: right;">
					<obv:showAllTags model="['tagFilterByProperty':'All']" />
				</div>

				<div class="row">
					<!-- main_content -->
					<div class="list span9">

						<div class="observations thumbwrap">
							<div class="observation">
								<div>
									<!-- main_content -->
									<div class="info-message">
										<span class="name" style="color: #b1b1b1;"> <i
											class="icon-screenshot"></i> ${observationInstanceTotal} </span>
										Observation<g:if test="${observationInstanceTotal>1}">s</g:if>
										<g:if test="${queryParams.groupId}">
	                                            of <span class="highlight">
												${SpeciesGroup.get(queryParams.groupId).name} </span> group
	                                    </g:if>
										<g:if test="${queryParams.habitat}">
	                                            in <span class="highlight">
												${Habitat.get(queryParams.habitat).name} </span> habitat
	                                    </g:if>
										<g:if test="${queryParams.tag}">
	                                            tagged <span
												class="highlight"> ${queryParams.tag} </span>
										</g:if>
									</div>

								</div>
								<div style="clear: both;"></div>

								<div id="map_view_bttn" class="btn-group">
									<a class="btn btn-success dropdown-toggle"
										data-toggle="dropdown" href="#"
										onclick="$(this).parent().css('background-color', '#9acc57'); showMapView(); return false;">
										Map view <span class="caret"></span> </a>
								</div>


								<div id="observations_list_map" class="observation"
									style="clear: both; display: none;">
									<obv:showObservationsLocation
										model="['observationInstanceList':totalObservationInstanceList]">
									</obv:showObservationsLocation>
								</div>


								<div class="btn-group"
									style="float: right; margin-right: 80px; z-index: 10">
									<button id="selected_sort"
										class="btn dropdown-toggle" data-toggle="dropdown" href="#"><g:if
											test="${params.sort == 'visitCount'}">
                                               Sort by Most viewed
                                            </g:if> <g:elseif
											test="${params.sort == 'lastRevised'}">
                                                Sort by Last updated
                                            </g:elseif> <g:else>
                                                Sort by Latest
                                            </g:else> <span class="caret"></span>
									</button>
									
									<ul id="sortFilter" class="dropdown-menu" style="width:auto;">
										<li class="group_option">
													<a class=" sort_filter_label" value="createdOn"> Latest
												</a></li>
												<li class="group_option">
													<a class=" sort_filter_label" value="lastRevised"> Last Updated
												</a></li>
												<li class="group_option">
													<a class=" sort_filter_label" value="visitCount"> Most Viewed
												</a></li>
									</ul>
									

								</div>

							</div>


							<obv:showObservationsList />

						</div>
					</div>



				</div>
				<!-- main_content end -->
			</div>
		</div>
	</div>
	<!--container end-->
	<g:javascript>	
        $(document).ready(function(){
            $('.dropdown-toggle').dropdown();
            
            $('.sort_filter_label').click(function(){
                $(this).addClass('active'); 
                updateGallery(undefined, ${queryParams.max}, 0);
                return false;   
            });

			$('#speciesNameFilterButton').button();
			if(${params.speciesName == 'Unknown' }) {
				$("#speciesNameFilterButton").addClass('active')
			} 
			
			$("#speciesNameFilterButton").click(function() {
				if($("#speciesNameFilterButton").hasClass('active')) {
					$("#speciesNameFilter").val('All')
					$("#speciesNameFilterButton").removeClass('active')
				} else {
					$("#speciesNameFilter").val('Unknown')
					$("#speciesNameFilterButton").addClass('active')
					
				}
				updateGallery(undefined, ${queryParams.max}, 0);
                return false;
			});
			                
            function getSelectedGroup() {
                var grp = ''; 
                $('#speciesGroupFilter button').each (function() {
                        if($(this).hasClass('active')) {
                                grp += $(this).attr('value') + ',';
                        }
                });

                grp = grp.replace(/\s*\,\s*$/,'');
                return grp;	
            } 
                
            function getSelectedHabitat() {
                var hbt = ''; 
                $('#habitatFilter button').each (function() {
                        if($(this).hasClass('active')) {
                                hbt += $(this).attr('value') + ',';
                        }
                });

                hbt = hbt.replace(/\s*\,\s*$/,'');
                return hbt;	
            } 
                
            function getSelectedSortBy() {
                var sortBy = ''; 
                 $('.sort_filter_label').each (function() {
                        if($(this).hasClass('active')) {
                        	console.log($(this));
                        	console.log($(this).attr('value'));
                                sortBy += $(this).attr('value') + ',';
                        }
                });

                sortBy = sortBy.replace(/\s*\,\s*$/,'');
                return sortBy;	
            } 
                
            function getSelectedSpeciesName() {
                var sName = ''; 
				sName = $("#speciesNameFilter").attr('value');
				if(sName) {
                	sName = sName.replace(/\s*\,\s*$/,'');
                	return sName;
                }	
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
                
                $('#speciesGroupFilter button').click(function(){
                	$('#speciesGroupFilter button.active').removeClass('active');
                	$(this).addClass('active');
                    updateGallery(undefined, ${queryParams.max}, 0);
                    return false;
                });
                
                $('#habitatFilter button').click(function(){
                	$('#habitatFilter button.active').removeClass('active');
                	$(this).addClass('active');
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
         
                $("li.tagit-choice").click(function(){
                    var tg = $(this).contents().first().text();
                    window.location.href = "${g.createLink(action: 'list')}/?tag=" + tg ;
                });
                
                $(".snippet.tablet").live('hover', function(e){
                    if(e.type == 'mouseenter'){    
                        $(".figure", this).slideUp("fast");   
                        $(".all_content", this).hide();   
                        $('.observation_info_wrapper', this).slideDown("fast"); 
                    }
                
                    if(e.type == 'mouseleave'){    
                        $(".figure", this).slideDown("fast");   
                        $(".all_content", this).show();   
                        $('.observation_info_wrapper', this).slideUp("fast"); 
                    }


                   });

            });
        </g:javascript>
	<script>
		function showMapView() {
			$('#observations_list_map').slideToggle(function() {

				if ($(this).is(':hidden')) {
					$('div.observations > div.observations_list').show();
					$('#map_view_bttn').css('background-color', 'transparent');
				} else {
					$('div.observations > div.observations_list').hide();
				}

				google.maps.event.trigger(big_map, 'resize');
				big_map.setCenter(nagpur_latlng);
			});
		}
	</script>
</body>
</html>
