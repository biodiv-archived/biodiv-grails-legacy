<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, observationInstance.isChecklist ? '.png' :null): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance.id
%>

<div style="position:relative;overflow:hidden">
    <div class="figure span2 observation_story_image" style="display: table;height:220px;" 
            title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
            <g:link url="${uGroup.createLink(controller:controller, action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}"
                    >
                    <div style="${observationInstance.isChecklist?'height:100px;width:100px;':''}position:relative;margin:auto;">
                    <g:if
                            test="${imagePath}">
                            <img class="img-polaroid" style=" ${observationInstance.isChecklist? 'opacity:0.7;' :''}"
                                    src="${imagePath}"
                                    />
                    </g:if>
                    <g:else>
                            <img class="galleryImage img-polaroid"
                                    src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                                    title="You can contribute!!!" />
                    </g:else>
                        <g:if test="${observationInstance.isChecklist}">
                        <div class="checklistCount">${observationInstance.speciesCount}</div>
                        </g:if>
                    </div>

            </g:link>

    </div>
</div>
<obv:showStory model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStory>
