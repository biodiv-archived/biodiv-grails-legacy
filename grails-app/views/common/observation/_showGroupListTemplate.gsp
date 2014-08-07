<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>

<div class="view_tags grid_4">
	<h5><g:message code="msg.Groups" /></h5>
	<ul class="tagit">
		<g:each in="${SpeciesGroup.list()}">
			<g:if test="${it.name.equals('All')}">
				<li style="padding:0 5px;clear:both;">
					${it.name} <span class="tag_stats"> ${Observation.count()}</span>
				</li>
			</g:if>
			<g:else>
				<li style="padding:0 5px;clear:both;">
					${it.name} <span class="tag_stats"> ${Observation.getCountForGroup(it.id)}</span>
				</li>
			</g:else>
		</g:each>
	</ul>
</div>
