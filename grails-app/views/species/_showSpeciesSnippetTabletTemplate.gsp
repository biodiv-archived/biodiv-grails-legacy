<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.Resource"%>
<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
<%
    def basePath = '';
    if(mainImage?.context?.value() == Resource.ResourceContext.OBSERVATION.toString()){
        basePath = grailsApplication.config.speciesPortal.observations.serverURL
    }
    else if(mainImage?.context?.value() == Resource.ResourceContext.SPECIES.toString() || mainImage?.context?.value() == Resource.ResourceContext.SPECIES_FIELD.toString()){
        basePath = grailsApplication.config.speciesPortal.resources.serverURL
    }

    def imagePath = '';
    def speciesGroupIcon =  speciesInstance.fetchSpeciesGroup().icon(ImageType.ORIGINAL)
    if(mainImage?.fileName == speciesGroupIcon.fileName) 
        imagePath = mainImage.thumbnailUrl(basePath, '.png');
    else
        imagePath = mainImage?mainImage.thumbnailUrl(basePath):null;
    def obvId = speciesInstance.id
%>

<g:if test="${speciesInstance}">
    <g:set var="featureCount" value="${speciesInstance.featureCount}"/>
</g:if>
<div class="snippet tablet ">
    <span class="badge ${speciesInstance.fetchSpeciesGroup().iconClass()} ${(featureCount>0) ? 'featured':''}" > </span>
    <div class="figure"
        title='<g:if test="${obvTitle != null}">${obvTitle.replaceAll("<.*>","")}</g:if>'>
                <g:link url="${uGroup.createLink(controller:'species', action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup) }" name="g${pos}">
                
                <g:if test="${imagePath}">
                <img class="img-polaroid" src="${imagePath}" />
                </g:if>
                <g:else>
                <img class="img-polaroid"
                src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                title="You can contribute!!!" />
                </g:else>
		</g:link>
	</div>
	<div class="caption" >
            <g:render template="/species/showSpeciesStoryTabletTemplate" model="['speciesInstance':speciesInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"/>
            <uGroup:objectPost model="['objectInstance':speciesInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
	</div>
</div>
