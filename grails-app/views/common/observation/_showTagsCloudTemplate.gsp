<div>
	<g:if test="${tags}">
		<tc:tagCloud tags="${tags}" action="list" sort="${true}" style 
						color="${[start: '#084B91', end: '#9FBBE5']}"
						size="${[start: 12, end: 30, unit: 'px']}"
						paramName='tag'/>
						
		<g:if test="${showMoreTagPageLink}">
			<span class="pull-right"><a href="${showMoreTagPageLink}">more
					tags</a></span>
		</g:if>
	</g:if>					
	<g:else>
		<span class="msg" style="padding-left: 50px;">No tags</span>
	</g:else>
</div>
