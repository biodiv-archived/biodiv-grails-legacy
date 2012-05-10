
<div class="snippet span8">
	<div class="row">
		<g:set var="mainImage" value="${observationInstance.mainImage()}" />
		<div class="figure span3" title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'
			style="float: left;max-height: 220px; max-width: 200px">

			<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>

			<div class="wrimg">
				<div class="thumbnail">
					<g:if
						test="${imagePath && (new File(grailsApplication.config.speciesPortal.observations.rootDir + imagePath)).exists()}">



						<g:link action="show" controller="observation"
							id="${observationInstance.id}">
							<img
								src="${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: imagePath)}" />
						</g:link>

					</g:if>
					<g:else>
						<g:link action="show" controller="observation"
							id="${observationInstance.id}">
							<img class="galleryImage"
								src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
								title="You can contribute!!!" />
						</g:link>
					</g:else>
				</div>
			</div>
		</div>
		<div class="span5 observation_story_wrapper">
			<obv:showStory model="['observationInstance':observationInstance]"></obv:showStory>
		</div>
	</div>
</div>
