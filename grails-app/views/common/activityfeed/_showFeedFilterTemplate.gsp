<%@page import="species.participation.Observation"%>
<%@page import="species.groups.UserGroup"%>
<div class="feedFilterDiv">
	<div class="btn-group pull-left" style="z-index: 10; clear:both">
		<button id="feedFilterButton" class="btn dropdown-toggle" data-toggle="dropdown" href="#" rel="tooltip"
			data-original-title="Filter by" onclick="$('#feedFilter').show(); return false;"> All <span class="caret"></span>
		</button>
		
		<ul id="feedFilter" class="dropdown-menu" style="width: auto;">
			<li class="group_option"><a class=" feed_filter_label"
				value="All"> All </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Observation.class.getCanonicalName()}"> Observation </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${UserGroup.class.getCanonicalName()}"> Group </a></li>
		</ul>
	</div>
</div>
