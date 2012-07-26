<g:each var="commentInstance" in="${comments}">
	<li><comment:showComment
			model="['commentInstance':commentInstance]" />
	</li>
</g:each>
