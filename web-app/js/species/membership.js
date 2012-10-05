/**
 * 
 */
$(document).ready(function(){
	$(".joinUs").live('click', function() {
		if($(this).hasClass('disabled')) return false;
		var me = this;
		$.ajax({
        	url: window.joinUsUrl,
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.statusComplete) {
            		$(me).html("Joined").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert-error').addClass('alert-success').html(data.msg);
            	} else {
            		$(me).html("Error sending request").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            		//reloadActionsHeader();
            	}
            	document.location.reload(true);
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, this.success, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
				});
            }
		});
		return false;
	})
	
	$(".requestMembership").live('click', function() {
		if($(this).hasClass('disabled')) return false;
		var me = this;
		$.ajax({
        	url: window.requestMembershipUrl,
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.statusComplete) {
            		$(me).html("Sent Request").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            	} else {
            		$(me).html("Error sending request").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            		reloadActionsHeader();
            	}
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, this.success, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".alertMsg").html(msg.msg).removeClass('alert alert-success').addClass('alert alert-error');
				});
            }
		});
		return false;
	})
	
	$(".leaveUs").live('click', function() {
		if($(this).hasClass('disabled')) return false;
		$('#leaveUsModalDialog').modal('show');
	});
	
	$("#leave").click(function() {
		if($(this).hasClass('disabled')) return false;
		var dataGroupId = $(this).attr('data-group-id');
		var me = $(".leaveUs[data-group-id="+dataGroupId+"]");
		$.ajax({
        	url: window.leaveUrl,
            method: "POST",
            dataType: "json",
            success: function(data) {
            	if(data.statusComplete) {
            		$("me").html("Thank You").removeClass("btn-info").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            		//reloadMembers();
            	} else {
            		$("me").html("Couldn't Leave").removeClass("btn-success").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
            	}
            	$('#leaveUsModalDialog').modal('hide');
            	document.location.reload(true)
            }, error: function(xhr, status, error) {
				handleError(xhr, status, error, this.success, function() {
                	var msg = $.parseJSON(xhr.responseText);
                    $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
				});
            }
		});
	})
	
	
	$("#invite").click(function(){
		$('#memberUserIds').val(members_autofillUsersComp[0].getEmailAndIdsList().join(","));
		$('#inviteMembersForm').ajaxSubmit({ 
			url:window.inviteMembersFormUrl,
			dataType: 'json', 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			
			success: function(data, statusText, xhr, form) {
				if(data.statusComplete) {
					$('#inviteMembersDialog').modal('hide');
					$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				} else {
					$("#invite_memberMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				}				
			}, error:function (xhr, ajaxOptions, thrownError){
					//successHandler is used when ajax login succedes
	            	var successHandler = this.success;
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
	$('#leaveUsModalDialog').modal({
		"show" : false,
		"backdrop" : "static"
	});
		
	$('#inviteMembers').click(function(){
			$.ajax({ 
	         	url:window.isLoggedInUrl,
				success: function(data, statusText, xhr, form) {
					if(data === "true"){
						$('#memberUserIds').val('');
						$('#inviteMembersDialog').modal('show');
						return false;
					}else{
						window.location.href = window.loginUrl+"?spring-security-redirect="+window.location.href;
					}
	            },
	            error:function (xhr, ajaxOptions, thrownError){
	            	return false;
				} 
	     	});
	});
		
});