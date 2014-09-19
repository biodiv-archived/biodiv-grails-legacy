<li class="dropdown">
	<a class="dropdown-toggle" 
		data-toggle="dropdown" style="color:#bbb;"
		href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}"
		onclick="loadSuggestedGroups($(this).next('ul'), '${uGroup.createLink(controller:'userGroup', action:'suggestedGroups')}');return false;">
			<i class="icon-group-gray"></i> <g:message code="default.groups.label" /> <b class="caret"></b>
	</a>
	<ul class="dropdown-menu" style="max-height:300px;overflow-x:hidden;overflow-y:auto;">
	</ul>
</li>
