<g:each var="commentInstance" in="${comments}">
	<li class="${commentInstance.id}"><comment:showComment
			model="['commentInstance':commentInstance, 'userLanguage':userLanguage]" /></li>
</g:each>
