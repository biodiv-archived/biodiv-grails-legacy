<ul>
	<g:each in="${votes}" var="recoVote">
		<li>by <g:link controller="sUser" action="show"
				id="${recoVote?.author.id}">
				${recoVote?.author.username}
			</g:link> <g:message code="text.on" /> <g:formatDate format="MMMMM dd, yyyy" date="${recoVote?.votedOn}" />
			<g:message code="text.with.confidence" />  "${recoVote.confidence.value() }"
		</li>
	</g:each>
</ul>
