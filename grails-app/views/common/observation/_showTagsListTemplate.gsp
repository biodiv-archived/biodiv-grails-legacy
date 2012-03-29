<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.participation.Observation"%>

<div class="view_tags grid_4">
	<ul class="tagit">
		<g:each in="${tags}">
			<li class="tagit-choice" style="padding:0 5px;clear:both;">
				${it} <span class="tag_stats"> ${Observation.countByTag(it)}</span>
			</li>
		</g:each>
	</ul>
	<g:javascript>
	$(document).ready(function() {
		 $("li.tagit-choice").click(function(){
         	var tg = $(this).contents().first().text();
         	window.location.href = "${g.createLink(controller:'observation', action: 'list')}/?tag=" + tg ;
         });
	})
</g:javascript>
</div>
