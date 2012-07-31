<g:each in="${userGroups}"	var="userGroup" status="i">
	<li><label class="checkbox">
	
	<%
		boolean checked = userGroup.value;
		if(params.userGroup) {
			if(params.userGroup instanceof String) {
				checked = checked || (params.userGroup == String.valueOf(userGroup.key.id));
			} else {
				checked = checked || params.userGroup.containsValue(String.valueOf(userGroup.key.id))
			}
		}
	 %>
	<input type="checkbox" style="margin-left:0px;"
			name="userGroup.${i}" value="${userGroup.key.id}" ${checked?'checked':''}/>
		${userGroup.key.name}</label>
	</li>
</g:each>
