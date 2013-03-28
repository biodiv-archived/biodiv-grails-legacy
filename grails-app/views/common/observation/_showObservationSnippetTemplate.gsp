<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%def path = mainImage?mainImage.thumbnailUrl(): null;
def imagePath;
if(mainImage.type == ResourceType.IMAGE) {
	imagePath = g.createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: path)
} else if(mainImage.type == ResourceType.VIDEO){
	imagePath = g.createLinkTo(base:path,	file: '')
}
%>

<div class="figure span3 observation_story_image" style="display: table;height:220px;" 
	title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
	<g:link url="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}"
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
<obv:showStory model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStory>