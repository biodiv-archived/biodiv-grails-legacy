<g:set var="mainImage" value="${userGroupInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>
<div class="snippet tablet">

	<div class="figure" style="height:150px;"
		title='<g:if test="${userGroupTitle != null}">${userGroupTitle}</g:if>'>
		<g:link link="${uGroup.createLink(controller:'SUser', action:'show', 
			id:userGroupInstance.id, params:['pos':pos])}">
			<img class="logo" alt="${userGroupInstance.name}"
					src="${createLink(url: userGroupInstance.mainImage()?.fileName)}">
		</g:link>
	</div>
	<div class="all_content">
		<uGroup:showStoryTablet
			model="['userGroupInstance':userGroupInstance]"></uGroup:showStoryTablet>
	</div>
</div>
