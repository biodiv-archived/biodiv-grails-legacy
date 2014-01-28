<div class="modal hide fade in" id="selectedGroupList" style="width:auto;" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h3>Please join one of the groups</h3>
	</div>
	<div class="modal-body">
		<uGroup:showUserGroupsList
			model="['userGroupInstanceList':userGroupInstanceList, instanceTotal:0]" />
	</div>
	<div class="modal-footer">
		<a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
	</div>
</div>