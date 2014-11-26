/**
 * 
 */

var members_autofillUsersComp;
var experts_autofillUsersComp ;

function joinAction(me, joinUsUrl) {
	if(me.hasClass('disabled')) return false;
	
	$.ajax({
    	url: joinUsUrl,
        method: "POST",
        dataType: "json",
        success: function(data) {
        	if(data.statusComplete) {
        		$(me).html(data.shortMsg).removeClass("btn-success").addClass("disabled");
        		$(".alertMsg").removeClass('alert-error').addClass('alert-success').html(data.msg+"."+ window.i8ln.species.specie.reload);
        		//document.location.reload(true);
        	} else {
        		$(me).html(data.shortMsg).removeClass("btn-success").addClass("disabled");
        		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
        		//reloadActionsHeader();
        	}
        	
        }, error: function(xhr, status, error) {
            handleError(xhr, status, error, this.success, function() {
                var msg = $.parseJSON(xhr.responseText);
                $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
            });
        }
        });
	return false;
}

function requestMembershipAction(me, requestMembershipUrl) {
	if(me.hasClass('disabled')) return false;
	$.ajax({
    	url: requestMembershipUrl,
        method: "POST",
        dataType: "json",
        success: function(data) {
        	if(data.statusComplete) {
        		$(me).html(data.shortMsg).removeClass("btn-success").addClass("disabled");
        		$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
        		//document.location.reload(true);
        	} else {
        		$(me).html(data.shortMsg).removeClass("btn-success").addClass("disabled");
        		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
        		//reloadActionsHeader();
                }
        }, error: function(xhr, status, error) {
            handleError(xhr, status, error, this.success, function() {
                var msg = $.parseJSON(xhr.responseText);
                $(".alertMsg").html(msg.msg).removeClass('alert alert-success').addClass('alert alert-error');
            });
        }
        });
	return false;
}

function requestModeratorshipAction(me, url) {
	if($('#requestModerator').hasClass('disabled')) return false;
	$.ajax({
    	url: url,
        method: "POST",
        dataType: "json",
        data:{message:$('#requestModeratorMsg').val()},
        success: function(data) {
        	$('#requestModerator').html(data.shortMsg).removeClass("btn-success").addClass("disabled");
    		$('#requestModeratorDialog').modal('hide');
        	if(data.statusComplete) {
        		$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
        	} else {
        		$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
        	}
        }, error: function(xhr, status, error) {
			handleError(xhr, status, error, this.success, function() {
            	var msg = $.parseJSON(xhr.responseText);
                $(".alertMsg").html(msg.msg).removeClass('alert alert-success').addClass('alert alert-error');
			});
        }
	});
	return false;
}

