<%@page import="species.Resource"%>
<g:each in="${speciesInstance.getImages()}" var="r">
<%  def basePath 
    if(r.context.value() == Resource.ResourceContext.OBSERVATION.toString()){
        basePath = grailsApplication.config.speciesPortal.observations.serverURL
    }
    else if(r.context.value() == Resource.ResourceContext.SPECIES.toString() || r.context.value() == Resource.ResourceContext.SPECIES_FIELD.toString()){
        basePath = grailsApplication.config.speciesPortal.resources.serverURL
    }
%>
<%def gallImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)%>
<%def gallThumbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>
<a target="_blank"
    rel="${createLinkTo(file: r.fileName.trim(), base:basePath)}"
    href="${createLinkTo(file: gallImagePath, base:basePath)}">
    <img class="galleryImage"
    src="${createLinkTo(file: gallThumbImagePath, base:basePath)}"

    data-original="${createLinkTo(file: r.fileName.trim(), base:basePath)}" 
    /> </a>

<g:imageAttribution model="['resource':r, base:basePath]" />
</g:each>
