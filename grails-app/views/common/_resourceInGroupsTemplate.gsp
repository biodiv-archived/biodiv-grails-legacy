<div class="resource_in_groups">
	<g:if test="${observationInstance.userGroups}">
			<h5>Is in groups</h5>
			<ul class="tile" style="list-style: none;">
				<g:each in="${observationInstance.userGroups}" var="userGroup">
					<li class="pull-left checkbox">
						<uGroup:showUserGroupSignature
							model="[ 'userGroup':userGroup]" />
					</li>
				</g:each>
			</ul>
	</g:if>
</div>
