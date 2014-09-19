<div class="modal hide fade in" id="selectedGroupList" style="width:auto;" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h3><g:message code="showusergruplistmodtemp.please.join.one.group" /></h3>
	</div>
	<div class="modal-body">
		<uGroup:showUserGroupsList
			model="['userGroupInstanceList':userGroupInstanceList, instanceTotal:0]" />
	</div>
	<div class="modal-footer">
		<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="button.close" /></a>
	</div>
</div>
