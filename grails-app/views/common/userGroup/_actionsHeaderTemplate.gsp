<div class="pull-right" style="position: absolute;top:0px;right: 0px;">
	<g:if test="${canEditUserGroup}">
		<uGroup:inviteExpert model="['userGroupInstance':userGroupInstance, 'isExpertOrFounder':isExpertOrFounder, 'experts_autofillUsersId':experts_autofillUsersId]"/>
					
		<a id="inviteMembers" class="btn btn-primary" href="#inviteMembersDialog" role="button" data-toggle="modal"><i
			class="icon-envelope"></i> <g:message code="userGroup.members.label"
				default="${g.message(code:'title.default.invite.friends')}" /> </a>
		<div class="modal hide fade" id="inviteMembersDialog" tabindex='-1'
			role="dialog" aria-labelledby="inviteMembersModalLabel"
			aria-hidden="true">
			<div class="modal-header">
			    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
				<h3 id="inviteMembersModalLabel"><g:message code="actionheadertemplate.invite.friends" /> </h3>
			</div>
			<div class="modal-body">
				<p><g:message code="text.send.invitation" /></p>
				<div>
					<div id="invite_memberMsg"></div>
					<form id="inviteMembersForm" method="post"
						style="background-color: #F2F2F2;">
						<sUser:selectUsers model="['id':members_autofillUsersId]" />
						<input type="hidden" name="memberUserIds" id="memberUserIds" />
						<textarea id="inviteMemberMsg" class="comment-textbox" placeholder="Please write a note to invite member."></textarea>
					</form>
				</div>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
				<a href="#" id="inviteMemberButton" class="btn btn-primary"><g:message code="button.invite" /></a>
			</div>
		</div>
		
		
	</g:if>
	<uGroup:joinLeaveGroupTemplate model="['showLeave':false, 'showJoin':true, 'userGroupInstance':userGroupInstance]" />
	<div class="modal hide fade in" id="leaveUsModalDialog" tabindex="-1" role="dialog" aria-labelledby="leaveUsModalDialogLabel" >
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			<h3 id="leaveUsModalDialogLabel"><g:message code="actionheadertemplate.want.to.leave" /></h3>
		</div>
		<div class="modal-body">
			<p><g:message code="text.feedback" /></p>
		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal"><g:message code="button.close" /></a> <a href="#"
				id="leave" class="btn btn-primary"
				data-group-id="${userGroupInstance.id}"><g:message code="button.leave" /></a>
		</div>
	</div>
</div>
<script type="text/javascript">
//IMP:Header is loaded in drupal pages as well. Any code in this block is not run when loaded by ajax
	//So please don't put any code here. Put it in init_header function in membership.js
</script>
