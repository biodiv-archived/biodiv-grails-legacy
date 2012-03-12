<%@ page import="species.participation.Observation"%>
<div class="view_tags">
	<ul name="tags">
		<g:each in="${observationInstance.tags}">
			<li>
				${it} x ${Observation.countByTag(tag)}
			</li>
		</g:each>
	</ul>
</div>
