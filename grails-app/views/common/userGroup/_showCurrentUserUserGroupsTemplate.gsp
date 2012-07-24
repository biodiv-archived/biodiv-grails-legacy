<g:each in="${userGroups}"	var="userGroup" status="i">
	<li><label class="checkbox"><g:checkBox style="margin-left:0px;"
			name="userGroup.${i}" value="${userGroup.id}"/>
		${userGroup.name}</label>
	</li>
</g:each>