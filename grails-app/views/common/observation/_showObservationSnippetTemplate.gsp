<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>

<div class="figure span3 observation_story_image" style="display: table;height:220px;" 
	title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
	<g:link url="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}"
		>
		<g:if
			test="${imagePath && (new File(grailsApplication.config.speciesPortal.observations.rootDir + imagePath)).exists()}">
			<img class="img-polaroid"
				src="${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: imagePath)}"
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