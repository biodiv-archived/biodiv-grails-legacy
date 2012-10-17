		<a class="span3 logo" style="margin-left:0px;" href="${uGroup.createLink(mapping:'userGroup',  action:'show',
			'userGroup':userGroupInstance)}">
			<img class="logo" src="${userGroupInstance.mainImage()?.fileName}"
				title="${userGroupInstance.name}" alt="${userGroupInstance.name}" />
		</a>

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
	window.reloadActionsHeaderUrl = "${uGroup.createLink(mapping:'userGroup', action:'actionsHeader','userGroupWebaddress':userGroupInstance.webaddress) }";
	window.joinUsUrl =  "${uGroup.createLink(mapping:'userGroup', action:'joinUs','userGroupWebaddress':userGroupInstance.webaddress) }";
	window.requestMembershipUrl = "${uGroup.createLink(mapping:'userGroup', action:'requestMembership','userGroupWebaddress':userGroupInstance.webaddress) }";
	window.leaveUrl = "${uGroup.createLink(mapping:'userGroup', action:'leaveUs', 'userGroupWebaddress':userGroupInstance.webaddress) }";
	window.inviteMembersFormUrl = "${uGroup.createLink(mapping:'userGroup', action:'inviteMembers', 'userGroupWebaddress':userGroupInstance.webaddress)}";
	window.isLoggedInUrl = "${createLink(controller:'SUser', action:'isLoggedIn')}";
	window.loginUrl = '${createLink(controller:'login')}'
	
	var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	$(document).ready(function(){
		$(".ellipsis").trunk8({
			lines:2,
			fill: '&hellip;&nbsp;<a	href="${uGroup.createLink(mapping:'userGroup', action:'aboutUs', 'userGroupWebaddress':userGroupInstance.webaddress) }">read more</a>&nbsp;'
		})
	})

</r:script>