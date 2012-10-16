<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>
<div class="snippet tablet">

	<div class="figure" style="height:150px;"
		title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
		<g:if test="${userGroup}">
			<g:set var="url" value="${createLink(mapping:'userGroupModule', controller:'observation', action:'show', id:observationInstance.id, params:['pos':pos, 'webaddress':userGroup.webaddress]) }"/>
		</g:if>
		<g:elseif test="${userGroupWebaddress }">
			<g:set var="url" value="${createLink(mapping:'userGroupModule', controller:'observation', action:'show', id:observationInstance.id, params:['webaddress':userGroupWebaddress]) }"/>
		</g:elseif>
		<g:else>
			<g:set var="url" value="${createLink( controller:'observation', action:'show', id:observationInstance.id, params:['pos':pos]) }"/>
		</g:else>
		<g:link url="${url}" name="g${pos}">
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
	<div class="all_content">
		<obv:showStoryTablet
			model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStoryTablet>
	</div>
</div>
