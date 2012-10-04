
<div class="sidebar right-sidebar">
	<div class="super-section" style="left: 0px">
		<h5 class="nav-header">Bookmarks</h5>
		<ul class="nav block-tagadelic">
			<g:each in="${pages}" var="newsletterInstance">

				<li><g:if test="${userGroupInstance}">
						<a
							href="${createLink(mapping:'userGroupPageShow', params:['id':userGroupInstance.id, 'newsletterId':newsletterInstance.id]) }">
							${fieldValue(bean: newsletterInstance, field: "title")} </a>
					</g:if>
					<g:else>
						<a
							href="${createLink(controller:'userGroup', action:'page', id:newsletterInstance.id) }">
							${fieldValue(bean: newsletterInstance, field: "title")} </a>
					</g:else></li>
			</g:each>
			<li><g:if test="${userGroupInstance}">
					<g:link controller="userGroup" action="pages"
						id="${userGroupInstance.id}">Pages</g:link>
				</g:if> <g:else>
					<g:link controller="userGroup" action="pages">Pages</g:link>
				</g:else>
			</li>
		</ul>
	</div>
</div>