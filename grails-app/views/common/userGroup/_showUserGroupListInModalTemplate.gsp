<div class="modal hide" id="selectedGroupList">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">×</button>
		<h3>Please join one of the group</h3>
	</div>
	<div class="modal-body">
		<uGroup:showUserGroupsList
			model="['userGroupInstanceList':userGroupInstanceList, instanceTotal:0]" />
	</div>
	<div class="modal-footer">
		<a href="#" class="btn" data-dismiss="modal">Close</a>
	</div>
</div>