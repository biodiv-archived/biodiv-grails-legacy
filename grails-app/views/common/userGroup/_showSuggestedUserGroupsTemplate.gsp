
<ul class="nav span3"
	style="overflow: hidden; margin-bottom: 0px; margin-left: 40px;">
	<g:if test="${userGroups}">
		<g:each in="${userGroups}" var="userGroup">
			<li><uGroup:showUserGroupSignature
					model="['userGroup':userGroup]" />
			</li>
		</g:each>
	</g:if>
	<li style="float:right;"><g:link mapping="userGroupGeneric"
			action="list" absolute='true'>More ...</g:link></li>
</ul>