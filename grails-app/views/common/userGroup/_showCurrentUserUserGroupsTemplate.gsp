
<% def userGroups = userGroups.groupBy{it.key.allowObvCrossPosting};
def exclusiveUsergroups =  userGroups[false]
def otherUsergroups =  userGroups[true] %>
<input type="hidden" id="userGroupsList" name="userGroupsList" value="" />
<g:if test="${exclusiveUsergroups}">
	<div id="groupsWithSharingNotAllowed" class="btn-group userGroups"
		data-toggle="buttons-radio">
		Share with either of these groups
		<ul class="thumbnails" style="clear:both;">
			<g:each in="${exclusiveUsergroups}" var="userGroup" status="i">

				<li class="thumbnail">
					<%
		boolean checked = userGroup.value;
		if(params.userGroup) {
			if(params.userGroup instanceof String) {
				checked = checked || (params.userGroup == String.valueOf(userGroup.key.id));
			} else {
				checked = checked || params.userGroup.containsValue(String.valueOf(userGroup.key.id))
			}
		}
	 %> <label class="radio">
						<button type="button" class="btn ${checked?'active':''} single-post" value="${userGroup.key.id}">
							<img class="logo" src="${userGroup.key.icon().fileName}"
								title="${userGroup.key.name}" alt="${userGroup.key.name}" />
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
		if(params.userGroup) {
			if(params.userGroup instanceof String) {
				checked = checked || (params.userGroup == String.valueOf(userGroup.key.id));
			} else {
				checked = checked || params.userGroup.containsValue(String.valueOf(userGroup.key.id))
			}
		}
	 %> <label class="checkbox">
					<button type="button" class="btn ${checked?'active':''} multi-post"  value="${userGroup.key.id}">
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
			if($(this).hasClass("single-post")) {
				$("#groupsWithSharingAllowed button").removeClass('disabled')
			} else {
				if($("#groupsWithSharingAllowed button.active").length == 0) {
					$("#groupsWithSharingAllowed button").removeClass('disabled')
				}
			}
			
		} else {
			if($(this).hasClass("single-post")) {
				$("#groupsWithSharingAllowed button").addClass('disabled')
				$("#groupsWithSharingAllowed input").prop('checked', false)
				$("#groupsWithSharingNotAllowed button").addClass('disabled');
				$(this).removeClass('disabled');
			} else {
				$("#groupsWithSharingNotAllowed button").addClass('disabled')
				$("#groupsWithSharingNotAllowed input").prop('checked', false)
			}
		}
		e.preventDefault();
	});
	
});
</r:script>