<a class="pull-left" style="margin-left: 0px;"
	href="${uGroup.createLink(mapping:'userGroup',  action:'show',
			'userGroup':userGroupInstance)}">
	<img class="logo" src="${userGroupInstance.mainImage()?.fileName}"
	title="${userGroupInstance.name}" alt="${userGroupInstance.name}" /> </a>

<a class="brand"
	href="${uGroup.createLink(mapping:'userGroup',  action:'show',
			'userGroup':userGroupInstance)}"><h1>
	${userGroupInstance.name}
</h1>
</a>
<!-- span class="group-desc ellipsis multiline"> ${userGroupInstance.description}
</span-->
<div id="actionsHeader" style="position: relative; overflow: visible;">
	<uGroup:showActionsHeaderTemplate
		model="['userGroupInstance':userGroupInstance, members_autofillUsersId:1]" />
</div>



<g:javascript>

	window.reloadActionsHeaderUrl = "${uGroup.createLink(controller:'userGroup', action:'actionsHeader','userGroup':userGroupInstance) }";
	window.joinUsUrl =  "${uGroup.createLink(controller:'userGroup',action:'joinUs','userGroup':userGroupInstance) }";
	window.requestMembershipUrl = "${uGroup.createLink(controller:'userGroup',action:'requestMembership','userGroup':userGroupInstance) }";
	window.leaveUrl = "${uGroup.createLink(controller:'userGroup',action:'leaveUs', 'userGroup':userGroupInstance) }";
	window.inviteMembersFormUrl = "${uGroup.createLink(controller:'userGroup',action:'inviteMembers', 'userGroup':userGroupInstance)}";
	window.isLoggedInUrl = "${createLink(controller:'SUser', action:'isLoggedIn','userGroup':userGroupInstance)}";
	window.loginUrl = "${createLink(controller:'login','userGroup':userGroupInstance)}"
	window.aboutUrl = "${uGroup.createLink(controller:'userGroup',action:'about', 'userGroup':userGroupInstance) }";
	window.userTermsUrl = "${createLink(controller:'SUser', action: 'terms','userGroup':userGroupInstance)}";
	window.members_autofillUsersId = 1;
	// Don''t put any code here. Put it in init_header function in membership.js
</g:javascript>