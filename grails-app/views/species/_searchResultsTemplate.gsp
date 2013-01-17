<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.Species"%>
<%@ page import="species.Language"%>
<%@ page import="species.CommonNames"%>

<div class="observations_list observation" style="top: 0px;">

<div class="mainContentList">
	<div class="mainContent">

		<ul class="list_view thumbnails">

			<g:if test="${instanceTotal > 0}">
				<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">
					<li class="thumbnail clearfix">
						<div class="figure span3 observation_story_image"
							style="display: table; height: 220px;" title="">
							<a
								href="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}">
								<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
								<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.NORMAL, null)%>

								<g:if test="${thumbnailPath }">
									<img
										src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
										title="${speciesInstance.taxonConcept.name }" />
								</g:if> <g:else>
									<img class="group_icon" style="opacity: 0.4;"
										title="${speciesInstance.taxonConcept.name}"
										src="${createLinkTo(dir:'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.NORMAL)?.fileName, absolute:true)}" />
								</g:else> </a>
						</div>
						<div class="observation_story"
							style="overflow: visible; width: 100%">

							<div class="observation-icons">
								<g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
									<s:showThreatenedStatus
										model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]" />
								</g:if>
								<span
									class="group_icon species_groups_sprites active ${speciesInstance.fetchSpeciesGroup()?.iconClass()}"
									title="${speciesInstance.fetchSpeciesGroup()?.name}"></span>
							</div>

							<h6>
								<g:link class="ellipsis"
									url="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}">
									${speciesInstance.taxonConcept.italicisedForm }
								</g:link>
							</h6>
							<%def engCommonName=CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name%>
							<g:if test="${engCommonName}">
								<b class="commonName"> ${engCommonName} </b>
							</g:if>
							<div class="icons">
								<g:collect in="${speciesInstance}" expr="${it.fields.resources}"
									var="resourcesCollection">
									<g:each in="${resourcesCollection}" var="rs">
										<g:each in="${rs}" var="r">
											<g:if test="${r.type == species.Resource.ResourceType.ICON}">
												<a href="${href}">
												<img class="small_profile_pic"
													src="${createLinkTo(dir: 'images/icons', file: r.fileName.trim(), absolute:true)}"
													title="${r?.description}" />
												</a>
											</g:if>
										</g:each>
									</g:each>
								</g:collect>


								<g:each in="${speciesInstance.fetchTaxonomyRegistry()}">
									<span class="dropdown"> <a href="#"
										class="dropdown-toggle small_profile_pic taxaHierarchy ellipsis"
										data-toggle="dropdown" title="${it.key.name}"></a> <%def sortedTaxon = it.value.sort {it.rank} %>
										<span class="dropdown-menu toolbarIconContent"> <g:each
												in="${sortedTaxon}" var="taxonDefinition">
												<span class='rank${taxonDefinition.rank} '> ${taxonDefinition.italicisedForm}
												</span>
												<g:if test="${taxonDefinition.rank<8}">></g:if>
											</g:each> </span> </span>

								</g:each>


							</div>

							<div class="ellipsis multiline">
								<g:set var="summary" value="${speciesInstance.findSummary()}"></g:set>
								<g:if test="${summary != null && summary.length() > 300}">
									${summary[0..300] + ' ...'}
								</g:if>
								<g:else>
									${summary?:''}
								</g:else>
							</div>
						</div>
					</li>
				</g:each>
				<li>
					<div class="paginateButtons" style="clear: both">
						<center>
							<p:paginate total="${instanceTotal?:0}" action="${params.action}"
								controller="${params.controller?:'species'}"
								userGroup="${userGroup}"
								userGroupWebaddress="${userGroupWebaddress}"
								max="${queryParams.max}" params="${activeFilters}" />
						</center>
					</div>
				</li>

			</g:if>
		</ul>
	</div>
</div>
</div>