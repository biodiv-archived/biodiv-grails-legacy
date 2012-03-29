<div class="prop tablet">
		<div class="user-icon">
			<a href=/biodiv/SUser/show/${userInstance.id}> <img
				src="${userInstance.icon()}" class="small_profile_pic"
				title="${userInstance.username}" /> </a>
		</div>
		
		<div class="all_content username-value" >
			<sUser:showUserStoryTablet model="['userInstance':userInstance]"></sUser:showUserStoryTablet>
		</div>
</div>