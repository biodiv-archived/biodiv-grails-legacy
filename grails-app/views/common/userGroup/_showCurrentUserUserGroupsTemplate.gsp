
<% def userGroups = userGroups.groupBy{it.key.allowObvCrossPosting};
def exclusiveUsergroups =  userGroups[false]
def otherUsergroups =  userGroups[true] 
def obvActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : ''
List userGroupsList = params.userGroupsList?params.userGroupsList.split(','):[]
def parentGroupId = ''
%>
<input type="hidden" class="userGroupsList" name="userGroupsList" value="${params.userGroupsList}" />
<g:if test="${exclusiveUsergroups}">
	<div>Share with either of these groups</div>
	<div class="groupsWithSharingNotAllowed btn-group userGroups"
		data-toggle="buttons-radio">
		
		<br />
		<ul class="thumbnails clearfix">
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
                        
                } else {
                    if(userGroupsList.contains(String.valueOf(userGroup.key.id)))
			checked = true
                }
		
		if( params.webaddress) {
			boolean isParentGroup =  (params.webaddress == userGroup.key.webaddress)
			checked = checked || isParentGroup
			parentGroupId = isParentGroup ? userGroup.key.id :""
			
		}
	 %>  <label class="radio">
						<button type="button"
							class="btn input-prepend ${checked?'active btn-success ' + obvActionMarkerClass + ' ' + parentGroupId :''} single-post"
							value="${userGroup.key.id}"
							style="padding: 0px; height: 42px; border-radius: 6px;">


							<span class="add-on" style="height: 32px; margin-right: -5px;">
								<i class="icon-ok signature ${checked? 'icon-black':'icon-white'}"></i> </span>

							<div style="display: inline-block">
								<uGroup:showUserGroupSignature
									model="[ 'userGroup':userGroup.key]" />
							</div>

						</button> </label>
			</g:each>
		</ul>
	</div>

</g:if>

<g:if test="${exclusiveUsergroups && otherUsergroups}">
	<div>or with any of these groups</div>
</g:if>
<div class="groupsWithSharingAllowed btn-group userGroups"
	data-toggle="buttons-checkbox">
	<ul class="thumbnails" style="clear: both;">
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
		} else {
                    if(userGroupsList.contains(String.valueOf(userGroup.key.id)))
			checked = true
                }

		
		if( params.webaddress) {
			boolean isParentGroup =  (params.webaddress == userGroup.key.webaddress)
			checked = checked || isParentGroup
			parentGroupId = isParentGroup ? userGroup.key.id :""
		}
	 %> <label class="checkbox">
					<button type="button"
						class="btn input-prepend ${checked? 'active btn-success ' + obvActionMarkerClass + ' ' + parentGroupId :''} multi-post"
						value="${userGroup.key.id}"
						style="padding: 0px; height: 42px; border-radius: 6px;">


						<span class="add-on" style="height: 32px; margin-right: -5px;">
							<i class="icon-ok signature ${checked? 'icon-black':'icon-white'}"></i> </span>

						<div style="display: inline-block">

							<uGroup:showUserGroupSignature
								model="[ 'userGroup':userGroup.key]" />
						</div>
					</button> </label>
		</g:each>
	</ul>
</div>
<div class="modal hide" id="userGroupSelectionModal" tabindex='-1'>
	<div class="modal-body"></div>

</div>
<g:javascript>
//TODO: g:javascript because it is being loaded in ajax way ... needs to change 
$(document).ready (function(){
    selectTickUserGroupsSignature("${parentGroupId}");
});
</g:javascript>
