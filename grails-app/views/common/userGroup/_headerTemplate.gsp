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

			<obv:identificationByEmail
				model="['source':'userGroupInvite', 'requestObject':request, 'activity':'Invite Friends', 'cssClass':'btn btn-large btn-success dropdown-toggle']" />

			<span class="pull-right"> <uGroup:isNotAMember
					model="['userGroupInstance':userGroupInstance]">
					<a class="btn btn-large btn-success" id="joinUs"> <i
						class="icon-plus"></i> Join Us</a>
				</uGroup:isNotAMember> <uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
					<a class="btn btn-large btn-primary" id="leaveUs"><i
						class="icon-minus"></i>Leave this group</a>
				</uGroup:isAMember> </span>


			<sec:permitted className='species.groups.UserGroup'
				id='${userGroupInstance.id}'
				permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

				<a class="btn btn-large btn-primary "
					href="${createLink(action:'edit', id:userGroupInstance.id)}">
					Edit Group </a>

				<a class="btn btn-large btn-danger"
					href="${createLink(action:'flagDeleted', id:userGroupInstance.id)}"
					onclick="return confirm('${message(code: 'default.observation.delete.confirm.message', default: 'This group will be deleted. Are you sure ?')}');">Delete
					Group </a>
			</sec:permitted>
		</div>
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
            	$("#joinUs").html("Joined").removeClass("btn-success").addClass("btn-disabled");
            	$(".message").removeClass('alert-error').addClass('alert-success').html(data.msg);
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, undefined, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".message").html(msg.msg).removeClass('alert-success').addClass('alert-error');
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
            	$("#leaveUs").html("Thank You").removeClass("btn-info").addClass("btn-disabled");
            	$(".message").removeClass('alert-error').addClass('alert-success').html(data.msg);
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, undefined, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".message").html(msg.msg).removeClass('alert-success').addClass('alert-error');
				});
            }
		});
	})
});
</r:script>