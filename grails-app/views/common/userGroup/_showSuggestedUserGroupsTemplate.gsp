
<li class="dropdown"> <a class="dropdown-toggle" style="float:right;height:30px;"
	data-toggle="dropdown"
	href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}">

		<b class="caret"
		style="border-top-color: black; border-bottom-color: black;"></b> </a>
	
	<ul class="dropdown-menu">

		<ul class="nav span3"
			style="overflow: hidden; margin-bottom: 0px; margin-left: 10px;width:200px">
			<g:if test="${userGroups}">
				<g:each in="${userGroups.subList(1, userGroups.size())}" var="userGroup">
					<li><uGroup:showUserGroupSignature
							model="['userGroup':userGroup]" /></li>
				</g:each>
			</g:if>
			<li style="float: right;"><g:link mapping="userGroupGeneric"
					action="list" absolute='true'>More ...</g:link>
			</li>
		</ul>
	</ul>
	
	<div class="login-box">
	<uGroup:showUserGroupSignature
		model="['userGroup':userGroups.get(0)]" /></div>
	</li>