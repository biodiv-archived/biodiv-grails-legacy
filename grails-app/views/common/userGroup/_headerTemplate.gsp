<div class="header">
	<div class="top_nav_bar navbar">
		<div class="container">
			<!-- Logo -->
			<div class="span3">
				<a href="${createLink(action:"show", id:userGroupInstance.id)}">
					<img class="logo" alt="${userGroupInstance.name}"
					src="${createLink(url: userGroupInstance.mainImage()?.fileName)}">
				</a>
			</div>
			<!-- Logo ends -->
			<!-- h1 class="span8">
							${userGroupInstance.name}
			</h1-->
			<ul class="nav">
				<li><a href="${createLink(action:'show', id:params.id)}">Home</a>
				</li>
				<li><a
					href="${createLink(action:'observations', id:params.id)}">Observations</a>
				</li>
				<li><a href="${createLink(action:'members', id:params.id)}">Members</a>
				</li>

				<li><a href="${createLink(action:'species', id:params.id)}">Species</a>
				</li>
				<li><a href="${createLink(action:'maps', id:params.id)}">Maps</a>
				</li>
				<li><a href="${createLink(action:'pages', id:params.id)}">Pages</a>
				</li>
				<li><a href="${createLink(action:'aboutUs', id:params.id)}">About
						Us</a>
				</li>
				<sec:permitted className='species.groups.UserGroup'
					id='${userGroupInstance.id}'
					permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

					<li><a href="${createLink(action:'settings', id:params.id)}">Settings</a>
					</li>
				</sec:permitted>
			</ul>
		</div>
	</div>


	<div style="position: relative; overflow: visible;">

		<div class="observation-icons pull-right">

			<!-- obv:identificationByEmail
				model="['source':'userGroupInvite', 'requestObject':request, 'activity':'Invite Friends', 'cssClass':'btn btn-large btn-success dropdown-toggle']" /-->

			<sec:permitted className='species.groups.UserGroup'
				id='${userGroupInstance.id}'
				permission='${org.springframework.security.acls.domain.BasePermission.WRITE}'>

				<a id="inviteMembers" class="btn btn-large btn-primary" href="#"><i
					class="icon-envelope"></i> <g:message code="userGroup.members.label"
						default="Invite Members" /> </a>
			</sec:permitted>
			
			<div class="modal hide" id="inviteMembersDialog">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Invite friends as members</h3>
				</div>
				<div class="modal-body">
					<p>Send an invitation to invite your friends to join and
						contribute in this interesting group…</p>
					<div>
						<div id="invite_memberMsg">
							
						</div>
						<form id="inviteMembersForm" method="post"
							style="background-color: #F2F2F2;">
							<sUser:selectUsers model="['id':members_autofillUsersId]" />
							<input type="hidden" name="memberUserIds" id="memberUserIds" />
						</form>
					</div>
				</div>
				<div class="modal-footer">
					<a href="#" class="btn" data-dismiss="modal">Close</a> <a href="#"
						id="invite" class="btn btn-primary">Invite</a>
				</div>
			</div>



			<uGroup:isNotAMember model="['userGroupInstance':userGroupInstance]">

				<g:if test="${userGroupInstance.allowUsersToJoin}">
					<a class="btn btn-large btn-success" id="joinUs"> <i
						class="icon-plus"></i> Join Us</a>
				</g:if>
				<g:else>
					<a class="btn btn-large btn-success" id="requestMembership"> <i
						class="icon-plus"></i> Request Membership</a>
				</g:else>


			</uGroup:isNotAMember>
			<uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
				<a class="btn btn-large btn-primary" id="leaveUs"><i
					class="icon-minus"></i>Leave this group</a>
			</uGroup:isAMember>

		</div>
	</div>

	<div class="msg ${(flash.message)?'alert':'' }" style="clear:both;">
		${flash.message}
	</div>


</div>

<r:script>
$(document).ready(function(){
	$("#joinUs").click(function() {
		$.ajax({
        	url: "${createLink(action:'joinUs',id:userGroupInstance.id) }",
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.success) {
            		$("#joinUs").html("Joined").removeClass("btn-success").addClass("disabled");
            		$(".msg").removeClass('alert-error').addClass('alert-success').html(data.msg);
            	} else {
            		$("#requestMembership").html("Error sending request").removeClass("btn-success").addClass("disabled");
            		$(".msg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            	}
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, undefined, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".msg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
				});
            }
		});
	})
	
	$("#requestMembership").click(function() {
		$.ajax({
        	url: "${createLink(action:'requestMembership',id:userGroupInstance.id) }",
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.success) {
            		$("#requestMembership").html("Sent Request").removeClass("btn-success").addClass("disabled");
            		$(".msg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            	} else {
            		$("#requestMembership").html("Error sending request").removeClass("btn-success").addClass("disabled");
            		$(".msg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            	}
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, undefined, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".msg").html(msg.msg).removeClass('alert alert-success').addClass('alert alert-error');
				});
            }
		});
	})
	
	$("#leaveUs").click(function() {
		$.ajax({
        	url: "${createLink(action:'leaveUs',id:userGroupInstance.id) }",
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.success) {
            		$("#leaveUs").html("Thank You").removeClass("btn-info").addClass("disabled");
            		$(".msg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            	} else {
            		$("#requestMembership").html("Couldn't Leave").removeClass("btn-success").addClass("disabled");
            		$(".msg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            	}
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, undefined, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".msg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
				});
            }
		});
	})
	
	var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	$("#invite").click(function(){
		$('#memberUserIds').val(members_autofillUsersComp[0].getEmailAndIdsList().join(","));
		$('#inviteMembersForm').ajaxSubmit({ 
			url:'${createLink(action:'inviteMembers',id:userGroupInstance.id)}',
			dataType: 'json', 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			
			success: function(data, statusText, xhr, form) {
				if(data.success) {
					$('#inviteMembersDialog').modal('hide');
					$(".msg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				} else {
					$("#invite_memberMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				}				
			}, error:function (xhr, ajaxOptions, thrownError){
					//successHandler is used when ajax login succedes
	            	var successHandler = this.success, errorHandler;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
						var response = $.parseJSON(xhr.responseText);
						
					});
           } 
     	});	
	});
     	
   	$('#inviteMembersDialog').modal({
		"show" : false,
		"backdrop" : "static"
	});
		
	$('#inviteMembers').click(function(){
			$.ajax({ 
	         	url:"${createLink(controller:'SUser', action:'isLoggedIn')}",
				success: function(data, statusText, xhr, form) {
					if(data === "true"){
						$('#memberUserIds').val('');
						$('#inviteMembersDialog').modal('show');
						return false;
					}else{
						window.location.href = "${createLink(controller:'login')}?spring-security-redirect="+window.location.href;
					}
	            },
	            error:function (xhr, ajaxOptions, thrownError){
	            	return false;
				} 
	     	});
	});
		
});
</r:script>