<%@page import="species.participation.Observation"%>
<%@page import="species.participation.Discussion"%>
<%@page import="content.eml.Document"%>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.participation.Checklists"%>
<%@page import="species.Species"%>

<div class="feedFilterDiv">
	<div class="btn-group pull-right" style="z-index: 10; clear:both">
		<button id="feedFilterButton" class="btn dropdown-toggle" data-toggle="dropdown" href="#" rel="tooltip"
			data-original-title="${g.message(code:'showfeedfilter.filter.by')}"> <g:message code="default.group.label" /> <span class="caret"></span>
		</button>
		
		<ul id="feedFilter" class="dropdown-menu" style="width: auto;">
			<li class="group_option"><a class=" feed_filter_label"
				value="All"> <g:message code="default.all.label" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Observation.class.getCanonicalName()}"> <g:message code="default.observation.label" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${UserGroup.class.getCanonicalName()}"> <g:message code="default.group.label" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Checklists.class.getCanonicalName()}"> <g:message code="default.checklist.label" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Species.class.getCanonicalName()}"> <g:message code="default.species.label" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Discussion.class.getCanonicalName()}"> <g:message code="default.discussion.label" /> </a></li>
			<li class="group_option"><a class=" feed_filter_label"
				value="${Document.class.getCanonicalName()}"> <g:message code="default.document.label" /> </a></li>		
		</ul>
	</div>
</div>
