<g:if test="${canPullResource || observationInstance}">
	<div class="sidebar_section" style="clear:both;overflow:hidden;border:1px solid #CECECE;">
		<g:if test="${observationInstance}">
			<uGroup:resourceInGroups
					model="['observationInstance':observationInstance]"  />
		</g:if>
		
		<uGroup:objectPostToGroups
				model="['objectType':objectType, userGroup:params.userGroup, canPullResource:canPullResource, 'observationInstance':observationInstance]" />
	</div>
</g:if>