<%@ page import="org.grails.taggable.Tag"%>
<%
def controller = controller?controller:'document'
def action = action?action:'browser'
 %>
<div class="view_tags">
	<g:if test="${instance?.tags}">
        <ul class="${controller}_tagit tagit">
		<g:each in="${instance.tags}">
			<li class="tagit-choice" style="padding:0 5px;">
				${it}
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
                        $(".${controller}_tagit li.tagit-choice").click(function(){
			    var tg = $(this).contents().first().text();
	         	    window.location.href = "${uGroup.createLink(controller:controller, action: action, userGroupWebaddress:params.webaddress)}?tag="+tg ;
	                });
         }
	})
</script>
</div>
