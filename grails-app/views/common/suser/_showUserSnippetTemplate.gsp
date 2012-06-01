
<div class="snippet span11">
	<div class="row">
		<g:set var="mainImage" value="${userInstance.icon()}" />
		<div class="figure span3 observation_story_image" style="display: table;" >
			<g:link action="show" controller="SUser" id="${userInstance.id}">
				<img src="${userInstance.icon()}" class="normal_profile_pic"
					title="${userInstance.name}" />
			</g:link>
		</div>

		<div class="span8">
			<sUser:showUserStory model="['userInstance':userInstance]"></sUser:showUserStory>
		</div>
	</div>
</div>
