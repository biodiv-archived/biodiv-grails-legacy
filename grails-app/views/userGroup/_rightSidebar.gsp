
<div class="sidebar right-sidebar">
	<div class="super-section" style="left: 0px">
		<h5 class="nav-header">Bookmarks</h5>
		<ul class="nav block-tagadelic">
			<g:each in="${pages}" var="newsletterInstance">
				<li><a
					href="${createLink(mapping:'userGroupPageShow', params:['id':userGroupInstance.id, 'newsletterId':newsletterInstance.id]) }">
						${fieldValue(bean: newsletterInstance, field: "title")} </a>
				</li>
			</g:each>
			<li><g:link controller="userGroup" action="pages" id="${userGroupInstance.id}">Pages</g:link>
		</ul>
	</div>
</div>