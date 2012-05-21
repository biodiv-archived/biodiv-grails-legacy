<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>

<div class="filters" style="position: relative;">
	<obv:showGroupFilter
		model="['observationInstance':observationInstance]" />
</div>

<div class="tags_section span3" style="float: right;">
	<div style="clear:both;">
		<obv:identificationByEmail model="['source':'observationList', 'requestObject':request]" />
		<br />
	</div>
	<g:if test="${params.action == 'search' }">
		<obv:showAllTags
			model="['tags':tags , 'count':tags?tags.size():0, 'isAjaxLoad':true]" />
	</g:if>
	<g:else>
		<obv:showAllTags
			model="['tagFilterByProperty':'All' , 'params':params, 'isAjaxLoad':true]" />
	</g:else>
</div>
<div class="row">
	<!-- main_content -->
	<div class="list span9">

		<div class="observations thumbwrap">
			<div class="observation">
				<div>
					<obv:showObservationFilterMessage
						model="['observationInstanceTotal':observationInstanceTotal, 'queryParams':queryParams]" />
				</div>
				<div style="clear: both;"></div>
				<!-- needs to be fixed -->
				<div id="map_view_bttn" class="btn-group">
					<a class="btn btn-success dropdown-toggle" data-toggle="dropdown"
						href="#"
						onclick="$(this).parent().css('background-color', '#9acc57'); showMapView(); return false;">
						Map view <span class="caret"></span> </a>
				</div>
				<div class="btn-group" style="float: left; z-index: 10">
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
							value="createdOn"> Latest </a>
						</li>
						<li class="group_option"><a class=" sort_filter_label"
							value="lastRevised"> Last Updated </a>
						</li>
						<g:if test="${params.action == 'search'}">
							<li class="group_option"><a class=" sort_filter_label"
								value="score"> Relevancy </a>
							</li>
						</g:if>
						<g:else>
							<li class="group_option"><a class=" sort_filter_label"
								value="visitCount"> Most Viewed </a>
							</li>
						</g:else>
					</ul>


				</div>
				
				<div id="observations_list_map" class="observation"
					style="clear: both; display: none;">
					<obv:showObservationsLocation
						model="['observationInstanceList':totalObservationInstanceList]">
					</obv:showObservationsLocation>
				</div>
			</div>

			<obv:showObservationsList />
		</div>
	</div>

	<!-- main_content end -->
</div>


