<ul>
	<g:each in="${votes}" var="recoVote">
		<li>By <g:link controller="sUser" action="show"
				id="${recoVote?.author.id}">
				${recoVote?.author.username}
			</g:link> on <g:formatDate format="MMMMM dd, yyyy" date="${recoVote?.votedOn}" />
			with confidence "${recoVote.confidence.value() }"
		</li>
	</g:each>
</ul>