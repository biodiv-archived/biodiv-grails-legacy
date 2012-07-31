<g:each in="${userGroups}"	var="userGroup" status="i">
	<li><label class="checkbox"><input type="checkbox" style="margin-left:0px;"
			name="userGroup.${i}" value="${userGroup.key.id}" ${(userGroup.value || (params.userGroup && params.userGroup.containsValue(userGroup.key.id+"")))?'checked':''}/>
		${userGroup.key.name}</label>
	</li>
</g:each>
