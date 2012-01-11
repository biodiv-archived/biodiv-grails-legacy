
	<g:each in="${speciesInstance.getImages()}" var="r">
		<%def gallImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)%>
		<%def gallThumbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>
		<a target="_blank"
			rel="${createLinkTo(file: r.fileName.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}"
			href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}">
			<img class="galleryImage"
			src="${createLinkTo(file: gallThumbImagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
			title="${r?.description}" /> </a>

		<g:imageAttribution model="['resource':r]" />
	</g:each>
