<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>

<div class="figure observation_story_image pull-left"
	style="display: table;"
	title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
	<g:link
		url="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }"
		name="l${pos}">
		<g:if
			test="${imagePath && (new File(grailsApplication.config.speciesPortal.observations.rootDir + imagePath)).exists()}">
			<img class="media-object"
				src="${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: imagePath)}" />
		</g:if>
		<g:else>
			<img class=" media-object galleryImage"
				src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				title="You can contribute!!!" />
		</g:else>
	</g:link>

</div>


<div class="observation_story_wrapper media-body">
<div class="media">
	<obv:showStory
		model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStory>
	
	<div style="position: absolute; right: 5px; bottom: -3px;">
		<sUser:showUserTemplate
			model="['userInstance':observationInstance.author, 'userGroup':userGroup]" />
	</div>
	</div>
</div>
