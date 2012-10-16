
<div class="snippet">
		<g:set var="mainImage" value="${userInstance.icon()}" />
		<div class="figure span3 observation_story_image" style="display: table;" >
			<a href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
				<img src="${userInstance.icon()}" class="normal_profile_pic"
					title="${userInstance.name}" />
			</a>
		</div>

		<sUser:showUserStory model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserStory>
</div>
