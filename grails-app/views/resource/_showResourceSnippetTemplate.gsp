<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${resourceInstance}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null): null;
%>

<div class="snippet">
    <span class="badge ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
    </span>
    <div class="figure pull-left observation_story_image" 
        title=''>

        <g:link url="${resourceInstance.url}" target="_blank">
        <div style="position:relative;margin:auto;">
            <g:if test="${imagePath}">
            <img class="img-polaroid" src="${imagePath}"/>
            </g:if>
            <g:else>
            <img class="galleryImage img-polaroid"
            src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
            title="${g.message(code:'showobservationsnippet.title.contribute')}" />
            </g:else>
        </div>
        </g:link>
    </div>
    <g:render template="/resource/showResourceStoryTemplate" model="['resourceInstance':resourceInstance, container:container, showDetails:true, showFeatured:showFeatured, 'userLanguage' : resourceInstance.language]"/>
</div>
