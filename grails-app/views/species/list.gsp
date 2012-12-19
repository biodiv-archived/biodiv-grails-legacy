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
		<uGroup:rightSidebar/>
		<div class="tabbable" style="margin-left:0px;clear:both;">
			<ul class="nav nav-tabs" style="margin-bottom: 0px">
				<li class="active"><a href="#list" data-toggle="tab">Gallery</a>
				</li>
				<li><a href="#contribute" data-toggle="tab">Contribute</a>
				</li>
			</ul>

			<div class="tab-content">
				<div id="list" class="tab-pane active">
						<s:speciesFilter></s:speciesFilter>
						<s:showSpeciesList></s:showSpeciesList>
				</div>
				<div id="contribute" class="tab-pane">
					<g:include controller="species" action="contribute" />
				</div>
			</div>
		</div>
	</div>
	
	<g:javascript>
		$(document).ready(function(){
			window.params.tagsLink = "${uGroup.createLink(controller:'species', action: 'tags')}"
		});
	</g:javascript>
	<r:script>
	
	$(document).ready(function() {
		$('#speciesGallerySort').change(function(){
			updateGallery(window.location.pathname + window.location.search, ${params.limit?:51}, 0, undefined, false);
			return false;
		});
		
		$('li.poor_species_content').hover(function(){
			$(this).children('.poor_species_content').slideDown(200);
		}, function(){
			$(this).children('.poor_species_content').slideUp(200);
		});
		$(".grid_view").show();
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
