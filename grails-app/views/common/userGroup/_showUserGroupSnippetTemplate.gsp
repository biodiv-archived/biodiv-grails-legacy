<g:set var="mainImage" value="${userGroupInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>


<td
	title='<g:if test="${userGroupTitle != null}">${userGroupTitle}</g:if>'>

	<g:link controller="userGroup" action="show" id="${userGroupInstance.id}" params="['pos':pos]">
		<img class="logo" alt="${userGroupInstance.name}"
			src="${createLink(url: userGroupInstance.mainImage()?.fileName)}">
	</g:link></td>

<uGroup:showStory model="['userGroupInstance':userGroupInstance, 'showLeave':(showLeave != null)?showLeave:false]"></uGroup:showStory>

