<div class="mainContentList">
	<div class="mainContent" name="p${params?.offset}">
		<ul style="list-style:none;">
			<g:each in="${discussionInstanceList}" status="i"
				var="discussionInstance">
				<li>
						<g:render template="/discussion/showDiscussionStoryTemplate" model="['discussionInstance':discussionInstance, canPullResource:canPullResource, 'userGroupInstance':userGroupInstance]"/>
				</li>
			</g:each>
		</ul>
				
	</div>
</div>
