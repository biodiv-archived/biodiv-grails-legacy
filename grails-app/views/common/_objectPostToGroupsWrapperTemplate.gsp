<g:if test="${observationInstance || canPullResource}">
	<div class="sidebar_section" style="clear:both;overflow:hidden;border:1px solid #CECECE;">
		<g:if test="${observationInstance}">
			<uGroup:resourceInGroups
					model="['observationInstance':observationInstance]"  />
		</g:if>
		
		<uGroup:objectPostToGroups
				model="['objectType':objectType, userGroup:params.userGroup, canPullResource:canPullResource, isBulkPull:isBulkPull, 'observationInstance':observationInstance]" />
	</div>
</g:if>