<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.Species"%>
<%@ page import="species.Language"%>
<%@ page import="species.CommonNames"%>

<g:if test="${speciesInstance}">
<g:set var="featureCount" value="${speciesInstance.featureCount}"/>
</g:if>

<span class="badge ${(featureCount>0) ? 'featured':''}">
</span>

	<div class="figure span3 observation_story_image"
		style="display: table; height: 220px;" title="">
		<a
			href="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}">
			<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
			<%def thumbnailPath = ImageUtils.getFileName(mainImage?.fileName, ImageType.NORMAL, null)%>

			<g:if test="${thumbnailPath}">
				<img
					src="${createLinkTo( base:grailsApplication.config.speciesPortal.resources.serverURL,
						file: thumbnailPath)}"
					title="${speciesInstance.taxonConcept.name }" />
			</g:if> <g:else>
                                <img class="img-polaroid" style="opacity:0.7;"
                                        title="${speciesInstance.taxonConcept.name.replaceAll('<.*>','')}"
                                        src="${createLinkTo(dir: '', file:speciesInstance.fetchSpeciesGroupIcon(ImageType.NORMAL)?.fileName, absolute:true)}"></img>
			</g:else> </a>
	</div>
	<div class="observation_story"
		style="overflow: visible; width: 100%">
            <div>
		<div class="observation-icons">								
			<span
				class="group_icon species_groups_sprites active pull-right ${speciesInstance.fetchSpeciesGroup()?.iconClass()}"
				title="${speciesInstance.fetchSpeciesGroup()?.name}"></span>
			<g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
				<s:showThreatenedStatus
					model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]" />
			</g:if>
                  </div>
              </div>
              <div class="span7">
                    <h6>
			<g:link
                        url="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}" title="${speciesInstance.taxonConcept.name}">
                            <span class="ellipsis" style="display:block;width:100%;float:none">${speciesInstance.taxonConcept.italicisedForm }</span>
                            </g:link>
                    </h6>
                    <%def engCommonName=CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name%>
                    <g:if test="${engCommonName}">
                            <div><b class="commonName"> ${engCommonName} </b></div>
                    </g:if>
                    <div class="icons clearfix">
                            
                            <g:each in="${speciesInstance.fetchTaxonomyRegistry()}">
                                    <div class="dropdown" style="display:inline-block;"> <a href="#"
                                            class="dropdown-toggle small_profile_pic taxaHierarchy pull-left"
                                            data-toggle="dropdown" title="${it.key.name}"></a> <%def sortedTaxon = it.value.sort {it.rank} %>
                                            <span class="dropdown-menu toolbarIconContent"> <g:each
                                                            in="${sortedTaxon}" var="taxonDefinition">
                                                            <span class='rank${taxonDefinition.rank} '> ${taxonDefinition.italicisedForm}
                                                            </span>
                                                            <g:if test="${taxonDefinition.rank<8}">></g:if>
                                                    </g:each> </span> </div>

                            </g:each>
                            
                            <div>
                                    <g:each in="${speciesInstance.getIcons()}" var="r">
                                            <%def imagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
                                            <img class="icon group_icon" href="${href}"
                                                    src="${createLinkTo(file: imagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                                                    title="${r?.description}" />
                                    </g:each>
                            </div>



                    </div>
                    
                    <g:set var="summary" value="${speciesInstance.notes()}"></g:set>

                    <g:if test="${summary}">
                        <div class="ellipsis multiline clearfix notes_view">
                                <g:if test="${summary != null && summary.length() > 300}">
                                        ${summary[0..300] + ' ...'}
                                </g:if>
                                <g:else>
                                        ${summary?:''}
                                </g:else>
                        </div>
                    </g:if>
                </div>
                <div class="story-footer">
                    <div class="footer-item">
                        <obv:like model="['resource':speciesInstance]"/>
                    </div>
                    <uGroup:objectPost model="['objectInstance':speciesInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
                </div>


	</div>

