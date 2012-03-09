
<%@ page import="species.participation.Observation"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
</head>
<body>
	<div class="container_16">
		<div class="grid_16 big_wrapper">
			<h1>
				<g:message code="default.list.label" args="[entityName]" />
			</h1>
			<g:set var="carouselId" value="a" />
			<!-- obv:showRelatedStory model="['observationId': null, 'controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'speciesGroup' , 'filterPropertyValue': 830 ,'id':carouselId]" /-->
			<obv:showGroupFilter model="['observationInstance':observationInstance]" />
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
			<div class="list">
					<div class="observations thumbwrap grid_16">
						<div class="observation">
							<g:each in="${observationInstanceList}" status="i"
								var="observationInstance">
								<obv:showSnippet model="['observationInstance':observationInstance]"></obv:showSnippet>
							</g:each>
						</div>
					</div>
				</div>
			<div class="paginateButtons"  style="clear:both">
				<g:paginate total="${observationInstanceTotal}" max="2"/>
			</div>
		</div>
	</div>
	<g:javascript>	
	$(document).ready(function(){
		$( "#speciesGroupFilter" ).buttonset();
		
		$('#speciesGroupFilter label[value$="${params.sGroup}"]').each (function() {
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
		
		$(".paginateButtons a").click(function() {
			updateGallery($(this).attr('href'));
			return false;
		});
		
		$("ul[name='tags']").tagit({select:true,  tagSource: "${g.createLink(action: 'tags')}"});
	});
	</g:javascript>
</body>
</html>
