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
<g:set var="title" value="Species"/>
<g:render template="/common/titleTemplate" model="['title':title, 'description':'', 'canonicalUrl':canonicalUrl, 'imagePath':'']"/>

<r:require modules="species"/>
<r:require modules="species_list" />

</head>
<body>

	<div class="span12">
		<s:showSubmenuTemplate model="['entityName':'Species']" />
                    <uGroup:rightSidebar/>
                    <obv:showRelatedStory
            model="['controller':params.controller, 'action':'related', 'filterProperty': 'featureBy', 'filterPropertyValue': true, 'id':'featureBy', 'userGroupInstance':userGroupInstance]" />
            <h5>Browse Species</h5>

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
						<uGroup:objectPostToGroupsWrapper model="['objectType':Species.class.canonicalName, canPullResource:canPullResource]"/>
						<div class="observations_list_wrapper" style="top: 0px;">
							<s:showSpeciesList/>
						</div>
				</div>
				<div id="contribute" class="tab-pane">
                                    <g:render template="contributeTemplate"/>
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
