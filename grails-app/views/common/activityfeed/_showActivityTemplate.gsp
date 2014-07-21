<b> ${feedInstance.author.name} :<span class="yj-context"> ${raw(activityTitle)}</span></b>
<g:if test="${feedText != null}">
	<div class="feedActivityHolderContext yj-message-body">
		<pre>${raw(feedText)}</pre>
	</div>
</g:if>
