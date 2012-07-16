<%@ page import="species.participation.Observation"%>
<div class="view_tags">
	<g:if test="${observationInstance.tags}">

		<ul class="tagit">
			<g:each in="${observationInstance.tags}">
				<li class="tagit-choice" style="padding:0 5px;">
					${it} <span class="tag_stats"> ${Observation.countByTag(it)}</span>
				</li>
			</g:each>
		</ul>
	</g:if>
</div>