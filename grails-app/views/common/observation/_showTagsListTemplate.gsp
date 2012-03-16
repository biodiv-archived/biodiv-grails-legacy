<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.participation.Observation"%>

<div class="view_tags grid_4">
	<ul class="tagit">
		<g:each in="${tags}">
			<li class="tagit-choice" style="padding:0 5px;clear:both;">
				${it} <span class="tag_stats"> ${Observation.countByTag(it.name)}</span>
			</li>
		</g:each>
	</ul>
</div>