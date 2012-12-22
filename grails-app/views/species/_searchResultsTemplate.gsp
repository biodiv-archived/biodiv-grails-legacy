<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.Species"%>
<%@ page import="species.Language"%>
<%@ page import="species.CommonNames"%>

<div class="observations_list thumbwrap" style="top:0px;">

		<g:if test="${instanceTotal > 0}">
			<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">

				<div class="media"
					style="margin: 5px; padding: 10px; height: 180px; border-bottom: 1px solid #c6c6c6; background-color: #fdfdfd;">
					<a class="pull-left figure"
						style="max-height: 220px; max-width: 200px;"
						href="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}">
						<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
						<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.NORMAL, null)%>

						<g:if test="${thumbnailPath }">
							<img class="media-object"
								src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
											file: thumbnailPath)}"
								title="${speciesInstance.taxonConcept.name }" />
						</g:if> <g:else>
							<img class="group_icon media-object" style="opacity: 0.4;"
								title="${speciesInstance.taxonConcept.name}"
								src="${createLinkTo(dir:'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.NORMAL)?.fileName, absolute:true)}" />
						</g:else> </a>
					<div class="media-body" style="">
						<h6 class="media-heading">
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
											<img class="icon" href="${href}"
												src="${createLinkTo(dir: 'images/icons', file: r.fileName.trim(), absolute:true)}"
												title="${r?.description}" />
										</g:if>
									</g:each>
								</g:each>
							</g:collect>


							<g:each in="${speciesInstance.fetchTaxonomyRegistry()}">
								<div class="dropdown icon">
									<a href="#" class="dropdown-toggle icon taxaHierarchy ellipsis"
										data-toggle="dropdown" title="${it.key.name}"></a>
									<%def sortedTaxon = it.value.sort {it.rank} %>
									<div class="dropdown-menu toolbarIconContent">
										<g:each in="${sortedTaxon}" var="taxonDefinition">
											<span class='rank${taxonDefinition.rank} '> ${taxonDefinition.italicisedForm}
											</span>
											<g:if test="${taxonDefinition.rank<8}">></g:if>
										</g:each>
									</div>
								</div>

							</g:each>


							<img class="group_icon species_group_icon"
								title="${speciesInstance.fetchSpeciesGroup()?.name}"
								src="${createLinkTo(dir: 'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL).fileName, absolute:true)}" />

							<g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
								<s:showThreatenedStatus
									model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]" />
							</g:if>
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
				</div>
			</g:each>
			<div class="paginateButtons" style="clear: both;">
				<center>
					<p:paginateOnSearchResult total="${instanceTotal}" controller="species" action="search"
						params="[query:queryParams.q, fl:queryParams.fl]" />
				</center>
			</div>
		</g:if>
</div>