<li class="dropdown" style="height:50px;">
	<a class="dropdown-toggle"  style="height:30px;top:10px"
		data-toggle="dropdown"
		href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}"
		onclick="loadSuggestedGroups($(this).next('ul'), '${uGroup.createLink(controller:'userGroup', action:'suggestedGroups')}');return false;">
			<i class="icon-group"></i> Groups <b class="caret"
			style="border-top-color: black; border-bottom-color: black;"></b>
	</a>
	<ul class="dropdown-menu" style="padding:5px">
	</ul>
</li>
