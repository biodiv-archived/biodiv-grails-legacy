<div class="resource_in_groups">
	<g:if test="${observationInstance.userGroups}">
		<div class="sidebar_section ">
			<h5>Is in groups</h5>
			<ul class="tile" style="list-style: none; padding-left: 10px;">
				<g:each in="${observationInstance.userGroups}" var="userGroup">
					<li class=""><uGroup:showUserGroupSignature
							model="[ 'userGroup':userGroup]" /></li>
				</g:each>
			</ul>
		</div>
	</g:if>
</div>
