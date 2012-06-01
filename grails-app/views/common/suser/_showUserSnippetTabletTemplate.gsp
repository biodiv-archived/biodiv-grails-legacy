
<div class="snippet tablet">
	<g:set var="mainImage" value="${userInstance.icon()}" />
	<div class="figure" style="height:150px;">
		<g:link action="show" controller="SUser" id="${userInstance.id}">
			<img src="${userInstance.icon()}" class="normal_profile_pic"
				title="${userInstance.name}" />
		</g:link>
	</div>

	<div class="all_content">
		<sUser:showUserStoryTablet model="['userInstance':userInstance]"></sUser:showUserStoryTablet>
	</div>
</div>
