<g:set var="autofillUsersId" value="id1"/>
<div>
	<div class="btn-group" style="z-index:10; float: left; margin-left: 5px;">
		<a id="identification-email" class="${(source == 'observationShow')?'btn btn-mini' : 'btn'} dropdown-toggle" 
			data-toggle="dropdown" href="#"><i class="icon-envelope"></i>Share</a>
		<form id="email-form" name="email-form" style="display: none; background-color: #F2F2F2;">
                        <div class="form-row">
                            <span class="keyname">To</span>
                            <sUser:selectUsers model="['id':autofillUsersId]"/>
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

<g:javascript src="species/users.js"></g:javascript>
<script>

$(function() {
	var autofillUsersComp = $("#userAndEmailList_${autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	$('#identification-email').click(function(){
			$.ajax({ 
	         	url:"${createLink(controller:'SUser', action:'isLoggedIn')}",
				success: function(data, statusText, xhr, form) {
					if(data === "true"){
						$('#userIdsAndEmailIds').val('');
						$('.userAndEmailList').val('')
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
		
	function UpdateCKEditors() {
	    for (var i in CKEDITOR.instances) {
	        CKEDITOR.instances[i].updateElement();
	    }    
	}
	
	$('#email-form').bind('submit', function(event) {
		//adding last entry if not ended with comma
		var emailAndIdsList = autofillUsersComp[0].getEmailAndIdsList();
		if(emailAndIdsList.length == 0){
			$("#userAndEmailList_${autofillUsersId}" ).addClass('alert alert-error');
			event.preventDefault();
			return false; 
		} else {
			$('#userIdsAndEmailIds').val(emailAndIdsList.join(","));
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

	
