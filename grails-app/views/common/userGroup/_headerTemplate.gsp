		<g:link mapping="userGroup" action="show" class="span3 logo" style="margin-left:0px;"
			params="['webaddress':userGroupInstance.webaddress]">
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
window.reloadActionsHeaderUrl = "${createLink(mapping:'userGroup', action:'actionsHeader',params="['webaddress':userGroupInstance.webaddress]") }";
window.joinUsUrl =  "${createLink(mapping:'userGroup', action:'joinUs',params="['webaddress':userGroupInstance.webaddress]") }";
window.requestMembershipUrl = "${createLink(mapping:'userGroup', action:'requestMembership',params="['webaddress':userGroupInstance.webaddress]") }";
window.leaveUrl = "${createLink(mapping:'userGroup', action:'leaveUs',params="['webaddress':userGroupInstance.webaddress]") }";
window.inviteMembersFormUrl = "${createLink(mapping:'userGroup', action:'inviteMembers',params="['webaddress':userGroupInstance.webaddress]")}";
window.isLoggedInUrl = "${createLink(controller:'SUser', action:'isLoggedIn')}";
window.loginUrl = '${createLink(controller:'login')}'

var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({
	usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
});

$(document).ready(function(){
	$(".ellipsis").trunk8({
		lines:2,
		fill: '&hellip;&nbsp;<a	href="${createLink(mapping:'userGroup', action:'aboutUs', params:"['webaddress':userGroupInstance.webaddress]") }">read more</a>&nbsp;'
	})
})

</r:script>