
<% def userGroups = userGroups.groupBy{it.key.allowObvCrossPosting};
def exclusiveUsergroups =  userGroups[false]
def otherUsergroups =  userGroups[true] %>

<g:if test="${exclusiveUsergroups}">
	<div id="groupsWithSharingNotAllowed" class="btn-group" data-toggle="buttons-radio">
		Share with either of these groups
		<g:each in="${exclusiveUsergroups}" var="userGroup" status="i">

			<li class="thumbnail"><%
		boolean checked = userGroup.value;
		if(params.userGroup) {
			if(params.userGroup instanceof String) {
				checked = checked || (params.userGroup == String.valueOf(userGroup.key.id));
			} else {
				checked = checked || params.userGroup.containsValue(String.valueOf(userGroup.key.id))
			}
		}
	 %> <label class="radio"><input type="radio" style="margin-left: 0px;" name="groupsWithSharingNotAllowed"
					value="${userGroup.key.id}" ${checked?'checked':''} />  ${userGroup.key.name}
					<!-- img class="logo" src="${userGroup.key.icon().fileName}" title="${userGroup.key.name}" alt="${userGroup.key.name}"-->
			</label>
		</g:each>
	</div>
</g:if>

<g:if test="${exclusiveUsergroups && otherUsergroups}">
or with any of these groups
</g:if>
<div id="groupsWithSharingAllowed" class="btn-group" data-toggle="buttons-checkbox">
	<g:each in="${otherUsergroups}" var="userGroup" status="i">

		<li><%
		boolean checked = userGroup.value;
		if(params.userGroup) {
			if(params.userGroup instanceof String) {
				checked = checked || (params.userGroup == String.valueOf(userGroup.key.id));
			} else {
				checked = checked || params.userGroup.containsValue(String.valueOf(userGroup.key.id))
			}
		}
	 %> <label class="checkbox"><input type="checkbox" style="margin-left: 0px;" name="userGroup.${i}"
				value="${userGroup.key.id}" ${checked?'checked':''} />  ${userGroup.key.name}
		</label>
	</g:each>
</div>
<div class="modal hide" id="userGroupSelectionModal">
  <div class="modal-body">
  </div>
  
</div>