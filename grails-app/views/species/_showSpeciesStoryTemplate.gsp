<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@ page import="species.Species"%>
<%@ page import="species.Language"%>
<%@ page import="species.CommonNames"%>
<div class="observation_story">
    <div class="observation-icons">								
        <span
            class="group_icon species_groups_sprites active pull-right ${speciesInstance.fetchSpeciesGroup()?.iconClass()}"
            title="${speciesInstance.fetchSpeciesGroup()?.name}"></span>
        <g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
            <s:showThreatenedStatus
                model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]" />
        </g:if>
    </div>
    <div>
        <g:if test="${showFeatured}">
        <div class="featured_body">
        </g:if>
        <div class="featured_title ellipsis">
            <div class="heading"> 
                <g:link
                url="${uGroup.createLink(action:'show', controller:'species', id:speciesInstance.id)}" title="${speciesInstance.taxonConcept.name}">
                <span class="ellipsis">${speciesInstance.taxonConcept.italicisedForm }</span>
                </g:link>

                <g:if test="${showFeatured}">
                <small> featured on <time class="timeago" datetime="${featuredOn.getTime()}"></time></small>
                </g:if>
            </div>
        </div>
        <g:if test="${showFeatured}">
            <div class="featured_notes linktext">
                ${featuredNotes}
                <p>${speciesInstance.summary()}</p>
            </div>		
        </div>
        </g:if>
        <g:else>
            <%def engCommonName=CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name%>
            <g:if test="${engCommonName}">
                <div>
                    <b class="commonName"> ${engCommonName} </b>
                </div>
            </g:if>
            <div class="icons clearfix">

                <g:each in="${speciesInstance.fetchTaxonomyRegistry()}">
                    <div class="dropdown" style="display:inline-block;"> 
                        <a href="#"
                            class="dropdown-toggle small_profile_pic taxaHierarchy pull-left"
                            data-toggle="dropdown" title="${it.key.name}"></a> <%def sortedTaxon = it.value.sort {it.rank} %>
                        <span class="dropdown-menu toolbarIconContent"> 
                            <g:each
                            in="${sortedTaxon}" var="taxonDefinition">
                            <span class='rank${taxonDefinition.rank} '> ${taxonDefinition.italicisedForm}</span>

                            <g:if test="${taxonDefinition.rank<8}">></g:if>
                            </g:each> 
                        </span> 
                    </div>
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
            <div class="story-footer">
                    <div class="footer-item">
                        <obv:like model="['resource':speciesInstance]"/>
                    </div>
<%--                    <uGroup:objectPost model="['objectInstance':speciesInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />--%>
            </div>


            </g:else>
        </div>
</div>
