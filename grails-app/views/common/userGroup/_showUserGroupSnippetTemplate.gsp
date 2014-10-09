<g:set var="mainImage" value="${userGroupInstance.mainImage()}" />
<%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>
<td title='<g:if test="${userGroupTitle != null}">${userGroupTitle}</g:if>'>
	<div class="figure pull-left observation_story_image" style="height:150px;">
		<a
			href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroupInstance.domainName, 'userGroup':userGroupInstance, 'pos':pos)}">
			<img
			class="normal_profile_pic"
			src="${userGroupInstance.mainImage()?.fileName}" title="${userGroupInstance.name}"
			alt="${userGroupInstance.name}" /> </a>
	</div>

</td>
<td>
    ${userGroupInstance.name}
</td>

<uGroup:showStory
	model="['userGroupInstance':userGroupInstance, 'showLeave':(showLeave != null)?showLeave:false, 'showJoin':(showJoin != null)?showJoin:true]"></uGroup:showStory>

