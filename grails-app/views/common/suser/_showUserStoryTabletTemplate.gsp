<%@page import="species.participation.Observation"%>
<div class="observation_story tablet">
	<div class="prop tablet">
		<a href=/biodiv/SUser/show/${userInstance.id}> ${userInstance.username}
		</a>
	</div>

	<div class="prop tablet summary">
		<span class="observations_summary name tablet "></span>
		<div class="value tablet">
			${Observation.countByAuthor(userInstance)}
		</div>
			
	</div>
	
	<div class="prop tablet summary">
		<span class="tags_summary name tablet "></span>
		<div class="value tablet">
		<obv:showNoOfTagsOfUser model="['userId':userInstance.id]" />
		</div>
	</div>


</div>


