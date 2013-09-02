

<g:set var="mainImage" value="${userInstance.profilePicture()}" />
<div class="figure span2 observation_story_image"
	style="display: table; height: 220px;">
	<a
		href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
		<img src="${userInstance.profilePicture()}" class="img-polaroid"
		title="${userInstance.name}" /> </a>
</div>

<sUser:showUserStory
	model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserStory>

