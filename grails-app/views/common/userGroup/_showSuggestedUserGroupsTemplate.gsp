
<li class="dropdown"><a class="dropdown-toggle" style="padding: 0"
	data-toggle="dropdown"
	href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}">
		<%--	<i class="icon-group"></i>--%> <b class="caret"
		style="border-top-color: black; border-bottom-color: black; display: none;"></b>
		<uGroup:showUserGroupSignature model="['userGroup':userGroups.get(0)]" />
</a>

	<ul class="dropdown-menu" style="">
		<g:if test="${userGroups}">

			<g:each in="${userGroups.subList(1, userGroups.size())}"
				var="userGroup">
				<li><uGroup:showUserGroupSignature
						model="['userGroup':userGroup]" /></li>
			</g:each>
		</g:if>
		<li><obv:showRelatedStory
					model="['controller':'userGroup', 'observationId': 1, 'action':'getFeaturedUserGroups', 'id':'uG', hideShowAll:true]" /></li>
		<li style="float: right;"><g:link mapping="userGroupGeneric"
				action="list" absolute='true'>More ...</g:link>
		</li>
	</ul>
</li>
