<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>

<div class="observations_list" style="clear: both; top: 0px;">


	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">

			<ul class="grid_view thumbnails"
				style="list-style: none; text-align: left">
				<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">

					<g:if test="${i%6 == 0}">
						<li
							class="clearfix thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}">
					</g:if>
					<g:else>
						<li
							class="thumbnail ${speciesInstance.percentOfInfo > 0?'rich_species_content':'poor_species_content'}">
					</g:else>
					<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
					<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.NORMAL, null)%>

					<div class="snippet tablet">
                                                <div class="figure">
							<a
								href="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, userGroup:userGroup, userGroupWebaddress:userGroupWebaddress])}">


								<g:if test="${thumbnailPath }">
									<img class="img-polaroid"
										src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
										title=" ${speciesInstance.taxonConcept.name.replaceAll('<.*>','')}" />
								</g:if> <g:else>
									<img class="img-polaroid" style="opacity:0.7;"
										title="${speciesInstance.taxonConcept.name.replaceAll('<.*>','')}"
										src="${createLinkTo(dir: '', file:speciesInstance.fetchSpeciesGroupIcon(ImageType.NORMAL)?.fileName, absolute:true)}"></img>
								</g:else> </a>
						</div>
						<uGroup:objectPost model="['objectInstance':speciesInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
						<a
							href="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, userGroup:userGroup, userGroupWebaddress:userGroupWebaddress])}" style="display:block;">
							<span class="species_story ellipsis multiline sci_name"
							style="display: block;height:6=50px;" title="${speciesInstance.taxonConcept.name.replaceAll('<.*>','')}">${speciesInstance.taxonConcept.name.trim()}</span> </a>
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
			userGroupWebaddress="${userGroupWebaddress}" params="${params}"
			max="${params.max }" offset="${params.offset}" maxsteps="10" />
	</div>
	<div class="paginateButtons centered">
		<p:paginateOnAlphabet controller="species" action="list"
			total="${instanceTotal}" userGroup="${userGroup }" params="${params}"
			userGroupWebaddress="${userGroupWebaddress}"/>

	</div>


</div>

