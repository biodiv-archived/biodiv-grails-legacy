<li class="dropdown">
	<a class="dropdown-toggle" 
		data-toggle="dropdown" style="color:#bbb;"
		href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}"
		onclick="loadSuggestedGroups($(this).next('ul'), '${uGroup.createLink(controller:'userGroup', action:'suggestedGroups')}');return false;">
			<i class="icon-group-gray"></i> <g:message code="default.groups.label" /> <b class="caret"></b>
	</a>
	<ul class="dropdown-menu" style="width:254px;max-height:300px;overflow-x:hidden;overflow-y:auto;">
	<li style="text-align:center;overflow-x:hidden; overflow-y:auto;">
	    <g:link mapping="userGroupGeneric" action="list" absolute='true'><g:message code="text.see.all" /></g:link>
	</li>
	<li class="group_load" style="text-align:center;overflow-x:hidden; overflow-y:auto;">
	    <g:message code="text.list.loading" />
	</li>

	</ul>	
</li>


