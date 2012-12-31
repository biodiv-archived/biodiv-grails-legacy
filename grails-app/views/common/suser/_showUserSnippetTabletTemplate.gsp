<div class="snippet tablet">
	<g:set var="mainImage" value="${userInstance.icon()}" />
	<div class="figure" style="height:150px; width:200px;display:table;">
		<a href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
			<img src="${userInstance.icon()}" class="normal_profile_pic img-polaroid"
				title="${userInstance.name}" />
		</a>
	</div>

	<div class="all_content caption">
		<sUser:showUserStoryTablet model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserStoryTablet>
	</div>
</div>
