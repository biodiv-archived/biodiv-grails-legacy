<uGroup:resourceInGroups
		model="['observationInstance':observationInstance]"  />

<uGroup:objectPostToGroups
		model="['objectType':observationInstance.class.canonicalName, userGroup:params.userGroup, canPullResource:canPullResource, 'observationInstance':observationInstance]" />
