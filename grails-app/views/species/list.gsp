<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />
<r:require modules="species_list"/>


<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Species List</title>
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
</head>
<body>
		<div class="container_16 big_wrapper outer_wrapper">
		<s:showSubmenuTemplate model="['entityName':'Species']"/>
		<div class="gallerytoolbar grid_16" >
			<div class="filters" style="position: relative;overflow:visible;">
				<div class="paginateButtons">
					<center>
						<g:paginateOnAlphabet total="${speciesInstanceTotal}" />
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
						<li class="group_option"><a class=" sort_filter_label"
							value="title"> Sort by Title </a></li>
						<li class="group_option"><a class=" sort_filter_label"
							value="percentOfInfo">Sort by Richness </a></li>
					</ul>
				</div>
			
				<obv:showGroupFilter />
				
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
