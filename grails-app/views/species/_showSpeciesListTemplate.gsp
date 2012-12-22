<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>

<div class="observations_list" style="clear: both;top:0px;">


	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">

			<ul class="grid_view thumbnails"
				style="list-style: none; text-align: left">
				<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">



					<g:if test="${i%4 == 0}">
						<li
							class=" pull-left thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}"
							style="clear: both; margin: 5px 0px 5px 5px">
					</g:if>
					<g:else>
						<li
							class="pull-left thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}"
							style="margin: 5px 0px 5px 5px">
					</g:else>
					<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
					<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.SMALL, null)%>
					<div class="snippet tablet" style="height: 80px; ">
						<a
							href="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, userGroup:userGroup, userGroupWebaddress:userGroupWebaddress])}">


							<g:if test="${thumbnailPath }">
								<img class="icon pull-right"
									src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
									title=" ${speciesInstance.taxonConcept.name}" />
							</g:if> <g:else>
								<img class="icon pull-right"
									title="${speciesInstance.taxonConcept.name}"
									src="${createLinkTo(dir: 'images', file:speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL)?.fileName, absolute:true)}"
									style="float: right;"></img>
							</g:else>
							<p class="caption ellipsis multiline" title="${speciesInstance.taxonConcept.name}">
								${speciesInstance.taxonConcept.italicisedForm}
							</p> </a>


						<div class="poor_species_content" style="display: none;">No
							information yet</div>

					</div>
					</li>
				</g:each>
			</ul>



		</div>
	</div>


	<div class="paginateButtons centered">
		<p:paginate controller="species" action="list"
			total="${instanceTotal}" userGroup="${userGroup}"
			userGroupWebaddress="${userGroupWebaddress}"
			params="['startsWith':params.startsWith]" max="${params.max }"
			offset="${params.offset}" maxsteps="10" />
	</div>
	<div class="paginateButtons centered">
		<p:paginateOnAlphabet controller="species" action="list"
			total="${instanceTotal}" userGroup="${userGroup }"
			userGroupWebaddress="${userGroupWebaddress}" />

	</div>


</div>

