<div>
	<div class="btn-group" style="z-index:10; float: left; margin-left: 5px;">
		<a id="identification-email" class="${(source == 'observationShow')?'btn btn-mini' : 'btn'} dropdown-toggle" 
			data-toggle="dropdown" href="#"><i class="icon-envelope"></i>Share</a>
		<form id="email-form" name="email-form" style="display: none; background-color: #F2F2F2;">
                        <div class="form-row">
                            <span class="keyname">To</span>
                            <ul class="userOrEmail-list">
                            <li id="userOrEmail-new">
                            <input id="userAndEmailList"
                                    placeholder='Type user name or email id' style="float:left" type="text" />
                            </li>
                            </ul>
                        </div>
                        <div class="form-row">
                            <span class="keyname" style="clear:both">Subject</span><input type="text" style="width:97%" name="mailSubject"
                                    value="${mailSubject}"></input>
                        </div>        

                        <div class="form-row">
			<i class="icon-pencil"></i><span class="keyname" style="clear:both"> Message</span>
			<h5><label>${staticMessage}</label></h5>
				<div class="section-item">
                                        <div id="cke_message">
					<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
					</ckeditor:config>
					<ckeditor:editor name="userMessage" height="90px" toolbar="editorToolbar">
					</ckeditor:editor>
                                        </div>
				</div>
                        </div>        
			<input type="hidden" name="userIdsAndEmailIds" id="userIdsAndEmailIds" />
			<input class="btn btn-mini btn-primary" type="submit" value="SEND" style="margin:10px; float:right"> </input>
			<div id="email-form-close" value="close">
				<i class="icon-remove"></i>
			</div>
		</form>
	</div>
</div>

<script>
var validEmailAndIdList = []

function removeChoice(ele){
	var removedIndex = validEmailAndIdList.indexOf("" + $(ele).attr("id"));
	validEmailAndIdList.splice(removedIndex, 1);
	$(ele).parent().remove();
}

