<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${speciesInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(grailsApplication.config.speciesPortal.resources.serverURL, !speciesInstance.resources ? '.png' :null): null;
def obvId = speciesInstance.id
%>

<g:if test="${speciesInstance}">
    <g:set var="featureCount" value="${speciesInstance.featureCount}"/>
</g:if>
<div class="snippet">
    <span class="badge ${speciesInstance.fetchSpeciesGroup().iconClass()} ${(featureCount>0) ? 'featured':''}" >
                </span>

    <div class="figure pull-left observation_story_image"
        title='<g:if test="${obvTitle != null}">${obvTitle.replaceAll("<.*>","")}</g:if>'>
                <g:link url="${uGroup.createLink(controller:'species', action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="g${pos}">
                
                <g:if
				test="${imagePath}">
				<img class="img-polaroid" src="${imagePath}" />
			</g:if>
			<g:else>
				<img class="img-polaroid"
					src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
					title="You can contribute!!!" />
			</g:else>
		</g:link>
	</div>
            <g:render template="/species/showSpeciesStoryTemplate" model="['speciesInstance':speciesInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, featuredNotes:featuredNotes, featuredOn:featuredOn, showFeatured:showFeatured, showDetails:showDetails ]"/>
            <uGroup:objectPost model="['objectInstance':speciesInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
</div>
