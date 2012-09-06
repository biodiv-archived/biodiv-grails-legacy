<div class="header">
	
	<div class="top_nav_bar navbar">
		<div class="container">
			<!-- Logo -->
			<div class="logo" style="float:left; margin-left:0px;">
				<a href="${createLink(action:"show", id:userGroupInstance.id)}">
					<img class="logo" alt="${userGroupInstance.name}"
					src="${createLink(url: userGroupInstance.mainImage()?.fileName)}">
				</a>
			</div>
			<div style="margin-left:10px;">
					<h1>${userGroupInstance.name}</h1>
					<span class="ellipsis multiline">
						${userGroupInstance.description}
					</span>
				</div>
			
			<!-- Logo ends -->
			<!-- h1 class="span8">
							${userGroupInstance.name}
			</h1-->
			<ul class="nav" style="clear:both;">
				<li><a href="${createLink(action:'show', id:params.id)}">Home</a>
				</li>
				<li><a
					href="${createLink(action:'observations', id:params.id)}">Observations</a>
				</li>
				<li><a href="${createLink(action:'members', id:params.id)}">Members</a>
				</li>

				<!-- li><a href="${createLink(action:'species', id:params.id)}">Species</a>
				</li>
				<li><a href="${createLink(action:'maps', id:params.id)}">Maps</a>
				</li-->
				<li><a href="${createLink(action:'pages', id:params.id)}">Pages</a>
				</li>
				<li><a href="${createLink(action:'aboutUs', id:params.id)}">About
						Us</a>
				</li>
				<sec:permitted className='species.groups.UserGroup'
					id='${userGroupInstance.id}'
					permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

					<li><a href="${createLink(action:'settings', id:params.id)}">Settings</a>
					</li>
				</sec:permitted>
			</ul>
		</div>
	</div>


	<div id="actionsHeader" style="position: relative; overflow: visible;">
		<uGroup:showActionsHeaderTemplate model="['userGroupInstance':userGroupInstance]"/>
	</div>

	<g:if test="${flash.error}">
		<div class="alertMsg alert alert-error" style="clear:both;">
			${flash.error}
		</div>
	</g:if>
	
	<div class="alertMsg ${(flash.message)?'alert':'' }" style="clear:both;">
		${flash.message}
	</div>

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
	

</r:script>