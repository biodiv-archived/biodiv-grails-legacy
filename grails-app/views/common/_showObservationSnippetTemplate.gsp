
<div class="snippet grid_16">
	<g:set var="mainImage" value="${observationInstance.mainImage()}" />
	<div class="figure"
		style="float: left; max-height: 220px; max-width: 200px">
		<%def imagePath = mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
		<g:if test="${(new File(grailsApplication.config.speciesPortal.observations.rootDir + imagePath)).exists()}">
			

			<g:link action="show" controller="observation"
				id="${observationInstance.id}">

				<span class="wrimg"> <span></span> <img
					src="${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,
											file: imagePath)}" />
				</span>
			</g:link>

		</g:if>
		<g:else>
			<img class="galleryImage"
				src="${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
				title="You can contribute!!!" />
		</g:else>
	</div>
	<obv:showStory model="['observationInstance':observationInstance]"></obv:showStory>
</div>