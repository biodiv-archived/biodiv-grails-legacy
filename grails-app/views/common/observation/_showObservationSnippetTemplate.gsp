<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance.isChecklist ? observationInstance.sourceId: observationInstance.id
%>

<div style="position:relative;overflow:hidden">
    <g:render template="/common/observation/noOfResources" model="['instance':observationInstance]"/>
    <div class="figure span3 observation_story_image" style="display: table;height:220px;" 
            title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
            <g:link url="${uGroup.createLink(controller:controller, action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}"
                    >
                    <g:if
                            test="${imagePath}">
                            <img class="img-polaroid"
                                    src="${imagePath}"
                                    />
                    </g:if>
                    <g:else>
                            <img class="galleryImage img-polaroid"
                                    src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                                    title="You can contribute!!!" />
                    </g:else>
            </g:link>

    </div>
</div>
<obv:showStory model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStory>
