
<div class="thumbnail">
	<g:set var="mainImage" value="${userInstance.icon()}" />
	<div class="figure" style="height:150px;">
		<a href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
			<img src="${userInstance.icon()}" class="normal_profile_pic"
				title="${userInstance.name}" />
		</a>
	</div>

	<div class="all_content">
		<sUser:showUserStoryTablet model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserStoryTablet>
	</div>
</div>
