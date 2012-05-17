<div>
	<div class="btn-group">
		<a id="identification-email" class="btn"
			data-toggle="dropdown" href="#"><i class="icon-envelope"></i>Ask</a>
		<form id="email-form" name="email-form" style="display: none; background-color: #F2F2F2;"
			action="${createLink(controller:'observation', action:'sendIdentificationMail', id:observationInstance.id)}"
			method="post">
			<span class="keyname">To</span><input id="userAndEmailList"
				placeholder='Type user name or email id' style="width:70%" type="text" /><br />
			<span class="keyname">Subject</span><input type="text" style="width:70%" name="mailSubject"
				value="${mailSubject}"></input><br />
			<h5><label><i class="icon-pencil"></i> Message </label></h5>
				<div class="section-item" style="margin-right: 10px;">
					<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
					</ckeditor:config>
					<ckeditor:editor name="mailBody" height="90px" toolbar="editorToolbar">
						${mailBody}
					</ckeditor:editor>
				</div>
			<input type="hidden" name="userIds" id="userIds" />
			<input type="hidden" name="emailIds" id="emailIds" /> 
			<input class="btn btn-mini" type="submit" value="send" style="float:right"> </input>
			<div id="email-form-close" value="close">
				<i class="icon-remove"></i>
			</div>
		</form>
	</div>
</div>

<script>
	$(function() {	
		$("#observationUrlLink").attr("href", document.location.href);
		
		$('#identification-email').click(function(){
			$('#userIds').val('');
			$('#emailIds').val('');
			$('#userAndEmailList').val('')
			$('#email-form').show();
		});
		$('#email-form-close').click(function(){
			$('#email-form').hide();
		});
		
	function isEmail(email) {
		var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
		return regex.test(email);
	}
	function split( val ) {
		return val.split( /,\s*/ );
	}
	function extractLast( term ) {
		return split( term ).pop();
	}

	function removeErrorClass(){
		if($("#userAndEmailList" ).hasClass('alert alert-error')){
			$("#userAndEmailList" ).removeClass(('alert alert-error'));
		}
	}

	function validateAndAddEmail(lastEntry){
		if(isEmail(lastEntry)){
			var oldValue = $("#emailIds").val();
			if(oldValue === ""){
				$("#emailIds").val(lastEntry);
			}else{
				$("#emailIds").val(oldValue + "," + lastEntry);
			}
			removeErrorClass();
		}
	}

	$("#userAndEmailList" )
		// don't navigate away from the field on tab when selecting an item
		.bind( "keydown", function( event ) {
			if ( event.keyCode === $.ui.keyCode.TAB &&
					$( this ).data( "autocomplete" ).menu.active ) {
				event.preventDefault();
			}else if( event.keyCode === $.ui.keyCode.COMMA){
				//storing email after validation
				validateAndAddEmail(extractLast(this.value));
			}
		})
		.autocomplete({
			source: function( request, response ) {
				$.getJSON( "${createLink(controller:'SUser', action: 'ajaxUserSearch')}", {
					term: extractLast( request.term )
				}, response );
			},
			search: function() {
				// custom minLength
				var term = extractLast( this.value );
				if ( term.length < 2 ) {
					return false;
				}
			},
			focus: function() {
				// prevent value inserted on focus
				return false;
			},
			select: function( event, ui ) {
				var terms = split( this.value );
				// remove the current input
				terms.pop();
				// add the selected item
				terms.push( ui.item.value );
				// add placeholder to get the comma-and-space at the end
				terms.push( "" );
				this.value = terms.join( ", " );

				//adding user ids from suggestion	
				var userId = ui.item.userId 
				var oldValue = $("#userIds").val();
				if(oldValue === ""){
					$("#userIds").val(userId);
				}else{
					$("#userIds").val(oldValue + "," + userId);
				}
				removeErrorClass();
				return false;
			}
		});

	$('#email-form').bind('submit', function(event) {
		//adding last entry if not ended with comma
		validateAndAddEmail($.trim(extractLast($("#userAndEmailList" ).val())));
		
		if(($('#userIds').val() == '') && ($('#emailIds').val() == '')){
			$("#userAndEmailList" ).addClass('alert alert-error');
			event.preventDefault();
			return false; 
		}
		
 		$(this).ajaxSubmit({ 
         	url:"${createLink(controller:'observation', action:'sendIdentificationMail', id:observationInstance.id)}",
			dataType: 'json', 
			type: 'POST',
			resetForm: true,
			success: function(data, statusText, xhr, form) {
				showRecoUpdateStatus('Email sent to respective person', 'success');
            	return false;
            },
            error:function (xhr, ajaxOptions, thrownError){
            	//successHandler is used when ajax login succedes
            	//var successHandler = this.success, errorHandler = showRecoUpdateStatus;
            	//handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
            	showRecoUpdateStatus('Error while sending email', 'error');
            	return false;
			} 
     	});
 		$('#userIds').val('');
		$('#emailIds').val('');
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
</style>
	