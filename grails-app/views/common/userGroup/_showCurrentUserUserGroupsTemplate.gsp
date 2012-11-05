<% def userGroups = userGroups.groupBy{it.key.allowObvCrossPosting};
def exclusiveUsergroups =  userGroups[false]
def otherUsergroups =  userGroups[true] 
def obvActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : ''
%>
<input type="hidden" id="userGroupsList" name="userGroupsList" value="" />
<g:if test="${exclusiveUsergroups}">
	<div id="groupsWithSharingNotAllowed" class="btn-group userGroups"
		data-toggle="buttons-radio">
		Share with either of these groups
		<ul class="thumbnails" style="clear:both;">
			<g:each in="${exclusiveUsergroups}" var="userGroup" status="i">

				<li>
					<%
		boolean checked = userGroup.value;
		if(params.userGroupId) {
			if(params.userGroupId instanceof String) {
				checked = checked || (params.userGroupId == String.valueOf(userGroup.key.id));
			} else {
				checked = checked || params.userGroupId.containsValue(String.valueOf(userGroup.key.id))
			}
		}
	 %>
	 <label class="radio">
						<button type="button" class="btn ${checked?'active btn-success ' + obvActionMarkerClass :''} single-post" value="${userGroup.key.id}">
							<uGroup:showUserGroupSignature  model="[ 'userGroup':userGroup.key]" />
						</button> </label>
			</g:each>
		</ul>
	</div>

</g:if>

<g:if test="${exclusiveUsergroups && otherUsergroups}">
or with any of these groups
</g:if>
<div id="groupsWithSharingAllowed" class="btn-group userGroups"
	data-toggle="buttons-checkbox">
	<ul class="thumbnails"  style="clear:both;">
		<g:each in="${otherUsergroups}" var="userGroup" status="i">

			<li>
				<%
		boolean checked = userGroup.value;
		if(params.userGroupId) {
			if(params.userGroupId instanceof String) {
				checked = checked || (params.userGroupId == String.valueOf(userGroup.key.id));
			} else {
				checked = checked || params.userGroupId.containsValue(String.valueOf(userGroup.key.id))
			}
		}
	 %> 
	 <label class="checkbox">
					<button type="button" class="btn ${checked? 'active btn-success ' + obvActionMarkerClass :''} multi-post"  value="${userGroup.key.id}">
						<img class="logo" src="${userGroup.key.icon().fileName}"
							title="${userGroup.key.name}" alt="${userGroup.key.name}" />
					</button> </label>
		</g:each>
	</ul>
</div>
<div class="modal hide" id="userGroupSelectionModal">
	<div class="modal-body"></div>

</div>
<r:script>
$(document).ready (function(){

	$(".userGroups button").click(function(e){
		if($(this).hasClass('active')) {
			//trying to unselect group
			
			//if on obv create page	and one group is coming as parent group		
			if($("#userGroups").hasClass('create') && ($("#userGroups button.create").length > 0)){
				//this group is parent group
				if($(this).hasClass('create')){
					alert("Can't unselect parent group");
				}else{
					//un selecting other group
					$(this).removeClass('btn-success');
				}	
			}else{
				$(this).removeClass('btn-success');
				if($(this).hasClass("single-post")) {
					$("#groupsWithSharingNotAllowed button.single-post").removeClass('disabled')
					$("#groupsWithSharingAllowed button.multi-post").removeClass('disabled')
				} else {
					if($("#groupsWithSharingAllowed button.active").length == 0) {
						$("#groupsWithSharingAllowed button.multi-post").removeClass('disabled')
					}
				}
			}
		} else {
			//trying to select new group
			
			//if on obv create page and one group is coming as parent group
			if($("#userGroups").hasClass('create') && ($("#userGroups button.create").length > 0)){
				//either current one belongs to exclusive group or parent group is exclusive group
			 	if($(this).hasClass("single-post") ||($("#groupsWithSharingNotAllowed button.create").length > 0)){
					alert("Can't select this group because it will unselect parent group");
				}else{
					//parent group is multipost one and this new group is also belong to multi select so selecting it
					$(this).removeClass('disabled').addClass('btn-success');
				}
			}else{
				//on obv edit page
				if($(this).hasClass("single-post")) {
					$("#groupsWithSharingAllowed button.multi-post").addClass('disabled').removeClass('active btn-success')
					$("#groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success');
					$(this).removeClass('disabled').addClass('btn-success');
				} else {
					$("#groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success')
					$(this).removeClass('disabled').addClass('btn-success');
				}
			}
		}
		e.preventDefault();
	});
	
});
</r:script>