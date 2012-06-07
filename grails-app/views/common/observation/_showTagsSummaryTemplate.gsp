<%@ page import="species.participation.Observation"%>
<div class="view_tags">
	<g:if test="${tags}">
                <i class="icon-tags"></i> Tags
		<ul class="tagit">
			<g:each in="${tags.entrySet()}">
				<li class="tagit-choice" style="padding:0 5px;">
					${it.getKey()} <span class="tag_stats"> ${it.getValue()}</span>
				</li>
			</g:each>
		</ul>
	</g:if>
</div>