$(function() {
	//validEmailAndIdList = []
	$('#identification-email').click(function(){
			$.ajax({ 
	         	url:"${createLink(controller:'SUser', action:'isLoggedIn')}",
				success: function(data, statusText, xhr, form) {
					if(data === "true"){
						$('#userIdsAndEmailIds').val('');
						$('#userAndEmailList').val('')
						$('#email-form').show();
						return false;
					}else{
						window.location.href = "/biodiv/login?spring-security-redirect="+window.location.href;
					}
	            },
	            error:function (xhr, ajaxOptions, thrownError){
	            	return false;
				} 
	     	});
	});
	$('#email-form-close').click(function(){
			$('#email-form').hide();
	});
		
	function isEmail(email) {
		var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
		return regex.test(email);
	}

	function removeErrorClass(){
		if($("#userAndEmailList" ).hasClass('alert alert-error')){
			$("#userAndEmailList" ).removeClass(('alert alert-error'));
		}
	}

	function addLiChoice(itemValue, id){
		$('<li class="userOrEmail-choice">' + itemValue + '<span id="'+ id + '" class="userOrEmail-close" onClick="removeChoice(this);return false;"> x</span></li>').insertBefore("#userOrEmail-new");
	}
	
	function validateAndAddEmail(lastEntry){
		if(isEmail(lastEntry)){
			addLiChoice(lastEntry, lastEntry);
			validEmailAndIdList.push(""+lastEntry);
			removeErrorClass();
		}
		$("#userAndEmailList").val("");
	}

	function addUserId(ui){
		var userId = ui.item.userId;
		addLiChoice(ui.item.value, userId);
		validEmailAndIdList.push(""+userId);
		removeErrorClass();
		$("#userAndEmailList").val("");
	}

	$("#userAndEmailList" )
		// don't navigate away from the field on tab when selecting an item
		.bind( "keydown", function( event ) {
			if ( event.keyCode === $.ui.keyCode.TAB &&
					$( this ).data( "autocomplete" ).menu.active ) {
				event.preventDefault();
			}else if( event.keyCode === $.ui.keyCode.COMMA || event.keyCode === $.ui.keyCode.ENTER){
				//storing email after validation
				validateAndAddEmail($.trim(this.value));
				event.preventDefault();
			}
		})
		.autocomplete({
			source: function( request, response ) {
				$.getJSON( "${createLink(controller:'SUser', action: 'terms')}", {
					term: request.term
				}, response );
			},
			search: function() {
				// custom minLength
				var term = this.value;
				if ( term.length < 2 ) {
					return false;
				}
			},
			focus: function() {
				return false;
			},
			select: function( event, ui ) {
				//adding user ids from suggestion
				addUserId(ui);	
				return false;
			}
		});

	function UpdateCKEditors() {
	    for (var i in CKEDITOR.instances) {
	        CKEDITOR.instances[i].updateElement();
	    }    
	}
	
	$('#email-form').bind('submit', function(event) {
		//adding last entry if not ended with comma
		validateAndAddEmail($.trim($("#userAndEmailList" ).val()));
		if(validEmailAndIdList.length == 0){
			$("#userAndEmailList" ).addClass('alert alert-error');
			event.preventDefault();
			return false; 
		}else{
			$('#userIdsAndEmailIds').val(validEmailAndIdList.join(","));
		}
		UpdateCKEditors();
		
		$(this).ajaxSubmit({ 
         	url:"${createLink(controller:'observation', action:'sendIdentificationMail')}",
			dataType: 'json', 
			type: 'POST',
			data:{sourcePageUrl : window.location.href, source:"${source}"},
			resetForm: true,
			success: function(data, statusText, xhr, form) {
				//showRecoUpdateStatus('Email sent to respective person', 'success');
            	return false;
            },
            error:function (xhr, ajaxOptions, thrownError){
            	//successHandler is used when ajax login succedes
            	var successHandler = this.success;//, errorHandler = showRecoUpdateStatus;
            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() { return false;});
            	//showRecoUpdateStatus('Error while sending email', 'error');
            	return false;
			} 
     	});
 		$('#userIdsAndEmailIds').val('');
		$('#email-form').hide();
     	event.preventDefault();
 	});
 	
});
</script>
<style>
#email-form {
	background-clip: padding-box;
	background-color: #FFFFFF;
	border-color: rgba(0, 0, 0, 0.2);
	border-radius: 0 0 5px 5px;
	border-style: solid;
	border-width: 1px;
	box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
	display: none;
	float: left;
	left: 0;
	list-style: none outside none;
	margin: 0;
	min-width: 400px;
	max-width: 400px;
	width : 400px;
	padding: 10px;
	position: absolute;
	top: 100%;
	z-index: 1000;
	color: #000000;
}

#email-form-close {
	position: absolute;
	top: 0;
	right: 0;
}

input#userAndEmailList {
-moz-box-sizing: border-box;
border: medium none !important;
margin: 0 !important;
outline: medium none;
padding: 0 !important;
width: inherit !important;
}

.userOrEmail-list {
background: none repeat scroll 0 0 #FFFFFF;
border: 1px solid #CCCCCC;
cursor: text;
margin: 0;
overflow: auto;
padding: 3px;
list-style: none;       
clear: both;       
}

li.userOrEmail-choice {
background-color: #DEE7F8;
border: 1px solid #CAD8F3;
padding: 2px 13px 3px 4px;
border-radius: 5px 5px 5px 5px;
display: block;
float: left;
margin: 2px 5px 2px 0;
position: relative;
}

li.userOrEmail-choice:hover {
background-color: #bbcef1;
}

span.userOrEmail-close {
cursor:pointer;
}

li#userOrEmail-new {
padding: 2px 4px 1px 0;
margin-left: 0;         
border-radius: 5px 5px 5px 5px;
display: block;
float: left;
margin: 2px 5px 2px 0;
position: relative;
}

.form-row {
margin: 10px;
}

.keyname {
float: left;
font-size: 0.9em;
font-weight: bold;
margin-right: 10px;
text-align: right;
}

#cke_userMessage {
padding: 0;
width: 100%;         
}

#cke_message {
width: 100%;
min-width: 100%;
max-width: 100%;
}

</style>
	
