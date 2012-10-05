<g:set var="mainImage" value="${userGroupInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>


<td
	title='<g:if test="${userGroupTitle != null}">${userGroupTitle}</g:if>'>

	<uGroup:showUserGroupSignature
		model="[ 'userGroup':userGroupInstance, 'pos':pos]" />
</td>

<uGroup:showStory
	model="['userGroupInstance':userGroupInstance, 'showLeave':(showLeave != null)?showLeave:false, 'showJoin':(showJoin != null)?showJoin:true]"></uGroup:showStory>

