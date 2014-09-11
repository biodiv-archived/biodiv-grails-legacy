<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, !observationInstance.resource ? '.png' :null): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance.id
%>
<g:if test="${observationInstance}">
    <g:set var="featureCount" value="${observationInstance.featureCount}"/>
</g:if>

<div class="snippet">
    <span class="badge ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
            </span>
    <div class="figure pull-left observation_story_image" 
            title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
            <g:link url="${uGroup.createLink(controller:controller, action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}"
            >
                    <div style="position:relative;margin:auto;">
                    <g:if
                            test="${imagePath}">
                            <img class="img-polaroid" style=" ${observationInstance.isChecklist? 'opacity:0.7;' :''}"
                                    src="${imagePath}"
                                    />
                    </g:if>
                    <g:else>
                            <img class="galleryImage img-polaroid"
                                    src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                                    title="${g.message(code:'showobservationsnippet.title.contribute')}" />
                    </g:else>
                        <g:if test="${observationInstance.isChecklist}">
                        <div class="checklistCount">${observationInstance.speciesCount}</div>
                        </g:if>
                    </div>

            </g:link>

    </div>
    <obv:showStory model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'featuredNotes':featuredNotes, featuredOn:featuredOn, showDetails:showDetails, showFeatured:showFeatured]"></obv:showStory>
</div>
