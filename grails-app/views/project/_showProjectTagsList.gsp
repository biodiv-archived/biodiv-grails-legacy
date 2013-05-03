<%@ page import="org.grails.taggable.Tag"%>
<div class="view_tags">
	<g:if test="${projectInstance?.tags}">
		<ul class="tagit">
		<g:each in="${projectInstance.tags}">
			<li class="tagit-choice" style="padding:0 5px;">
				${it}
			</li>
		</g:each>
		</ul>
	</g:if>
	<g:else>
		<span class="msg" style="padding-left: 50px;">No tags</span>
	</g:else>
	
<g:javascript>
	$(document).ready(function() {
		if((${isAjaxLoad?:'false'} == 'false') || (!${isAjaxLoad?1:0})){
			 $("li.tagit-choice").click(function(){
			 	var tg = $(this).contents().first().text();
	         	window.location.href = "${uGroup.createLink(controller:'project', action: 'list')}?tag=" + tg ;
	         });
         }
	})
</g:javascript>
</div>
