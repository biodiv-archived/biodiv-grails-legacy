<a class="span3 logo" style="margin-left: 0px;"
	href="${uGroup.createLink(mapping:'userGroup',  action:'show',
			'userGroup':userGroupInstance)}">
	<img class="logo" src="${userGroupInstance.mainImage()?.fileName}"
	title="${userGroupInstance.name}" alt="${userGroupInstance.name}" /> </a>

<h1>
	${userGroupInstance.name}
</h1>
<span class="group-desc ellipsis multiline"> ${userGroupInstance.description}
</span>
<div id="actionsHeader" style="position: relative; overflow: visible;">
	<uGroup:showActionsHeaderTemplate
		model="['userGroupInstance':userGroupInstance]" />
</div>





<g:javascript>
	// Don''t put any code here. Put it in init_header function in membership.js
</g:javascript>