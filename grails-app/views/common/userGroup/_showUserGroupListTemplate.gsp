
<div class="observations_list observation" style="clear: both;">
	<div class="btn-group button-bar" data-toggle="buttons-radio"
		style="float: right;visibility:hidden;">
		<button class="list_view_bttn btn list_style_button active">
			<i class="icon-align-justify"></i>
		</button>
	</div>
	<div>
		<div>
			<%
				def userGroupPos = (queryParams?.offset != null) ? queryParams.offset : (params?.offset != null) ? params.offset : 0
			%>
			
			<table class="table table-hover span8" style="margin-left: 0px;">
				<thead>
					<tr>
						<th style="width:20%">Group</th>
						<th style="width:20%">Name</th>
						<th>Species Groups</th>
						<th>Habitats</th>
						<th>Members</th>
						<th style="width:20%">Join</th>
					</tr>
				</thead>
				<tbody class="mainContentList">
					<g:each in="${userGroupInstanceList}" status="i"
						var="userGroupInstance">
						<tr class="mainContent">
							<uGroup:showSnippet
								model="['userGroupInstance':userGroupInstance, 'userGroupTitle':userGroupTitleList?.get(i), 'pos':userGroupPos+i, 'showLeave':true]"></uGroup:showSnippet>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>

	<g:if test="${instanceTotal > (queryParams?.max?:0)}">
		<div class="centered" style="clear: both;">
			<div class="btn loadMore">
				<span class="progress" style="display: none;">Loading ... </span> <span
					class="buttonTitle">Load more</span>
			</div>
		</div>
	</g:if>

	<%activeFilters?.loadMore = true %>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<g:paginate total="${instanceTotal}" max="${queryParams?.max}"
			action="${params.action}" params="${activeFilters}" />
	</div>
	
	
	<div class="modal hide" id="leaveUsModalDialog" tabindex='-1'>
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Do you want to leave this group???</h3>
				</div>
				<div class="modal-body">
					<p>We would like to know your feedback and any ideas on making
						this group a more interesting and a happening place. We are thankful
						for your wonderful contribution to this group and would like to hear
						from you soon.</p>
				</div>
				<div class="modal-footer">
					<a href="#" class="btn" data-dismiss="modal">Close</a> <a href="#"
						id="leave" class="btn btn-primary" data-group-id="">Leave</a>
				</div>
			</div>
</div>

<style>
thead th {
	background: rgba(106, 201, 162, 0.2);
}

.table tbody tr:hover td,.table tbody tr:hover th {
	background-color: #FEFAD5;
}

.table tbody tr td,.table tbody tr th {
	background-color: white;
}
</style>
<g:javascript>
$(document).ready(function(){
	
	$(".joinUs").unbind('click').on('click', function() {
		console.log('livejoin ');
		var joinUsUrl =  "${uGroup.createLink(controller:'userGroup', action:'joinUs') }";
		joinUsUrl = joinUsUrl + "/?id=" + $(this).attr('data-group-id') //+"/joinUs";
		joinAction($(this), joinUsUrl);
	});
	
	$(".requestMembership").unbind('click').on('click', function() {
		var requestMembershipUrl = "${uGroup.createLink(controller:'userGroup', action:'requestMembership') }";
		requestMembershipUrl = requestMembershipUrl+"/?id="+$(this).attr('data-group-id')//+"/requestMembership";
		requestMembershipAction($(this), requestMembershipUrl);
	});
	
	$(".leaveUs").unbind('click').on('click', function() {
		var leaveUrl = "${uGroup.createLink(controller:'userGroup', action:'leaveUs') }";
		leaveUrl = leaveUrl //+"/"+$(this).attr('data-group-id')+"/leaveUs";
		$("#leave").attr('data-group-id', $(this).attr('data-group-id'))
		$("#leave").attr('data-leaveUrl', leaveUrl)
		$('#leaveUsModalDialog').modal('show');
	});
});
</g:javascript>
