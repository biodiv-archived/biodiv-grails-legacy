<%@ page import="species.participation.Observation"%>
<style>
.tag_label {
	background-color: #DEE7F8;
	border: 1px solid #CAD8F3;
	padding: 2px 13px 3px 4px;
	border-radius: 5px 5px 5px 5px;
	display: block;
	float: left;
	margin: 2px 5px 2px 0;
	clear: both;
}

.tag_stats {
	color: #444444;
	font-size: 120%;
	font-weight: bold;
}

.tag_stat_div {
	clear: both;
}

ul.tagit li { 
	//clear: both;
}
</style>
<div class="grid_5 sidebar_section view_tags">
	<g:if test="${observationInstance.tags}">
		<div class="title">Tagged</div>

		<ul class="tagit">
			<g:each in="${observationInstance.tags}">
				<li class="tagit-choice">
					${it} <span class="tag_stats"> x ${Observation.countByTag(it)}
				</span></li>
			</g:each>
		</ul>
	</g:if>
</div>