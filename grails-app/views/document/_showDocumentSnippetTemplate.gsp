<%@ page import="content.eml.Document"%>
<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${documentInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, null): null;
def obvId = documentInstance.id
%>
<g:if test="${documentInstance}">
<g:set var="featureCount" value="${documentInstance.featureCount}"/>
</g:if>

<div class="snippet">
    <span class="badge ${documentInstance.group?.iconClass()} ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? 'Featured':''}">
    </span>
    <div class="figure pull-left observation_story_image" 
        title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
        <g:link url="${uGroup.createLink(controller:'document', action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}"
        >
        <div style="position:relative;margin:auto;">
            <g:if
            test="${imagePath}">
            <img class="img-polaroid" src="${imagePath}"/>
            </g:if>
            <g:else>
            <img class="galleryImage img-polaroid"
            src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
            title="You can contribute!!!" />
            </g:else>
        </div>

        </g:link>

    </div>

    <g:render template="/document/showDocumentStoryTemplate" model="['documentInstance':documentInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'featuredNotes':featuredNotes, featuredOn:featuredOn, showDetails:showDetails, showFeatured:showFeatured]"></g:render>
</div>
