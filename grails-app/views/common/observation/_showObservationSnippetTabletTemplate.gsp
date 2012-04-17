
<div class="snippet tablet">
	<g:set var="mainImage" value="${observationInstance.mainImage()}" />
	<div class="figure">

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
						<img
							src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
							title="You can contribute!!!" />
					</g:link>
				</g:else>
			</div>
		</div>
	</div>
	<div class="all_content">
		<obv:showStoryTablet
			model="['observationInstance':observationInstance]"></obv:showStoryTablet>
	</div>
</div>
