<%@page import="species.participation.Observation"%>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.participation.Checklists"%>
<%@page import="species.Species"%>

<div class="feedFilterDiv">
	<div class="btn-group pull-right" style="z-index: 10; clear:both">
		<button id="feedFilterButton" class="btn dropdown-toggle" data-toggle="dropdown" href="#" rel="tooltip"
			data-original-title="Filter by"> <g:message code="msg.Map.view" /> <span class="caret"></span>
		</button>
		
		<ul id="feedFilter" class="dropdown-menu" style="width: auto;">
			<li class="group_option"><a class=" feed_filter_label"
				value="All"> <g:message code="msg.All" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Observation.class.getCanonicalName()}"> <g:message code="msg.Observation" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${UserGroup.class.getCanonicalName()}"> <g:message code="msg.Group" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Checklists.class.getCanonicalName()}"> <g:message code="msg.Checklist" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Species.class.getCanonicalName()}"> <g:message code="msg.Species" /> </a></li>
		</ul>
	</div>
</div>
