
<div class="snippet tablet">
	<g:set var="mainImage" value="${observationInstance.mainImage()}" />
	<div class="figure"
		style="float: left; max-height: 220px; max-width: 200px">

		<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>
		<g:link action="show" controller="observation"
			id="${observationInstance.id}">

			<g:if
				test="${imagePath && (new File(grailsApplication.config.speciesPortal.observations.rootDir + imagePath)).exists()}">

				<span class="wrimg"> <span></span> 
                                <div class="thumbnail">
                                    <img
                                        src="${createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: imagePath)}" />
                                </div>                                                        
				</span>

			</g:if>
			<g:else>
                                <div class="thumbnail">
                                    <img class="galleryImage"
                                            src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                                            title="You can contribute!!!" />
                                </div>                                                        
			</g:else>
		</g:link>
	</div>
	<div class="all_content" style="width:200px; padding:0; margin:0;">
		<obv:showStoryTablet model="['observationInstance':observationInstance]"></obv:showStoryTablet>
	</div>
</div>
