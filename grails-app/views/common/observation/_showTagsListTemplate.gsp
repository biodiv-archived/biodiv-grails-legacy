<%@ page import="org.grails.taggable.Tag"%>
<div class="view_tags">
	<g:if test="${tags}">
		<ul class="tagit">
		<g:each in="${tags.entrySet()}">
			<li class="tagit-choice" style="padding:0 5px;clear:both;">
				${it.getKey()} <span class="tag_stats"> ${it.getValue()}</span>
			</li>
		</g:each>
		</ul>
	</g:if>
	<g:else>
		<span class="msg" style="padding-left: 50px;">No tags</span>
	</g:else>
	
<script type="text/javascript">
	$(document).ready(function() {
		if((${isAjaxLoad?:'false'} == 'false') || (!${isAjaxLoad?1:0})){
			 $("li.tagit-choice").click(function(){
			 	var tg = $(this).contents().first().text();
	         	window.location.href = "${uGroup.createLink(controller:'observation', action: 'list', 'userGroupWebaddress':params.webaddress)}?tag=" + tg ;
	         });
         }
	})
</script>
</div>
