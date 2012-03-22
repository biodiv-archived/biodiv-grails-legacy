
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
<g:javascript src="jquery.infinitescroll.js"
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
			
			<obv:showGroupFilter
				model="['observationInstance':observationInstance]" />
			
			<obv:showObservationsLocation
					model="['observationInstanceList':observationInstanceList]" />
				
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
												<g:if test="${queryParams.groupId}">
													of <span class="highlight">${SpeciesGroup.get(queryParams.groupId).name.toLowerCase()}</span> group
												</g:if>
												<g:if test="${queryParams.habitat}">
													in <span class="highlight">${Habitat.get(queryParams.habitat).name.toLowerCase()}</span> habitat
												</g:if>
												<g:if test="${queryParams.tag}">
													tagged <span class="highlight">${queryParams.tag}</span>
												</g:if>
											</div>
                                           <div id="list_view_bttn" class="list_style_button active"></div> 
                                           <div id="grid_view_bttn" class="grid_style_button"></div>
                                        </div>
					<div class="observation grid_11">
						<div class="mainContent">
		                                    <div class="grid_view">
								<g:each in="${observationInstanceList}" status="i"
									var="observationInstance">
									<obv:showSnippetTablet
										model="['observationInstance':observationInstance]"></obv:showSnippetTablet>
								</g:each>
                                                    </div>       
                                                    <div class="list_view" style="display:none;">
                                                                                <g:each in="${observationInstanceList}" status="i"
                                                                                        var="observationInstance">
                                                                                        <obv:showSnippet
                                                                                                model="['observationInstance':observationInstance]"></obv:showSnippet>
                                                                                </g:each>
                                                    </div>       
                                            </div>
                                            <g:if test="${observationInstanceTotal > queryParams.max}">
                                            	<div class="button loadMore"><span class="progress" style="display:none;"><img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"/>Loading ... </span><span class="buttonTitle">Load more</span></div>
                                            </g:if>
					</div>
					
					<div class="tags_section grid_4">
						<obv:showAllTags/>
						<div id="tagList" class="grid_4 sidebar_section" style="display:none;">
							<obv:showTagsList model="['tags':tags]" />
						</div>	
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
         
         $("li.tagit-choice").click(function(){
         	var tg = $(this).contents().first().text();
         	window.location.href = "${g.createLink(action: 'list')}/?tag=" + tg ;
         });
         
         $('#list_view_bttn').click(function(){
			$('.grid_view').hide();
			$('.list_view').show();
			$(this).addClass('active');
			$('#grid_view_bttn').removeClass('active');
			$.cookie("observation_listing", "list");
		});
		
		$('#grid_view_bttn').click(function(){
			$('.grid_view').show();
			$('.list_view').hide();
			$(this).addClass('active');
			$('#list_view_bttn').removeClass('active');
			$.cookie("observation_listing", "grid");
		});
		
		if ($.cookie("observation_listing") == "list") {
			$('.list_view').show();
			$('.grid_view').hide();
			$('#grid_view_bttn').removeClass('active');
			$('#list_view_bttn').addClass('active');
		}else{
			$('.grid_view').show();
			$('.list_view').hide();
			$('#grid_view_bttn').addClass('active');
			$('#list_view_bttn').removeClass('active');	
		}

		$.autopager({
                 
                    autoLoad: false,
    		    // a selector that matches a element of next page link
    		    link: 'div.paginateButtons a.nextLink',

    		    // a selector that matches page contents
    		    content: '.mainContent',
    		
    		    // a callback function to be triggered when loading start 
		    start: function(current, next) {
                        $(".loadMore .progress").show();
                        $(".loadMore .buttonTitle").hide();
		    },
		
		    // a function to be executed when next page was loaded. 
		    // "this" points to the element of loaded content.
		    load: function(current, next) {
		    			$(".mainContent:last").hide().fadeIn(3000);
                        if (next.url == undefined){
                            $(".loadMore").hide();
                        }else{
                            $(".loadMore .progress").hide();
                            $(".loadMore .buttonTitle").show();
                        }
		    }
		});
	
                $('.loadMore').click(function() {
                    $.autopager('load');
                    return false;
                });
	});
	</g:javascript>
</body>
</html>
