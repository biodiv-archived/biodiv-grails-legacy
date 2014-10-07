<g:each var="commentInstance" in="${comments}">
	<li class="${commentInstance.id}">${userLanguage}<comment:showComment
			model="['commentInstance':commentInstance, 'userLanguage':userLanguage]" /></li>
</g:each>
