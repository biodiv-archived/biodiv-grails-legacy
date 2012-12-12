<div class="snippet tablet" style="height:auto; width:auto">
	<div class="all_content">
		<div class="observation_story tablet">
		  	<h5><a href="${uGroup.createLink(controller:'checklist', action:'show', id:checklistInstance.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress)}">${checklistInstance.title}</a></h5> 
        	<div class="icons-bar">
            	<div class="observation-icons">
                    <span class="group_icon species_groups_sprites active ${checklistInstance.speciesGroup.iconClass()}" title="${checklistInstance.speciesGroup?.name}"></span>
            	</div>

            	<div class="user-icon">
                    <a href="${uGroup.createLink(controller:'SUser', action:'show', id:checklistInstance.author.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress)}"> <img
                            src="${checklistInstance.author.icon()}" class="small_profile_pic"
                            title="${checklistInstance.author.name}" />
                    </a>
            	</div>
        	</div>
		</div>
	</div>
</div>
