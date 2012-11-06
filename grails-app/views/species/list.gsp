<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'species', action:'list')}" />
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />


<r:require modules="species_list" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Species List</title>

</head>
<body>
	<div class="span12">
		<s:showSubmenuTemplate model="['entityName':'Species']" />
		<div class="tabbable">
			<ul class="nav nav-tabs pull-right" style="margin-bottom: 0px">
				<li class="active"><a href="#list" data-toggle="tab">Gallery</a>
				</li>
				<li><a href="#contribute" data-toggle="tab">Contribute</a>
				</li>
			</ul>

			<div class="tab-content">
				<div id="list" class="tab-pane active">
					<div class="gallerytoolbar">
						<div class="filters"
							style="position: relative; overflow: visible;">
							<div class="paginateButtons">
								<center>
									<p:paginateOnAlphabet controller="species" action="list" total="${speciesInstanceTotal}"  userGroup="${userGroup }" userGroupWebaddress="${userGroupWebaddress}"/>
								</center>
							</div>
							<div class="btn-group" style="float: right; z-index: 10">
								<button id="selected_sort" class="btn dropdown-toggle"
									data-toggle="dropdown" href="#" rel="tooltip"
									data-original-title="Sort by">

									<g:if test="${params.sort == 'title'}">
                                               Sort by Title
                                            </g:if>
									<g:else>
                                               Sort by Richness
                                            </g:else>
									<span class="caret"></span>
								</button>
								<ul id="sortFilter" class="dropdown-menu">
									<li class="group_option"><a class="sort_filter_label"
										value="title"> Sort by Title </a></li>
									<li class="group_option"><a class="sort_filter_label"
										value="percentOfInfo">Sort by Richness </a></li>
								</ul>
							</div>

							<obv:showGroupFilter model="['hideHabitatFilter':true]"/>

						</div>
					</div>


					<div align="center" class="observations thumbwrap">
						
						<ul class="grid_view thumbnails" style="list-style: none; text-align: left">
							<g:each in="${speciesInstanceList}" status="i"
								var="speciesInstance">



								<g:if test="${i%3 == 0}">
									<li
										class=" pull-left thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}"
										style="clear: both;margin:5px">
								</g:if>
								<g:else>
									<li
										class="pull-left thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}"
										style="margin:5px">
								</g:else>
								<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
								<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.SMALL, null)%>
								<div class="snippet tablet" style="width: 285px;height: auto;">
									<a href="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, userGroup:userGroup, userGroupWebaddress:userGroupWebaddress])}">
										

											<g:if test="${thumbnailPath }">
												<img class="icon pull-right"
													src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
													title=" ${speciesInstance.taxonConcept.name}" />
											</g:if>
											<g:else>
												<img class="icon pull-right" title="${speciesInstance.taxonConcept.name}"
													src="${createLinkTo(dir: 'images', file:speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL)?.fileName, absolute:true)}"
													style="float: right;"></img>
											</g:else>
											<p class="caption">${speciesInstance.taxonConcept.italicisedForm}</p>
										</a>
									
										
										<div class="poor_species_content" style="display: none;">No
											information yet</div>
									
								</div>
								</li>
							</g:each>
						</ul>
					</div>
					<br />

					<div class="paginateButtons span11">
						<center>
							<p:paginate controller="species" action="list" total="${speciesInstanceTotal}"   userGroup="${userGroup}" userGroupWebaddress="${userGroupWebaddress}"
								params="['startsWith':params.startsWith]" max="${params.max }" offset="${params.offset}" maxsteps="10" />
						</center>
					</div>
					<br />

					<div class="paginateButtons span11">
						<center>
							<p:paginateOnAlphabet controller="species" action="list" total="${speciesInstanceTotal}"  userGroup="${userGroup }" userGroupWebaddress="${userGroupWebaddress}"/>
						</center>
					</div>
				</div>
				<div id="contribute" class="tab-pane">
					<g:include controller="species" action="contribute" />
				</div>
			</div>
		</div>
	</div>
	
<g:javascript>
$(document).ready(function(){
	window.params = {
	<%
		params.each { key, value ->
			println '"'+key+'":"'+value+'",'
		}
	%>
		"tagsLink":"${g.createLink(action: 'tags')}",
		"queryParamsMax":"${params.max}",
		"isGalleryUpdate":false,
		"offset":0
	}
});
</g:javascript>
<r:script>

$(document).ready(function(){
	
	$('#speciesGallerySort').change(function(){
		updateGallery(window.location.pathname + window.location.search, ${params.limit?:51}, 0, undefined, false);
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
<style>


.snippet.tablet .figure img {
	height: auto;
}

.figure .thumbnail {
	height: 120px;
	margin: 0 auto;
	text-align: center;
	*font-size: 120px;
	line-height: 120px;
}

.thumbnails > li {
	margin: 0 0 18px 10px;
}
</style>
	
</body>
</html>
