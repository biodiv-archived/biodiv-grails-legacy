
	<div>

		<div class="observation-icons pull-right">
			<sec:permitted className='species.groups.UserGroup'
								id='${userGroupInstance.id}'
								permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

								<a class="btn btn-large btn-primary "
									href="${createLink(action:'edit', id:userGroupInstance.id)}">
									<i
					class="icon-edit"></i>Edit Group </a>
								
			</sec:permitted>
							
			<sec:permitted className='species.groups.UserGroup'
				id='${userGroupInstance.id}'
				permission='${org.springframework.security.acls.domain.BasePermission.WRITE}'>

				<a id="inviteMembers" class="btn btn-large btn-primary" href="#"><i
					class="icon-envelope"></i> <g:message code="userGroup.members.label"
						default="Invite Members" /> </a>
			</sec:permitted>
			
			<div class="modal hide" id="inviteMembersDialog">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Invite friends as members</h3>
				</div>
				<div class="modal-body">
					<p>Send an invitation to invite your friends to join and
						contribute in this interesting group…</p>
					<div>
						<div id="invite_memberMsg">
							
						</div>
						<form id="inviteMembersForm" method="post"
							style="background-color: #F2F2F2;">
							<sUser:selectUsers model="['id':members_autofillUsersId]" />
							<input type="hidden" name="memberUserIds" id="memberUserIds" />
						</form>
					</div>
				</div>
				<div class="modal-footer">
					<a href="#" class="btn" data-dismiss="modal">Close</a> <a href="#"
						id="invite" class="btn btn-primary">Invite</a>
				</div>
			</div>



			<uGroup:isNotAMember model="['userGroupInstance':userGroupInstance]">

				<g:if test="${userGroupInstance.allowUsersToJoin}">
					<a class="btn btn-large btn-success" id="joinUs"> <i
						class="icon-plus"></i> Join Us</a>
				</g:if>
				<g:else>
					<a class="btn btn-large btn-success" id="requestMembership"> <i
						class="icon-plus"></i> Request Membership</a>
				</g:else>


			</uGroup:isNotAMember>
			<uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
				<a class="btn btn-large btn-primary" id="leaveUs"><i
					class="icon-minus"></i>Leave this group</a>
				<div class="modal hide" id="leaveUsModalDialog">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">×</button>
						<h3>Do you want to leave this group???</h3>
					</div>
					<div class="modal-body">
						<p>
						We would like to know your feedback and any ideas on making this group a more interesting and a happening place.
						We are thankful for your wonderful contribution to this group and would like to hear from you soon.</p>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn" data-dismiss="modal">Close</a> <a href="#"
							id="leave" class="btn btn-primary">Leave</a>
					</div>
				</div>
			</uGroup:isAMember>

		</div>
	</div>
