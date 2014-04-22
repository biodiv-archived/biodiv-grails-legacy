<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance?.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, !observationInstance.resource ? '.png' :null): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance?.id
%>

<g:if test="${observationInstance}">
    <g:set var="featureCount" value="${observationInstance.featureCount}"/>
</g:if>
<div class="snippet tablet">
    <span class="badge ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? 'Featured':''}">
                </span>

    <div class="figure"
        title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
                <g:link url="${uGroup.createLink(controller:controller, action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="g${pos}">
                
                <g:if
				test="${imagePath}">
				<img class="img-polaroid" style=" ${observationInstance.isChecklist? 'opacity:0.7;' :''}"
					src="${imagePath}" />
			</g:if>
			<g:else>
				<img class="img-polaroid"
					src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
					title="You can contribute!!!" />
			</g:else>
                        <g:if test="${observationInstance?.isChecklist}">
                        <div class="checklistCount">${observationInstance?.speciesCount}</div>
                        </g:if>
		</g:link>
                <!--div class="mouseover" style="padding-left:0px;">
                </div-->

	</div>
	<div class="caption" >
		<obv:showStoryTablet
			model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStoryTablet>
		<uGroup:objectPost model="['objectInstance':observationInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
	</div>
</div>