<!--container end-->
	<g:javascript>	
        $(document).ready(function(){
        	$('#selected_sort').tooltip({placement:'right'});
            $('button').tooltip();
            $('.dropdown-toggle').dropdown();
            	
            $('#speciesNameFilter').button();
            if(${params.speciesName == 'Unknown' }){
				$("#speciesNameFilterButton").addClass('active');
				$("#speciesNameAllButton").removeClass('active');
			}else{
				$("#speciesNameFilterButton").removeClass('active');
				$("#speciesNameAllButton").addClass('active');
            }
        	$('#observationFlagFilter').button();
            if(${params.isFlagged == 'true' }){
				$("#observationFlaggedButton").addClass('active');
				$("#observationWithNoFlagFilterButton").removeClass('active')
			}else{
				$("#observationFlaggedButton").removeClass('active');
				$("#observationWithNoFlagFilterButton").addClass('active')
			}
   
           
           $("#observationFlaggedButton").click(function() {
           		if($("#observationFlaggedButton").hasClass('active')){
           			return false;
           		}
				$("#observationFlagFilter").val('true')
				$("#observationWithNoFlagFilterButton").removeClass('active')
				$("#observationFlaggedButton").addClass('active')
				
				updateGallery(undefined, ${queryParams.max}, 0);
                return false;
			});
			
			$("#observationWithNoFlagFilterButton").click(function() {
           		if($("#observationWithNoFlagFilterButton").hasClass('active')){
           			return false;
           		}
				$("#observationFlagFilter").val('false')
				$("#observationFlaggedButton").removeClass('active')
				$("#observationWithNoFlagFilterButton").addClass('active')
				
				updateGallery(undefined, ${queryParams.max}, 0);
                return false;
			});
			
			$("#speciesNameAllButton").click(function() {
           		if($("#speciesNameAllButton").hasClass('active')){
           			return false;
           		}
				$("#speciesNameFilter").val('All')
				$("#speciesNameFilterButton").removeClass('active')
				$("#speciesNameAllButton").addClass('active')
				
				updateGallery(undefined, ${queryParams.max}, 0);
                return false;
			});
			
			$("#speciesNameFilterButton").click(function() {
				if($("#speciesNameFilterButton").hasClass('active')){
           			return false;
           		}
			    $("#speciesNameFilter").val('Unknown')
				$("#speciesNameFilterButton").addClass('active')
			    $("#speciesNameAllButton").removeClass('active')
					
				updateGallery(undefined, ${queryParams.max}, 0);
                return false;
			});
			                
        	$('#speciesGroupFilter button').click(function(){
        		if($(this).hasClass('active')){
        			return false;
        		}
        		$('#speciesGroupFilter button.active').removeClass('active').css('backgroundPosition', '0px 0px');
            	$(this).addClass('active').css('backgroundPosition', '0px -64px');
            	updateGallery(undefined, ${queryParams.max}, 0);
            	return false;
         	});
                
         	$('#habitatFilter button').click(function(){
         		if($(this).hasClass('active')){
        			return false;
        		}
         		$('#habitatFilter button.active').removeClass('active').css('backgroundPosition', '0px 0px');
            	$(this).addClass('active').css('backgroundPosition', '0px -64px');
            	updateGallery(undefined, ${queryParams.max}, 0);
            	return false;
         	});
                
           $('.sort_filter_label').click(function(){
           		var caret = '<span class="caret"></span>'
           		if(stringTrim(($(this).html())) == stringTrim($("#selected_sort").html().replace(caret, ''))){
           			$("#sortFilter").hide();
                	return false;
        		}
        		$('.sort_filter_label.active').removeClass('active');
        		$(this).addClass('active');
                $("#selected_sort").html($(this).html() + caret);
                $("#sortFilter").hide();
                updateGallery(undefined, ${queryParams.max}, 0);
                return false;   
           });

			$("#selected_sort").click(function(){
				$("#sortFilter").show();
			});
			
<%--                $('#speciesNameFilter input').change(function(){--%>
<%--                	alert(" should not called");--%>
<%--                    updateGallery(undefined, ${queryParams.max}, 0);--%>
<%--                    return false;--%>
<%--                });--%>
<%--        --%>
                
                $(".paginateButtons a").click(function() {
                    updateGallery($(this).attr('href'));
                    return false;
                });
                
                $("ul[name='tags']").tagit({select:true,  tagSource: "${g.createLink(action: 'tags')}"});
         
          		
                $("li.tagit-choice").live('click', function(){
               		setActiveTag($(this).contents().first().text());
                	updateGallery(undefined, undefined, 0);
                	return false;
                });
               
               $('#tc_tagcloud a').live('click', function(){
               		setActiveTag($(this).contents().first().text());
		 			updateGallery(undefined, undefined, 0);
					return false;
			   });
               
                $("#removeTagFilter").live('click', function(){
                	var oldActiveTag = $("li.tagit-choice.active");
		 			if(oldActiveTag){
		 				oldActiveTag.removeClass('active');
		 			}
		 			var oldActiveTag = $("#tc_tagcloud a.active");
		 			if(oldActiveTag){
		 				oldActiveTag.removeClass('active');
		 			}
		 			updateGallery(undefined, undefined, 0);
                	return false;
                });
                
                $("#removeUserFilter").live('click', function(){
                	updateGallery(undefined, undefined, 0, true);
                	return false;
                });
                
                $("#removeQueryFilter").live('click', function(){
                	$( "#searchTextField" ).val('');
                	updateGallery(undefined, undefined, 0);
                	return false;
                });
               
               var tmpTarget =  window.location.pathname + window.location.search;
               setActiveTag($('<a href="'+ tmpTarget +'"></a>').url().param()["tag"]);
              
            });
            
            function stringTrim(s){
           		return $.trim(s);
            }
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
	                        sortBy += $(this).attr('value') + ',';
                        }
                });

                sortBy = sortBy.replace(/\s*\,\s*$/,'');
                return sortBy;	
            } 
            
            function getSelectedFlag() {
                var flag = ''; 
				flag = $("#observationFlagFilter").attr('value');
				if(flag) {
                	flag = flag.replace(/\s*\,\s*$/,'');
                	return flag;
                }	
            } 
                
            function getSelectedSpeciesName() {
                var sName = ''; 
				sName = $("#speciesNameFilter").attr('value');
				if(sName) {
                	sName = sName.replace(/\s*\,\s*$/,'');
                	return sName;
                }	
            } 
				
			function getSelectedTag() {
				var tag = ''; 
				tag = $("li.tagit-choice.active").contents().first().text();
				if(!tag){
					tag = $("#tc_tagcloud a.active").contents().first().text();	
				}
				if(tag) {
                	tag = stringTrim(tag.replace(/\s*\,\s*$/,''));
                	return tag;
                }	
            } 
            
	        function getFilterParameters(url, limit, offset, removeUser) {
                    var params = url.param();
                    var sortBy = getSelectedSortBy();
                    if(sortBy) {
                            params['sort'] = sortBy;
                    }

                    var sName = getSelectedSpeciesName();
                    if(sName) {
                            params['speciesName'] = sName;
                    }
					
					var flag = getSelectedFlag();
                    if(flag) {
                            params['isFlagged'] = flag;
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
					
					var tag = getSelectedTag();
					if(tag){
						params['tag'] = tag;
					}else{
						//removing old tag from url
						if(params['tag'] != undefined){
							delete params['tag'];
						}
					}
					
					var query = $( "#searchTextField" ).val();
					if(query){
						params['query'] = query;
					}else{
						//removing old tag from url
						if(params['query'] != undefined){
							delete params['query'];
						}
					}
					
					if(removeUser){
						if(params['user'] != undefined){
							delete params['user'];
						}
					}
					
					return params;
                }	
<%--                --%>
		<%--                History.Adapter.bind(window,'statechange',function(){ // Note: We are using statechange instead of popstate--%>
<%--        			var State = History.getState(); // Note: We are using History.getState() instead of event.state--%>
<%--        			History.log(State.data, State.title, State.url);--%>
<%--        			alert(JSON.stringify(State));--%>
<%--        			alert("data " + JSON.stringify(State.data) + "  title "  + State.title + "  url " + State.url);--%>
<%--        			//window.location.href = State.url;--%>
<%--    			});--%>

		<%--                window.onpopstate = function(event) {  --%>
<%--                	alert(event);--%>
<%--  					alert("location: " + document.location + ", state: " + JSON.stringify(event.state));--%>
<%--  					if(event.state !== undefined){--%>
<%--  						window.location = document.location;--%>
<%--  					} --%>
<%--				};--%>
		<%--                $(window).bind('statechange',function(){--%>
<%--                	alert("state changed " );--%>
<%--                });--%>
		<%--                --%>
				function setActiveTag(activeTag){
					if(activeTag != undefined){
 							$('li.tagit-choice').each (function() {
 								if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
                       				$(this).addClass('active');
                       			}
                       			else{
                       				if($(this).hasClass('active')){
                       					$(this).removeClass('active');
                       				}
                       			}
                       		});
                       		
                       		$('#tc_tagcloud a').each(function() {
 								if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
                       				$(this).addClass('active');
                       			}else{
                       				if($(this).hasClass('active')){
                       					$(this).removeClass('active');
                       				}
                       			}
               				});
               				
				 		}
				}
				
                function updateListPage(activeTag) {
  					return function (data) {
  						$('.observations_list').replaceWith(data.obvListHtml);
						$('#info-message').replaceWith(data.obvFilterMsgHtml);
  						$('#tags_section').replaceWith(data.tagsHtml);
  						$('.observation_location_wrapper').replaceWith(data.mapViewHtml);
  						setActiveTag(activeTag);
					}
				}
                
                function updateGallery(target, limit, offset, removeUser, isGalleryUpdate) {
                    if(target === undefined) {
                            target = window.location.pathname + window.location.search;
                    }
                    
                    var a = $('<a href="'+target+'"></a>');
                    var url = a.url();
                    var href = url.attr('path');
                    var params = getFilterParameters(url, limit, offset, removeUser);
                    //alert(" tag in params " + params['tag'] );
                    isGalleryUpdate = (isGalleryUpdate == undefined)?true:isGalleryUpdate
                    if(isGalleryUpdate)
                    	params["isGalleryUpdate"] = isGalleryUpdate;
                    var recursiveDecoded = decodeURIComponent($.param(params));
                    
                    var doc_url = href+'?'+recursiveDecoded;
                    var History = window.History;
                    delete params["isGalleryUpdate"]
                    History.pushState({state:1}, "Species Portal", '?'+decodeURIComponent($.param(params))); 
                    //alert("doc_url " + doc_url);
                    if(isGalleryUpdate) {
	                   	$.ajax({
	  						url: doc_url,
	  						dataType: 'json',
	  						
	  						beforeSend : function(){
	  							$('.observations_list').css({"opacity": 0.5});
	  							$('#tags_section').css({"opacity": 0.5});
	  						},
	  						
	  						success: updateListPage(params["tag"]),
							statusCode: {
		    					401: function() {
		    						show_login_dialog();
		    					}	    				    			
		    				},
		    				error: function(xhr, status, error) {
		    					var msg = $.parseJSON(xhr.responseText);
		    					$('.message').html(msg);
							}
						});
					} else {
						window.location = doc_url;
					}
                }
           
                $( "#search" ).click(function() {                		
						updateGallery(undefined, ${queryParams.max}, 0);
                		return false;
				});
				
		function showMapView() {
			$('#observations_list_map').slideToggle(function() {

				if ($(this).is(':hidden')) {
					$('div.observations > div.observations_list').show();
					$('#map_view_bttn').css('background-color', 'transparent');
					$('#map_results_list > div.observations_list').remove();
					updateGallery(undefined, undefined, 0);
					//alert(" hiding map view");
					
				} else {
					$('div.observations > div.observations_list').hide();
					$('div.observations > div.observations_list').html('');
					//alert("showing map view");
				}

				google.maps.event.trigger(big_map, 'resize');
				big_map.setCenter(nagpur_latlng);
			});
		}
</g:javascript>
