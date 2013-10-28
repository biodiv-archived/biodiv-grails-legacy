
<% def userGroups = userGroups.groupBy{it.key.allowObvCrossPosting};
def exclusiveUsergroups =  userGroups[false]
def otherUsergroups =  userGroups[true] 
def obvActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : ''
List userGroupsList = params.userGroupsList?params.userGroupsList.split(','):[]
def parentGroupId = ''
%>
<input type="hidden" id="userGroupsList" name="userGroupsList" value="${params.userGroupsList}" />
<g:if test="${exclusiveUsergroups}">
	<div>Share with either of these groups</div>
	<div id="groupsWithSharingNotAllowed" class="btn-group userGroups"
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
<div id="groupsWithSharingAllowed" class="btn-group userGroups"
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
						style="padding: 0px; height: 52px; border-radius: 6px;">


						<span class="add-on" style="height: 40px; margin-right: -5px;">
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
<r:script>
$(document).ready (function(){

	$(".userGroups button").click(function(e){
		if($(this).hasClass('active')) {
			//trying to unselect group
			
			//if on obv create page	and one group is coming as parent group		
			if($("#userGroups").hasClass('create') && ($("#userGroups button.create").length > 0)){
				//this group is parent group
				if($(this).hasClass('create') && ${parentGroupId != ''} && $(this).hasClass('${parentGroupId}')){
					alert("Can't unselect parent group");
				}else{
					//un selecting other group
					$(this).removeClass('btn-success');
					$(this).find(".icon-ok").removeClass("icon-black").addClass("icon-white");
				}	
			}else{
				$(this).removeClass('btn-success');
				$(this).find(".icon-ok").removeClass("icon-black").addClass("icon-white");
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
					$(this).find(".icon-ok").removeClass("icon-white").addClass("icon-black");
				}
			}else{
				//on obv edit page
				if($(this).hasClass("single-post")) {
					$("#groupsWithSharingAllowed button.multi-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
					$("#groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
					$(this).removeClass('disabled').addClass('btn-success');
				} else {
					$("#groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
					$(this).removeClass('disabled').addClass('btn-success');
				}
				$(this).find(".icon-ok").removeClass("icon-white").addClass("icon-black");
			}
		}
		e.preventDefault();
	});
	
});
</r:script>
