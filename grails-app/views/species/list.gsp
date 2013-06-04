<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>

<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'species', action:'list', base:Utils.getIBPServerDomain()])}" />
<g:set var="title" value="List"/>
<g:render template="/common/titleTemplate" model="['title':title, 'description':'', 'canonicalUrl':canonicalUrl, 'imagePath':'']"/>
<title>${title} | ${params.controller.capitalize()} | ${Utils.getDomainName(request)}</title>

<r:require modules="species_list" />

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
						<sUser:isAdmin>
							<s:showDownloadAction model="['source':'Species', 'requestObject':request ]" />
						</sUser:isAdmin> 
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
