


		<g:link controller="userGroup" action="show" class="span3 logo" style="margin-left:0px;"
			id="${userGroupInstance.id}">
			<img class="logo" src="${userGroupInstance.mainImage()?.fileName}"
				title="${userGroupInstance.name}" alt="${userGroupInstance.name}" />
		</g:link>

		<h1>
			${userGroupInstance.name}
		</h1>
		<span class="ellipsis multiline"> ${userGroupInstance.description}
		</span>
		<div id="actionsHeader" style="position: relative; overflow: visible;">
			<uGroup:showActionsHeaderTemplate
				model="['userGroupInstance':userGroupInstance]" />
		</div>
	




<r:script>
window.reloadActionsHeaderUrl = "${createLink(action:'actionsHeader',id:userGroupInstance.id) }";
window.joinUsUrl =  "${createLink(action:'joinUs',id:userGroupInstance.id) }";
window.requestMembershipUrl = "${createLink(action:'requestMembership',id:userGroupInstance.id) }";
window.leaveUrl = "${createLink(action:'leaveUs',id:userGroupInstance.id) }";
window.inviteMembersFormUrl = '${createLink(action:'inviteMembers',id:userGroupInstance.id)}';
window.isLoggedInUrl = "${createLink(controller:'SUser', action:'isLoggedIn')}";
window.loginUrl = '${createLink(controller:'login')}'

var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({
	usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
});

$(document).ready(function(){
	$(".ellipsis").trunk8({
		lines:2,
		fill: '&hellip;&nbsp;<a	href="${createLink(action:'aboutUs', id:userGroupInstance.id) }">read more</a>&nbsp;'
	})
})

</r:script>