<%@page import="species.utils.ImageType"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@page import="species.utils.Utils"%>

${resourceInstanceList}
<div class="span4 pull-right">
    <g:render template="/species/showTaxonBrowserTemplate" model="['speciesInstance':speciesInstance, 'expandSpecies':true, 'expandAll':false, 'speciesId':speciesInstance.taxonConcept?.id, expandAllIcon:false, isSpeciesContributor:isSpeciesContributor]"/>
    <g:render template="/species/inviteForContribution" model="['hide':true]"/>
</div>

<!-- media gallery -->
<div class="span8 right-shadow-box" style="margin:0px;">

    <div style="padding-bottom:10px;height: 520px">
                <center>
            <div id="gallerySpinner" class="spinner">
                <img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
                alt="${message(code:'spinner.alt',default:'Loading...')}" />
            </div>
        </center>


        <div id="resourceTabs" style="visibility:hidden;">
            <g:if test="${isSpeciesContributor}">
            <a id="addSpeciesImagesBtn" class="btn btn-success"
                        style="float: right; margin-right: 5px; margin-top: 5px;"> Add Images </a>

            </g:if>
            <ul>
                <li><a href="#resourceTabs-1">Images</a></li>
                <li><a id="flickrImages" href="#resourceTabs-3">Flickr Images</a></li>
            </ul>
            <div id="resourceTabs-1">
                <!--a class="myeditable" href="#">Contribute Images</a-->
                <g:set var="resourcesInstanceList" value="${speciesInstance.listResourcesByRating()}"/>
                <div class="story-footer" style="right:0;bottom:372px;z-index:5;background-color:whitesmoke" >
                    <g:render template="/common/observation/noOfResources" model="['instance':speciesInstance, 'bottom':'bottom:55px;']"/>
                </div>
                
                <div id="gallery1" class="gallery" style="margin-top: 60px;">

                    <g:if test="${resourcesInstanceList}">
                    
                    <s:showSpeciesImages model="['speciesInstance':speciesInstance , 'resourcesInstanceList' : resourcesInstanceList]"></s:showSpeciesImages>
                    </g:if>
                    <g:else>
                    <% def fileName = speciesInstance.fetchSpeciesGroup().icon(ImageType.LARGE).fileName%>
                    <img class="group_icon galleryImage" 
                             src="${createLinkTo(dir: 'images', file: fileName, absolute:true)}" 
                             title="Contribute!!!"/>
                    </g:else>

                </div>
                </div>
                <div id="resourceTabs-3">						
                    <div id="gallery3"></div>
                    <div id="flickrBranding"></div><br/>
                    <div class="message ui-corner-all">These images are fetched from other sites and may contain some irrevelant images. Please use them at your own discretion.</div>
                </div> 
            </div>
        </div>


        <g:render template="/species/speciesaudio" model="['speciesInstance': speciesInstance, 'isSpeciesContributor':isSpeciesContributor , 'resourcesInstanceList' : resourcesInstanceList]"/>

        <div style="background-color:white">

            <!-- species page icons -->
            <div style="padding:5px 0px;position:relative;">
                <s:showSpeciesExternalLink model="['speciesInstance':speciesInstance]"/>
                    <div class="observation-icons">		
                        <img class="group_icon species_group_icon"  
                        title="${speciesInstance.fetchSpeciesGroup()?.name}"
                        src='${createLinkTo(dir: 'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}'/>

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
                    <div class="readmore sidebar_section notes_view">
                        ${raw(speciesInstance.notes())}
                    </div>
                </div>
            </div>


