<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance.id
%>
<div class="snippet tablet">
        <g:render template="/common/observation/noOfResources" model="['instance':observationInstance]"/>
	<div class="figure" style="height:150px;"
		title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
                <g:link url="${uGroup.createLink(controller:controller, action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="g${pos}">
			<g:if
				test="${imagePath}">
				<img class="img-polaroid"
					src="${imagePath}" />
			</g:if>
			<g:else>
				<img class="img-polaroid"
					src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
					title="You can contribute!!!" />
			</g:else>
		</g:link>
	</div>
	<div class="caption" >
		<obv:showStoryTablet
			model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStoryTablet>
	</div>
</div>
