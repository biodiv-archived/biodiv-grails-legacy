<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />
<r:require modules="species"/>


<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Species List</title>
<r:script>

$(document).ready(function(){

	$("#speciesGroupFilter").buttonset();
	$("#speciesGroupFilter label").each(function() {
		$(this).hover(function() {
			$(this).css('backgroundPosition', '0px -32px');
		}, function() {
			if($(this).attr('value') == '${params.sGroup}') {
				$(this).css('backgroundPosition', '0px -64px');
			} else {
				$(this).css('backgroundPosition', '0px 0px');
			}
		});
	})
		

	$('#speciesGroupFilter label[value$="${params.sGroup}"]').attr('aria-pressed', 'true').addClass('ui-state-hover').addClass('ui-state-active').css('backgroundPosition', '0px -64px');
	
	
	function updateGallery(target, limit, offset) {
		var a = $('<a href="'+target+'"></a>');
		var url = a.url();
		var href = url.attr('path');
		var params = url.param();
		params['sort'] = $('#speciesGallerySort option:selected').val()
		//params['order'] = $('#speciesGalleryOrder option:selected').val()
		var grp = ''; 
		$('#speciesGroupFilter label').each (function() {
			if($(this).attr('aria-pressed') === 'true') {
				grp += $(this).attr('value') + ',';
			}
		});
		
		grp = grp.replace(/\s*\,\s*$/,'');
		if(grp) {
			params['sGroup'] = grp;//$('#speciesGalleryFilter option:selected').val().toString();
		}
		
		if(limit != undefined) {
			params['max'] = limit.toString();
		}
		if(offset != undefined) {
			params['offset'] = offset.toString();
		}
		var recursiveDecoded = decodeURIComponent($.param(params));
		window.location = href+'?'+recursiveDecoded;
	}
	
	$(".paginateButtons a").click(function() {
		updateGallery($(this).attr('href'));
		return false;
	});
	
	$('#speciesGallerySort').change(function(){
		updateGallery(window.location.pathname + window.location.search, ${params.limit?:51}, 0);
		return false;
	});
	
	$('#speciesGroupFilter input').change(function(){
		updateGallery(window.location.pathname + window.location.search, ${params.limit?:51}, 0);
		return false;
	});
	
	$('li.poor_species_content').hover(function(){
		$(this).children('.poor_species_content').slideDown(200);
	}, function(){
		$(this).children('.poor_species_content').slideUp(200);
	}
	);
});
</r:script>
</head>
<body>
		<div class="container_16 big_wrapper outer_wrapper">
				<div class="page-header clearfix">
						<h1>
							<g:message code="default.species.heading" default="Species" />
						</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>


		<div class="paginateButtons grid_16">
			<center>
				<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
			</center>
		</div>
		<br />
		<br />

		<div class="gallerytoolbar grid_16" >
			<div id="speciesGroupFilter" class="filterBar" style="float: left;">
				<center>
					<!-- g:paginateOnSpeciesGroup/-->
					<%othersGroup = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS)%>

					<g:each in="${SpeciesGroup.list() }" var="sGroup" status="i">
						<g:if test="${sGroup != othersGroup }">
							<input type="radio" name="specuesGroupFilter"
								id="speciesGroupFilter${i}" value="${sGroup.id }" />
							<label for="speciesGroupFilter${i}" value="${sGroup.id}"
								title="${sGroup.name}" class="group_icon"
								style="background: url('${createLinkTo(dir: 'images', file: sGroup.icon(ImageType.SMALL)?.fileName, absolute:true)}') no-repeat; background-position: 0 0; width: 32px; height: 32px; margin-left: 6px;">
							</label>
						</g:if>
					</g:each>

					<input type="radio" name="specuesGroupFilter"
						id="specuesGroupFilter20" value="${othersGroup.id}" /> <label
						for="speciesGroupFilter${i}" value="${othersGroup.id}"
						title="${othersGroup.name}" class="group_icon"
						style="background: url('${createLinkTo(dir: 'images', file: othersGroup.icon(ImageType.SMALL)?.fileName, absolute:true)}') no-repeat; background-position: 0 0; width: 32px; height: 32px; margin-left: 6px;">
					</label>
				</center>
			</div>

			<div style="float: right;">
				Sort by <select name="speciesGallerySort" id="speciesGallerySort"
					class="value ui-corner-all">
					<option value="title"
						${params.sort?.equals('title')?'selected':'' }>Title</option>
					<option value="percentOfInfo"
						${params.sort?.equals('percentOfInfo')?'selected':''  }>Richness</option>
					<!-- option value="createdOn" ${params.sort?.equals('createdOn')?'selected':''  }>Recently Added</option>
				<option value="updatedOn" ${params.sort?.equals('updatedOn')?'selected':''  }>Recently Updated</option-->
				</select>
				<!-- Filter by group <select name="speciesGalleryFilter"
				id="speciesGalleryFilter" multiple="multiple"
				class="value ui-corner-all">
				<g:each in="${SpeciesGroup.list()}" var="sGroup">
					<option value="${sGroup.id}"
						${params.sGroup.equals(sGroup)?'selected':''  }>
						${sGroup.name}
					</option>
				</g:each>
			</select> -->
			</div>
		</div>
		
		<br /><br/><br/>
		
		<div class="grid_16" align="center">
			<g:set var="columnSize"
				value="${Math.ceil(speciesInstanceList.size()/3)}" />
			<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">

					<ul class="speciesList thumbwrap grid_5"
						style="list-style: none; text-align: left">
				<g:if test="${speciesInstance.percentOfInfo > 0}">		
					<li class="grid_5 rich_species_content">
				</g:if>
				<g:else>
					<li class="grid_5 poor_species_content">
				</g:else>
					<g:link action="show" id="${speciesInstance.id}">
						<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
						<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.SMALL, null)%>
						<g:if test="${thumbnailPath }">
						<img class="icon" style="float: right;"
							src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
							title=" ${speciesInstance.taxonConcept.name}" />
						</g:if><g:else>
							<img class="icon group_icon"
								title="${speciesInstance.taxonConcept.name}"
								src= "${createLinkTo(dir: 'images', file:speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL)?.fileName, absolute:true)}" 
								style="float: right;"></img>
						</g:else>
						<p class="caption">
							${speciesInstance.taxonConcept.italicisedForm}
						</p>
					</g:link>
					<div class="poor_species_content" style="display:none;">No information yet</div>
				</li>
					</ul>
			</g:each>
			</ul>
		</div>
		<br />

		<div class="paginateButtons  grid_16">
			<center>
				<g:paginate total="${speciesInstanceTotal}"
					params="['startsWith':params.startsWith]" max="50" maxsteps="10" />
			</center>
		</div>
		<br />

		<div class="paginateButtons grid_16">
			<center>
				<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
			</center>
		</div>

	</div>

</body>
</html>
