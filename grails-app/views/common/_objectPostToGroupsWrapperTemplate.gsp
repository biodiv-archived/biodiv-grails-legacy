<uGroup:resourceInGroups
		model="['observationInstance':observationInstance]" />

<div class="sidebar_section"
	style="clear: both; overflow: hidden; border: 1px solid #CECECE;">
	<uGroup:objectPostToGroups
		model="['objectType':observationInstance.class.canonicalName, userGroup:params.userGroup, canPullResource:canPullResource, 'observationInstance':observationInstance]" />
</div>