<g:set var="mainImage" value="${userGroupInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>
<div class="snippet tablet">

	<div class="figure" style="height:150px;"
		title='<g:if test="${userGroupTitle != null}">${userGroupTitle}</g:if>'>
		<g:link action="show" 
			id="${userGroupInstance.id}" params="['pos':pos]">
			<g:if
				test="${imagePath && (new File(grailsApplication.config.speciesPortal.userGroups.rootDir + imagePath)).exists()}">
				<img
					src="${createLinkTo(base:grailsApplication.config.speciesPortal.userGroups.serverURL,	file: imagePath)}" />
			</g:if>
			<g:else>
				<img
					src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"/>
			</g:else>
		</g:link>
	</div>
	<div class="all_content">
		<uGroup:showStoryTablet
			model="['userGroupInstance':userGroupInstance]"></uGroup:showStoryTablet>
	</div>
</div>
