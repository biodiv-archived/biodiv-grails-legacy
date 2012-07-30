<g:set var="mainImage" value="${userGroupInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>

<div class="snippet span8">
	<div class="row">
		<div class="figure span3 observation_story_image" style="display: table;" 
			title='<g:if test="${userGroupTitle != null}">${userGroupTitle}</g:if>'>

			<g:link action="show" 
				id="${userGroupInstance.id}"  params="['pos':pos]">
				<img class="logo" alt="${userGroupInstance.name}"
					src="${createLink(url: userGroupInstance.mainImage()?.fileName)}">
			</g:link>

		</div>
		<div class="span5 observation_story_wrapper">
			<uGroup:showStory model="['userGroupInstance':userGroupInstance]"></uGroup:showStory>
		</div>
	</div>
</div>