function membership_actions() {
	$(".joinUs").bind('click', function() {
		joinAction($(this), window.joinUsUrl);
	})
	
	$(".requestMembership").bind('click', function() {
		requestMembershipAction($(this), window.requestMembershipUrl);
	})
	
	$("#requestModeratorButton").bind('click', function() {
		requestModeratorshipAction($(this), window.requestModeratorshipUrl);
	})
	
	$(".leaveUs").bind('click', function() {
		if($(this).hasClass('disabled')) return false;
		$('#leaveUsModalDialog').modal('show');
		return false;
	});
	
	$("#leave").click(function() {
		if($(this).hasClass('disabled')) return false;
		var dataGroupId = $(this).attr('data-group-id');
		var leaveUrl = $(this).attr('data-leaveUrl');
		
		var me = $(".leaveUs[data-group-id="+dataGroupId+"]");
		
		$.ajax({
        	url: leaveUrl,
            method: "POST",
            dataType: "json",
            data:{'id':dataGroupId},
            success: function(data) {
            	if(data.statusComplete) {
            		$("me").html(data.shortMsg).removeClass("btn-info").addClass("disabled");
            		$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            		//reloadMembers();
            	} else {
            		$("me").html(data.shortMsg).removeClass("btn-success").addClass("disabled");
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
	
	
	$("#inviteMemberButton").click(function(){
		$('#memberUserIds').val(members_autofillUsersComp[0].getEmailAndIdsList().join(","));
		$('#inviteMembersForm').ajaxSubmit({ 
			url:window.inviteMembersFormUrl,
			dataType: 'json', 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			data:{message:$('#inviteMemberMsg').val()},
			success: function(data, statusText, xhr, form) {
				if(data.statusComplete) {
					$('#inviteMembersDialog').modal('hide');
					$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				} else {
					$("#invite_memberMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				}
				$('#inviteMembersForm')[0].reset()
			}, error:function (xhr, ajaxOptions, thrownError){
					//successHandler is used when ajax login succedes
	            	var successHandler = this.success;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
						var response = $.parseJSON(xhr.responseText);
						
				});
	            $('#inviteMembersForm')[0].reset()
           } 
     	});	
	});
	
	$("#inviteExpertButton").click(function(){
		$('#expertUserIds').val(experts_autofillUsersComp[0].getEmailAndIdsList().join(","));
		$('#inviteExpertsForm').ajaxSubmit({ 
			url:window.inviteExpertsFormUrl,
			dataType: 'json', 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			data:{message:$('#inviteModeratorMsg').val()},
			success: function(data, statusText, xhr, form) {
				if(data.statusComplete) {
					$('#inviteExpertsDialog').modal('hide');
					$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				} else {
					$("#invite_expertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				}
				$('#inviteExpertsForm')[0].reset()
			}, error:function (xhr, ajaxOptions, thrownError){
					//successHandler is used when ajax login succedes
	            	var successHandler = this.success;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
						var response = $.parseJSON(xhr.responseText);
						
				});
	            $('#inviteExpertsForm')[0].reset()
           } 
     	});	
	});
     	
   	$('#inviteMembersDialog').modal({
		"show" : false,
		"backdrop" : "static"
	});
   	
   	$('#inviteExpertsDialog').modal({
		"show" : false,
		"backdrop" : "static"
	});
   	
   	$('#requestModeratorDialog').modal({
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
						$('#userAndEmailList_1').val('');
						$(members_autofillUsersComp[0]).parent().children('li').each(function(){
							$(members_autofillUsersComp[0]).removeChoice($(this).find('span')[0]);
						});
						$('#inviteMembersForm')[0].reset()
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
	
	$('#inviteExperts').click(function(){
		$.ajax({ 
         	url:window.isLoggedInUrl,
			success: function(data, statusText, xhr, form) {
				if(data === "true"){
					$('#expertUserIds').val('');
					$('#userAndEmailList_2').val('');
					$(experts_autofillUsersComp[0]).parent().children('li').each(function(){
						$(experts_autofillUsersComp[0]).removeChoice($(this).find('span')[0]);
					});
					$('#inviteExpertsForm')[0].reset()
					$('#inviteExpertsDialog').modal('show');
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
	
	$('#requestModerator').click(function(){
		if($('#requestModerator').hasClass('disabled')) return false;
		$.ajax({ 
         	url:window.isLoggedInUrl,
			success: function(data, statusText, xhr, form) {
				if(data === "true"){
					$('#requestModeratorDialog').modal('show');
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
	
}

//this is called from domain/_headerTemplate
function init_group_header() {
console.log('init_group_header');	
	members_autofillUsersComp = $("#userAndEmailList_"+window.members_autofillUsersId).autofillUsers({
		usersUrl : window.userTermsUrl
	});

	experts_autofillUsersComp = $("#userAndEmailList_"+window.experts_autofillUsersId).autofillUsers({
		usersUrl : window.userTermsUrl
	});
    
    curators_autofillUsersComp = $("#userAndEmailList_curator").autofillUsers({
        //appendTo:"#userNameSuggestions",
		usersUrl : window.params.userTermsUrl
	});

    contributors_autofillUsersComp = $("#userAndEmailList_contributor").autofillUsers({
        //appendTo:"#userNameSuggestions",
		usersUrl : window.params.userTermsUrl
	});

}

/*
 * needs to be called only once for a page
 * as calling this function multiple times would result in
 * multiple bindings of following event handlers
 */
function init_header() {
	$("#allGroups").click(function(){
		
			$("#myGroupsInfo").slideUp('fast');
			$("#allGroupsInfo").slideDown('slow');
		
		return false;
	});
	$("#myGroups").click(function(){
		
			$("#allGroupsInfo").slideUp('fast');
			$("#myGroupsInfo").slideDown('slow');
		
		return false;
	});
	
	$('body').on('click.collapse-next.data-api', '[data-toggle=collapse-next]', function (e) {
		  $(this).parent().nextAll($(this).attr("data-target")).slideToggle();
	});
		
//	$(".close").click(function(){
//		$(this).parent().slideUp('fast');
//		return false;
//	})
	$(".active .submenu").show();
	
	init_group_header();

	membership_actions();
	
}

function loadYoutube(youtube_container) {
	var info = $(youtube_container).find(".info");
	var preview = $(youtube_container).find(".preview")
	
	var youtube_video_id = $(preview).find('span.videoId').text();
	if(youtube_video_id) {
		var api_url = "https://gdata.youtube.com/feeds/api/videos/" + youtube_video_id + "?v=2&alt=json-in-script&callback=?";
		 this;
		$.getJSON(api_url, function(data) {
		    $(info).html("<b><a href='http://youtube.com/watch?v=" + youtube_video_id + "' target='_blank'>" + data.entry.title.$t + "</a></b>");
		});	
	}
	
	
	$(preview).click(function() {
		var youtube_video_id = $(this).find('span.videoId').text();
		if(youtube_video_id) {
			var iframe_url = "http://www.youtube.com/embed/" + youtube_video_id + "?autoplay=1";
		    $(preview).html("<iframe width='385' height='300' src='" + iframe_url + "' frameborder='0' allowfullscreen></iframe>");
		    $(preview).css("float", "none");
		    return false;
		}
	 });
}

function rating() {
    rate($(".star_rating"));
    like($(".like_rating"));
}

function last_actions() {
    $(".ellipsis.multiline").trunk8({
        lines:2,
    tooltip:false,
    fill: '&hellip; <a id="read-more" href="#">'+window.i8ln.species.util.mor+'</a>'
    });
    $('#read-more').on('click', function (event) {
        $(this).parent().trunk8('revert').append(' <a id="read-less" href="#">'+window.i8ln.species.util.rles+'</a>');

        return false;
    });

    $('#read-less').on('click', function (event) {
        $(this).parent().trunk8();

        return false;
    });

    $(".ellipsis:not(.multiline)").trunk8();

    $('.collapse').on({
        shown: function(){
            $(this).css('overflow','visible');
        },
        hide: function(){
            $(this).css('overflow','hidden');
        }
    });

    $('#contributeMenu.collapse').on({
        shown: function(){
            $.cookie("contribute", "show", {path    : '/'});
        },
        hide: function(){
            $.cookie("contribute", "hide", {path    : '/'});
        }
    });

    if ($.cookie("contribute") == "show" ) {
        $('#contributeMenu.collapse').collapse('show');
    }

    $('.yj-message-body').linkify();
    $('.linktext').linkify(); 
    //applying table sorting
    $("table.tablesorter").tablesorter();
    rating();

    $("#contributeMenu .btn").popover();

    updateGroupPostSelection();

	$(".mainContentList").unbind('click').on('click', '.joinUs', function() {
		var joinUsUrl = window.params.userGroup.joinUsUrl + "/?id=" + $(this).attr('data-group-id') //+"/joinUs";
		joinAction($(this), joinUsUrl);
	});
	
	$(".requestMembership").unbind('click').on('click', function() {
		var requestMembershipUrl = window.params.userGroup.requestMembershipUrl+"/?id="+$(this).attr('data-group-id')//+"/requestMembership";
		requestMembershipAction($(this), requestMembershipUrl);
	});
	
	$(".leaveUs").unbind('click').on('click', function() {
		var leaveUrl = window.params.userGroup.leaveUrl //+"/"+$(this).attr('data-group-id')+"/leaveUs";
		$("#leave").attr('data-group-id', $(this).attr('data-group-id'))
		$("#leave").attr('data-leaveUrl', leaveUrl)
		$('#leaveUsModalDialog').modal('show');
	});
}

function loadSuggestedGroups(targetComp, url){
	var res = $(targetComp).children('li');
	if(res.length > 0){
		return 
	}
	$.ajax({
 		url: url,
 		type: 'POST',
		dataType: "json",
		success: function(data) {
			$(targetComp).append($(data.suggestedGroupsHtml));
			$(targetComp).show(1000);
			return false;
		}, error: function(xhr, status, error) {
			alert(xhr.responseText);
	   	}
	});
}
