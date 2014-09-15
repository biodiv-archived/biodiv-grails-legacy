<div>
	<div class="btn-group"
		style="z-index: 10; float: left; margin-left: 5px;">
		<button id="identification-email"
			class="${(params.action == 'show')?'btn btn-link' : 'btn'} dropdown-toggle"
            title="${titleTooltip?:g.message(code:'placeholder.share.email') }"
            data-toggle="dropdown" href="#"><i class="icon-envelope"></i>${g.message(code:'button.share')}</button>
		<form id="email-form" name="email-form" class="popup-form"
			style="display: none; background-color: #F2F2F2;">
            <div class="form-row" style="${hideTo?'display:none;':''}">
				<span class="keyname"><g:message code="default.to.label" /></span>
				<ul class="userOrEmail-list">
					<input id="userAndEmailList_${autofillUsersId}"
						placeholder='${g.message(code:"placeholder.name.email")}' style="float: left"
						type="text" />
				</ul>
            </div>
            
			<div class="form-row">
				<span class="keyname" style="clear: both"><g:message code="default.subject.label" /></span><input
					type="text" style="width: 97%" name="mailSubject"
					value="${mailSubject}"/>
			</div>

			<div class="form-row">
				<i class="icon-pencil"></i><span class="keyname" style="clear: both">
					<g:message code="default.message.label" /></span>
				<h5>
					<label>${staticMessage.encodeAsRaw()} </label>
				</h5>
				<div class="section-item">
					<div id="cke_message">
						<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
					</ckeditor:config>
						<ckeditor:editor name="userMessage" height="90px"
							toolbar="editorToolbar">
						</ckeditor:editor>
					</div>
				</div>
			</div>
			<input type="hidden" name="userIdsAndEmailIds"
				id="userIdsAndEmailIds" /> <input class="btn btn-mini btn-primary"
				type="submit" value="SEND" style="margin: 10px; float: right" />
			<div id="email-form-close" class="popup-form-close" value="close">
				<i class="icon-remove"></i>

			</div>
		</form>
	</div>

</div>
<style>
#cke_message #cke_userMessage{
	max-width: 370px;
}
</style>

<script type="text/javascript">

$(function() {
	var autofillUsersComp = $("#userAndEmailList_${autofillUsersId}").autofillUsers({
		usersUrl : '${uGroup.createLink(controller:'SUser', action: 'terms')}'
	});
	
	$('#identification-email').click(function(){
			$.ajax({ 
	         	url:"${uGroup.createLink(controller:'SUser', action:'isLoggedIn')}",
				success: function(data, statusText, xhr, form) {
					if(data === "true"){
						$('#userIdsAndEmailIds').val('');
						$('.userAndEmailList').val('')
						$('ul.userOrEmail-list > li').each(function(){
							autofillUsersComp[0].removeChoice($(this).find('span')[0]);
						});
						$('#email-form')[0].reset()

	                    for (var i in CKEDITOR.instances) {
                	        CKEDITOR.instances[i].setData('');
                        }
						$('#email-form').show();
						return false;
					}else{
						window.location.href = "${uGroup.createLink(controller:'login')}?spring-security-redirect="+window.location.href;
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
		
	function UpdateCKEditors() {
	    for (var i in CKEDITOR.instances) {
	        CKEDITOR.instances[i].updateElement();
	    }    
	}
	
	$('#email-form').bind('submit', function(event) {
		//adding last entry if not ended with comma
		var emailAndIdsList = autofillUsersComp[0].getEmailAndIdsList();
        <g:each in="${users}" var="user">
        emailAndIdsList.push("${user.id}");
        </g:each>

		if(emailAndIdsList.length == 0){
			$("#userAndEmailList_${autofillUsersId}" ).addClass('alert alert-error');
			event.preventDefault();
			return false; 
		} else {
            $('#userIdsAndEmailIds').val(emailAndIdsList.join(","));
		}
		
		UpdateCKEditors();
		
		$(this).ajaxSubmit({ 
         	url:"${uGroup.createLink(controller:'observation', action:'sendIdentificationMail')}",
			dataType: 'json', 
			type: 'POST',
                        data:{sourcePageUrl : window.location.href, source:"${source}", 'sourceController':'${params.controller}', 'sourceAction':'${params.action}'},
			resetForm: true,
			success: function(data, statusText, xhr, form) {
				//showUpdateStatus('Email sent to respective person', 'success');
            	return false;
            },
            error:function (xhr, ajaxOptions, thrownError){
            	//successHandler is used when ajax login succedes
            	var successHandler = this.success;//, errorHandler = showUpdateStatus;
            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() { return false;});
            	//showUpdateStatus('Error while sending email', 'error');
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
    /*
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
	cursor: pointer;
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
}*/
</style>
