<div>
	<div class="btn-group">
		<a id="identification-email" class="btn btn-mini"
			data-toggle="dropdown" href="#"><i class="icon-envelope"></i>Share</a>
		<form id="email-form" name="email-form" style="display: none; background-color: #F2F2F2;"
			action="${createLink(controller:'observation', action:'sendIdentificationMail')}"
			method="post">
			<span class="keyname">To</span><input id="userAndEmailList"
				placeholder='Type user name or email id' style="float:left" type="text" /><br />
			<span class="keyname" style="clear:both">Subject</span><input type="text" style="width:70%" name="mailSubject"
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
			<input type="hidden" name="userIdsAndEmailIds" id="userIdsAndEmailIds" />
			<input class="btn btn-mini btn-primary" type="submit" value="send" style="float:right"> </input>
			<div id="email-form-close" value="close">
				<i class="icon-remove"></i>
			</div>
		</form>
	</div>
</div>

<script>
var validEmailAndIdList = []

function removeChoice(ele){
	console.log($(ele).attr("id"));
	var removedIndex = validEmailAndIdList.indexOf("" + $(ele).attr("id"));
	//alert("before remove " + validEmailAndIdList + " index " + removedIndex + "  item " + $(ele).attr("id"));
	validEmailAndIdList.splice(removedIndex, 1);
	//alert("vvv == " + validEmailAndIdList);
	$(ele).parent().remove();
}

$(function() {
	//validEmailAndIdList = []
	//alert(validEmailAndIdList);
	$("#observationUrlLink").attr("href", document.location.href);
		
	$('#identification-email').click(function(){
			$('#userIdsAndEmailIds').val('');
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

	function removeErrorClass(){
		if($("#userAndEmailList" ).hasClass('alert alert-error')){
			$("#userAndEmailList" ).removeClass(('alert alert-error'));
		}
	}

	function addLiChoice(itemValue, id){
		$('<li class="userAndEmailList-choice">' + itemValue + '<span id="'+ id + '" class="userAndEmailList-close" onClick="removeChoice(this);return false;"> x</span></li>').insertBefore("#userAndEmailList");
	}
	
	function validateAndAddEmail(lastEntry){
		if(isEmail(lastEntry)){
			addLiChoice(lastEntry, lastEntry);
			validEmailAndIdList.push(""+lastEntry);
			alert("after push email " +  validEmailAndIdList);
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
			}else if( event.keyCode === $.ui.keyCode.COMMA){
				//storing email after validation
				validateAndAddEmail($.trim(this.value));
				event.preventDefault();
			}
		})
		.autocomplete({
			source: function( request, response ) {
				$.getJSON( "${createLink(controller:'SUser', action: 'ajaxUserSearch')}", {
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
		
 		$(this).ajaxSubmit({ 
         	url:"${createLink(controller:'observation', action:'sendIdentificationMail')}",
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
</style>
	