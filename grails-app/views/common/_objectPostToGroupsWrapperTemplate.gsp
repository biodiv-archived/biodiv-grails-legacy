<g:if test="${observationInstance.userGroups}">
	<div class="sidebar_section">
		<h5>Is in groups</h5>
		<ul class="tile" style="list-style: none; padding-left: 10px;">
			<g:each in="${observationInstance.userGroups}" var="userGroup">
				<li class=""><uGroup:showUserGroupSignature
						model="[ 'userGroup':userGroup]" /></li>
			</g:each>
		</ul>
	</div>
</g:if>
<div class="sidebar_section"
	style="clear: both; overflow: hidden; border: 1px solid #CECECE;">
	<uGroup:objectPostToGroups
		model="['objectType':observationInstance.class.canonicalName, userGroup:params.userGroup, canPullResource:canPullResource, 'observationInstance':observationInstance]" />
</div>