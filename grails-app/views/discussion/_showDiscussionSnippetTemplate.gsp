<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${discussionInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, null): null;
def obvId = discussionInstance.id
%>
<g:if test="${discussionInstance}">
<g:set var="featureCount" value="${discussionInstance.featureCount}"/>
</g:if>

<div class="snippet" style="width:100%;margin-bottom: 2px;">
    <span class="badge ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
    </span>
<%--    <g:if test="${params.showFeatured}">--%>
    <div class="figure pull-left observation_story_image"  
        title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
        <g:link url="${uGroup.createLink(controller:'discussion', action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
        <div style="position:relative;margin:auto;">
            <g:if
            test="${imagePath}">
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
<%--        </g:if>--%>
        <g:render template="/discussion/showDiscussionStoryTemplate" model="['discussionInstance':discussionInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'featuredNotes':featuredNotes, featuredOn:featuredOn, showDetails:false, showFeatured:showFeatured]"></g:render>
</div>
