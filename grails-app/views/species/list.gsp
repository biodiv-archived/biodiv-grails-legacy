<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'species', action:'list')}" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="layout" content="main" />


<r:require modules="species_list" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Species List</title>
</head>
<body>
<style  type="text/css">
.thumbnails>.thumbnail {
	margin: 0 10px 10px 0px;
        width:inherit;
        }
</style>

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
						<div class="observations_list_wrapper" style="top: 0px;">
							<s:showSpeciesList></s:showSpeciesList>
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
			window.params.tagsLink = "${uGroup.createLink(controller:'species', action: 'tags')}";
			$('#speciesGallerySort').change(function(){
				updateGallery(window.location.pathname + window.location.search, ${params.limit?:40}, 0, undefined, false);
				return false;
			});
		});
		
	</g:javascript>
	<r:script>
		$('.observations_list_wrapper').on('updatedGallery', function(event) {
    		$(".grid_view").show();
    	});
		
	</r:script>
</body>
</html>
