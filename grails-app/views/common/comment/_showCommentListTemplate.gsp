<g:each var="commentInstance" in="${comments}">
	<li class="${commentInstance.id}"><comment:showComment
			model="['commentInstance':commentInstance]" />
	</li>
</g:each>
