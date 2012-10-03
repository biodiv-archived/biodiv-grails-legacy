<g:if test="${userGroups}">
<h5 class="nav-header" title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality.">Groups<sup>Beta</sup></h5>
<div class="block-tagadelic">
<ul class="nav" style="overflow:hidden;margin-botom:0px;">	
	<g:each in="${userGroups}" var="userGroup">
		<li><uGroup:showUserGroupSignature
				model="['userGroup':userGroup]" />
		</li>
	</g:each>
	<li class="pull-right"><g:link controller="userGroup"
			action="list" style="display:inline;">More ...</g:link></li>
</ul>
</div>
</g:if>