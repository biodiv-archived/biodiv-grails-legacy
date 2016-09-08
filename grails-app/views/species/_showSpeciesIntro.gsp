<%@page import="species.utils.ImageType"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@page import="species.utils.Utils"%>

<div class="span4 pull-right">
    <g:render template="/species/showTaxonBrowserTemplate" model="['speciesInstance':speciesInstance, 'expandSpecies':true, 'expandAll':false, 'speciesId':speciesInstance.taxonConcept?.id, expandAllIcon:false, isSpeciesContributor:isSpeciesContributor, fieldFromName:fieldFromName]"/>
    <g:render template="/species/inviteForContribution" model="['hide':true]"/>
</div>

<!-- media gallery -->
<div class="span8 right-shadow-box" style="margin:0px;">
    <g:if test="${speciesInstance.taxonConcept.rank >= TaxonomyRank.SPECIES.ordinal()}">
    <div style="padding-bottom:10px">
                <center>
            <div id="gallerySpinner" class="spinner">
                <img src="${assetPath(src:'/all/spinner.gif', absolute:true)}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
            </div>
        </center>


        <div id="resourceTabs" style="visibility:hidden;">
            <g:if test="${isSpeciesContributor}">
            <a id="addSpeciesImagesBtn" class="btn btn-success"
                        style="float: right; margin-right: 5px; margin-top: 5px;"> <g:message code="button.add.images" /> </a>

            </g:if>
            <ul>
                <li><a href="#resourceTabs-1"><g:message code="button.images" /></a></li>
                <!-- <li><a id="flickrImages" href="#resourceTabs-3"><g:message code="button.flickr.images" /></a></li> -->
            </ul>
            <div id="resourceTabs-1">
                <!--a class="myeditable" href="#">Contribute Images</a-->
                    <div class="galleryWrapper">
                        <g:render template="/observation/galleryTemplate" model="['instance': speciesInstance]"/>
                    </div>
                </div>
                <!-- <div id="resourceTabs-3">						
                    <div id="gallery3" style="margin-top: 60px;"></div>
                    <div id="flickrBranding"></div><br/>
                    <div class="message ui-corner-all"><g:message code="showspeciesintro.irrelevant.images" /></div>
                </div> -->
            </div>
        </div>
        </g:if>
        <g:else>
            <div class="sidebar_section" style="border:0px;">
                <h5> <g:message code="button.childTaxa" /> <a href="${uGroup.createLink(controller:'species', action:'list', params:['taxon':speciesInstance.taxonConcept.id])}" class="pull-right"> Show All </a></h5>
                <s:showSpeciesList model="['instanceTotal':0]"/>
            </div>

        </g:else>
     
        <div style="background-color:white">

            <!-- species page icons -->
            <div style="padding:5px 0px;position:relative;">
                <s:showSpeciesExternalLink model="['speciesInstance':speciesInstance]"/>
                    <div class="observation-icons">		
                        <img class="group_icon species_group_icon"  
                        title="${speciesInstance.fetchSpeciesGroup()?.name}"
                        src='${assetPath(src: '/all/'+ speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}'/>

                        <g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
                        <s:showThreatenedStatus model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]"/>
                            </g:if>
                        </div>
                    </div>
                    <div style="margin:5px 0px;">
                        <g:each in="${speciesInstance.getIcons()}" var="r">
                        <%def imagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
                        <img class="icon group_icon" href="${href}"
                        src="${createLinkTo(file: imagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                        title="${r?.description}" />
                        </g:each>
                    </div>
                    
                </div>
            </div>


