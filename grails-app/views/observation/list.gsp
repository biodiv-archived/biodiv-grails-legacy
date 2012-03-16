
<%@ page import="species.participation.Observation"%>
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
			
			<g:set var="carouselId" value="a" />
			<!-- obv:showRelatedStory model="['observationId': null, 'controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'speciesGroup' , 'filterPropertyValue': 830 ,'id':carouselId]" /-->
			
			<obv:showGroupFilter
				model="['observationInstance':observationInstance]" />
				
			
			<div style="clear:both"></div>
			<div class="list">
				<div class="observations thumbwrap">
					<div class="observation grid_11">
											<div>Observations : ${observationInstanceTotal}</div>
                                           <div id="list_view_bttn" class="list_style_button"></div> 
                                            <div id="grid_view_bttn" class="grid_style_button active"></div>
                                        </div>
					<div class="observation grid_11">
                                                <div id="grid_view" style="display:none;">
						<g:each in="${observationInstanceList}" status="i"
							var="observationInstance">
							<obv:showSnippetTablet
								model="['observationInstance':observationInstance]"></obv:showSnippetTablet>
						</g:each>
                                                </div>       
                                                <div id="list_view">
						<g:each in="${observationInstanceList}" status="i"
							var="observationInstance">
							<obv:showSnippet
								model="['observationInstance':observationInstance]"></obv:showSnippet>
						</g:each>
                                                </div>       
					</div>
					
					<div class="tags_section grid_4">
					<obv:showAllTags/>
					<obv:showGroupList/>
					</div>
				</div>
			</div>
			
			<div class="paginateButtons" style="clear: both">
				<g:paginate total="${observationInstanceTotal}" max="2" />
			</div>
		</div>
	</div>
	<g:javascript>	
	$(document).ready(function(){
		
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
		
		
		function getFilterParameters(url, limit, offset) {
			
			var params = url.param();
			
			if($('#speciesGallerySort').length > 0) {
				params['sort'] = $('#speciesGallerySort option:selected').val();
				//params['orderBy'] = $('#speciesGalleryOrder option:selected').val();
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
			updateGallery(undefined, 5, 0);
			return false;
		});
		
		$('#habitatFilter input').change(function(){
			updateGallery(undefined, 5, 0);
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
			$('#grid_view').hide();
			$('#list_view').show();
			$(this).addClass('active');
			$('#grid_view_bttn').removeClass('active');
			$.cookie("observation_listing", "list");
		});
		
		$('#grid_view_bttn').click(function(){
			$('#grid_view').show();
			$('#list_view').hide();
			$(this).addClass('active');
			$('#list_view_bttn').removeClass('active');
			$.cookie("observation_listing", "grid");
		});
		
		if ($.cookie("observation_listing") == "grid") {
			$('#grid_view').show();
			$('#list_view').hide();
			$('#grid_view_bttn').addClass('active');
			$('#list_view_bttn').removeClass('active');
		}else{
			$('#list_view').show();
			$('#grid_view').hide();
			$('#grid_view_bttn').removeClass('active');
			$('#list_view_bttn').addClass('active');
		
		}

	});
	</g:javascript>
</body>
</html>
