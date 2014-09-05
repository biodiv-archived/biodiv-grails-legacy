<g:if test="${isExpertOrFounder}">
	<a id="inviteExperts" class="btn btn-primary" href="#inviteExpertsDialog" role="button" data-toggle="modal"><i
		class="icon-envelope"></i> <g:message code="userGroup.moderators.label"
			default="${g.message(code:'title.default.invite.moderators')}" /> </a>
	<div class="modal hide fade" id="inviteExpertsDialog" tabindex='-1'
		role="dialog" aria-labelledby="inviteExpertsModalLabel"
		aria-hidden="true">
		<div class="modal-header">
		    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			<h3 id="inviteExpertsModalLabel"><g:message code="inviteexperttemplate.invite.moderators" /> </h3>
		</div>
		<div class="modal-body">
			<p><g:message code="text.invite.to.join.moderate" /></p>
			<div>
				<div id="invite_expertMsg"></div>
				<form id="inviteExpertsForm" method="post"
					style="background-color: #F2F2F2;">
					<sUser:selectUsers model="['id':experts_autofillUsersId]" />
					<input type="hidden" name="expertUserIds" id="expertUserIds" />
					<textarea id="inviteModeratorMsg" class="comment-textbox" placeholder="${g.message(code:'ugroup.note.invite')}"></textarea>
				</form>
			</div>
		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
			<a href="#" id="inviteExpertButton" class="btn btn-primary"><g:message code="button.invite" /></a>
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
			<h3 id="requestModeratorModalLabel"><g:message code="inviteexperttemplate.become.moderator" /></h3>
		</div>
		<div class="modal-body">
			<p><g:message code="text.moderator.works" /></p>
			<div>
				<div id="requset_moderatorMsg"></div>
				<form id="requestModeratorForm" method="post"
					style="background-color: #F2F2F2;">
					<textarea id="requestModeratorMsg" class="comment-textbox" placeholder="${g.message(code:'ugroup.note.suitable')}"></textarea>
				</form>
			</div>
		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
			<a href="#" id="requestModeratorButton" class="btn btn-primary"><g:message code="button.send" /></a>
		</div>
	</div>
</g:else>
