
<div class="snippet tablet">
	<g:set var="mainImage" value="${observationInstance.mainImage()}" />
	<div class="figure" title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>

		<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>

		<div class="wrimg">
			<div class="thumbnail">
				<g:link action="show" controller="observation"
						id="${observationInstance.id}" params="['pos':pos]">
					<g:if
						test="${imagePath && (new File(grailsApplication.config.speciesPortal.observations.rootDir + imagePath)).exists()}">
						<img
							src="${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: imagePath)}" />
					</g:if>
					<g:else>
						<img
							src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
							title="You can contribute!!!" />
					</g:else>
				</g:link>
			</div>
		</div>
	</div>
	<div class="all_content">
		<obv:showStoryTablet
			model="['observationInstance':observationInstance]"></obv:showStoryTablet>
	</div>
</div>
