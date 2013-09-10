<li class="dropdown" style="height:50px;">
	<a class="dropdown-toggle"  style="height:30px;top:10px;color:#bbb;"
		data-toggle="dropdown"
		href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}"
		onclick="loadSuggestedGroups($(this).next('ul'), '${uGroup.createLink(controller:'userGroup', action:'suggestedGroups')}');return false;">
			<i class="icon-group-gray"></i> Groups <b class="caret"
			style="border-top-color: #bbb; border-bottom-color:#bbb;"></b>
	</a>

	<ul class="dropdown-menu" style="padding:5px">
	</ul>
</li>
