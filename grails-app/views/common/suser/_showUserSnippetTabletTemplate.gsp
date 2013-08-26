<div class="snippet tablet">
	<g:set var="mainImage" value="${userInstance.profilePicture()}" />
	<div class="figure" style="height:100px; width:140px;">
		<a href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
			<img src="${userInstance.profilePicture()}" class="img-polaroid"
				title="${userInstance.name}" />
		</a>
	</div>

	<div class="all_content caption">
		<sUser:showUserStoryTablet model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserStoryTablet>
	</div>
</div>
