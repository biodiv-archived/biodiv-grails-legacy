

<g:set var="mainImage" value="${userInstance.icon()}" />
<div class="figure span3 observation_story_image"
	style="display: table; height: 220px;">
	<a
		href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
		<img src="${userInstance.icon()}" class="img-polaroid"
		title="${userInstance.name}" /> </a>
</div>

<sUser:showUserStory
	model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserStory>

