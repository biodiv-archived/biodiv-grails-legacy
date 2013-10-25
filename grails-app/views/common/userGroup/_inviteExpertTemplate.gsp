<g:if test="${isExpertOrFounder}">
	<a id="inviteExperts" class="btn btn-primary" href="#inviteExpertsDialog" role="button" data-toggle="modal"><i
		class="icon-envelope"></i> <g:message code="userGroup.moderators.label"
			default="Invite Moderators" /> </a>
	<div class="modal hide fade" id="inviteExpertsDialog" tabindex='-1'
		role="dialog" aria-labelledby="inviteExpertsModalLabel"
		aria-hidden="true">
		<div class="modal-header">
		    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			<h3 id="inviteExpertsModalLabel">Invite moderators</h3>
		</div>
		<div class="modal-body">
			<p>Send an invitation to join and moderate this group</p>
			<div>
				<div id="invite_expertMsg"></div>
				<form id="inviteExpertsForm" method="post"
					style="background-color: #F2F2F2;">
					<sUser:selectUsers model="['id':experts_autofillUsersId]" />
					<input type="hidden" name="expertUserIds" id="expertUserIds" />
					<textarea id="inviteModeratorMsg" class="comment-textbox" placeholder="Please write a note to invite moderator."></textarea>
				</form>
			</div>
		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
			<a href="#" id="inviteExpertButton" class="btn btn-primary">Invite</a>
		</div>
	</div>		
</g:if>

<g:else>
	<a id="requestModerator" class="btn btn-primary" href="#requestModeratorDialog" role="button" data-toggle="modal"><i
		class="icon-envelope"></i> <g:message code="userGroup.requestmoderator.label"
			default="Become Moderator" /> </a>
	<div class="modal hide fade" id="requestModeratorDialog" tabindex='-1'
		role="dialog" aria-labelledby="requestModeratorModalLabel"
		aria-hidden="true">
		<div class="modal-header">
		    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			<h3 id="requestModeratorModalLabel">Become moderator</h3>
		</div>
		<div class="modal-body">
			<p>As moderator, you can be involved with featuring observations, documents and species pages within a group. You will have rights to add or remove resources in bulk, receive mails for all activity, and invite other moderators to the group.</p>
			<div>
				<div id="requset_moderatorMsg"></div>
				<form id="requestModeratorForm" method="post"
					style="background-color: #F2F2F2;">
					<textarea id="requestModeratorMsg" class="comment-textbox" placeholder="Please mention why you will be a suitable moderator for this group."></textarea>
				</form>
			</div>
		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
			<a href="#" id="requestModeratorButton" class="btn btn-primary">Send</a>
		</div>
	</div>
</g:else>