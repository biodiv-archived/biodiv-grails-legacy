
<a class="span3 logo" style="margin-left: 0px;"
	href="${uGroup.createLink(mapping:'userGroup',  action:'show',
			'userGroup':userGroupInstance)}">
	<img class="logo" src="${userGroupInstance.mainImage()?.fileName}"
	title="${userGroupInstance.name}" alt="${userGroupInstance.name}" /> </a>

<h1>
	${userGroupInstance.name}
</h1>
<span class="ellipsis multiline"> ${userGroupInstance.description}
</span>
<div id="actionsHeader" style="position: relative; overflow: visible;">
	<uGroup:showActionsHeaderTemplate
		model="['userGroupInstance':userGroupInstance]" />
</div>





<g:javascript>
	window.reloadActionsHeaderUrl = "${uGroup.createLink(mapping:'userGroup', action:'actionsHeader','userGroup':userGroupInstance) }";
	window.joinUsUrl =  "${uGroup.createLink(mapping:'userGroup', action:'joinUs','userGroup':userGroupInstance) }";
	window.requestMembershipUrl = "${uGroup.createLink(mapping:'userGroup', action:'requestMembership','userGroup':userGroupInstance) }";
	window.leaveUrl = "${uGroup.createLink(mapping:'userGroup', action:'leaveUs', 'userGroup':userGroupInstance) }";
	window.inviteMembersFormUrl = "${uGroup.createLink(mapping:'userGroup', action:'inviteMembers', 'userGroup':userGroupInstance)}";
	window.isLoggedInUrl = "${createLink(controller:'SUser', action:'isLoggedIn')}";
	window.loginUrl = '${createLink(controller:'login')}'
	
	var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	$(document).ready(function(){
		$(".ellipsis").trunk8({
			lines:2,
			fill: '&hellip;&nbsp;<a
		href="${uGroup.createLink(mapping:'userGroup', action:'about', 'userGroup':userGroupInstance) }">read
		more</a>&nbsp;'
		})
	})

</g:javascript>