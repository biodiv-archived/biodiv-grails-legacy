<div id="show-flags" class="btn-group" style="display:none">
    <a data-toggle="dropdown" href="#">
    <i class="icon-flag"></i>
    Show Flags
    </a>
    <ul class="dropdown-menu">
    <g:each in="${observationInstance.fetchAllFlags()}">
			<li style="padding:0 5px;clear:both;">
				${it.author.username} : ${it.flag.value()} 
			</li>
	</g:each>
    </ul>
</div>

<g:javascript>
	$(document).ready(function(){
		if(${observationInstance.flagCount} > 0){
			$("#show-flags").show();
		}
	})
</g:javascript>		