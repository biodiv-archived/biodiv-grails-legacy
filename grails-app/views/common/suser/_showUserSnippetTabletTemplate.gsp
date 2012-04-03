
<div class="snippet tablet">
	<g:set var="mainImage" value="${userInstance.icon()}" />
	<div class="figure">
		<g:link action="show" controller="SUser" id="${userInstance.id}">
			<img src="${userInstance.icon()}" class="normal_profile_pic"
				title="${userInstance.name}" />
		</g:link>
	</div>
	
	<div class="all_content">
	<div class="some_content">
		<sUser:showUserStoryTablet model="['userInstance':userInstance]"></sUser:showUserStoryTablet>
	</div>
	
	<div class="observation_info_wrapper" style="display:none;">
	
		<div class="prop">
			<span  class="name"><i class="icon-time"></i>Member since </span> <div class="value"> <g:formatDate
					format="yyyy-MM-dd" date="${userInstance.dateCreated}"
					type="datetime" style="MEDIUM" /> </div>
		</div>
		<div class="prop">
			<span class="name"><i class="icon-time"></i>Last visited </span> <div class="value"> <g:formatDate
					format="yyyy-MM-dd" date="${userInstance.lastLoginDate}"
					type="datetime" style="MEDIUM" /> </div>
		</div>
		<div class="btn btn-primary view-button"><g:link action="show" controller="SUser"
			id="${userInstance.id}">View</g:link></div>
		
	</div>
	</div>
</div>
