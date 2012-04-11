<div id="show-flags" class="btn-group dropdown"
	${(observationInstance.flagCount == 0)?"style='display:none'":""}>
	<a data-toggle="dropdown" href="#show-flags"> <i class="icon-flag"></i>
		Show Flags </a>
	<ul class="dropdown-menu">
		<g:each in="${observationInstance.fetchAllFlags()}">
			<li style="padding: 0 5px; clear: both;">
				${it.author.username} : ${it.flag.value()}
			</li>
		</g:each>
	</ul>
</div>