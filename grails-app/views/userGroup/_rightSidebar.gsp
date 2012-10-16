
<div class="sidebar right-sidebar span3">
	<div class="sidebar_section" style="left: 0px">
		<h5 class="nav-header">Bookmarks</h5>
		
		<ul class="nav block-tagadelic">
			<g:each in="${pages}" var="newsletterInstance">

				<li><g:if test="${userGroupInstance}">
						<a
							href="${createLink(mapping:'userGroupPageShow', params:['webaddress':userGroupInstance.webaddress, 'newsletterId':newsletterInstance.id]) }">
							${fieldValue(bean: newsletterInstance, field: "title")} </a>
					</g:if>
					<g:else>
						<a
							href="${createLink(controller:'userGroup', action:'page', id:newsletterInstance.id) }">
							${fieldValue(bean: newsletterInstance, field: "title")} </a>
					</g:else></li>
			</g:each>
			<li><g:if test="${userGroupInstance}">
						<a
							href="${createLink(mapping:'userGroup', controller:"userGroup", action:"pages", params:['webaddress':userGroupInstance.webaddress])}">Pages </a>
				</g:if> <g:else>
					<g:link mapping="pages">Pages</g:link>
				</g:else>
			</li>
		</ul>
	</div>
</div>
