<div class="snippet tablet">
	<div class="figure" style="height:150px; width:150px;">
		<a href="${uGroup.createLink([action:"show", controller:"user", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
			<img src="${userInstance.profilePicture()}" class="img-polaroid"
				title="${userInstance.name}" />
		</a>
	</div>

	<div class="all_content caption">
		<sUser:showUserStoryTablet model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserStoryTablet>
	</div>
</div>
