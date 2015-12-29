<%@page import="species.Resource.ResourceType"%>
<%@page import="species.participation.Observation"%>
<%
def imagePath = null; 
%>

<div class="snippet">
    <span class="badge ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}"></span>
    <div class="figure pull-left observation_story_image" 
            title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
            <g:link url="${uGroup.createLink(controller:'observation', action:'list', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'dataset':datasetInstance.id, isMediaFilter:false) }" name="l${pos}">
                    <div style="position:relative;margin:auto;">
                    <g:if
                            test="${imagePath}">
                            <img class="img-polaroid" style="opacity:0.7"
                                    src="${imagePath}"
                                    />
                    </g:if>
                    <g:else>
                            <img class="galleryImage img-polaroid"
                                    src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                                    title="${g.message(code:'showobservationsnippet.title.contribute')}" />
                    </g:else>
                        <div class="checklistCount">${Observation.countByDataset(datasetInstance)}</div>
                    </div>

            </g:link>

    </div>
    <g:render template="/dataset/showDatasetStoryTemplate" model="['datasetInstance':datasetInstance, showDetails:true,'userLanguage':userLanguage]"/>
</div>
