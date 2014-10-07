<div id="download-box" class="btn-group"
	style="z-index: 10; margin-left: 5px;${instanceTotal == 0 ? 'display:none;' :'' }">
	<a id="download-action"
		class="btn ${(params.action=='show')?'btn-link':''} dropdown-toggle"
		data-toggle="dropdown" href="#"> <i class=" icon-download-alt"></i>
		<g:message code="button.download" />
	</a>
        <a id="feature-action"
		class="btn ${(params.action=='show')?'btn-link':''} dropdown-toggle"
		data-toggle="dropdown" href="#"> <i class=" icon-download-alt"></i>
		<g:message code="button.feature" />
	</a>

	<div id="download-options" class="popup-form" style="display: none">
		<form id="download-form">
			<div>
				<span class="label label-info"
					style="padding: 5px; margin-bottom: 10px;"><g:message code="msg.link.available" /></span>
			</div>
 
			<input type="hidden" name="downloadType" value="ZIP" /> <br />
			 
			<textarea class="comment-textbox"
				placeholder="Please let us know how you intend to use this data"
				name="notes"></textarea>
			<input class="btn pull-right" type="submit" value="OK"></input>
			<div id="download-close" class="popup-form-close" value="close">
				<i class="icon-remove"></i>
			</div>
		</form>

		<div id="downloadMessage"></div>
	</div>
</div>

<r:script>
$('#download-close').click(function(){
	$('#download-options').hide();
});

$('#download-action').click(function(){
	$.ajax({ 
        	url:"${uGroup.createLink(controller:'SUser', action:'isLoggedIn')}",
		success: function(data, statusText, xhr, form) {
			if(data === "true"){
				$('#download-options').show();
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

$(document).ready(function(){
	$('#download-form').bind('submit', function(event) {
			var filterUrl = window.location.href
			var queryString =  window.location.search
			$(this).ajaxSubmit({ 				
	         	url:"${uGroup.createLink(controller:'species', action:'requestExport', userGroupWebaddress:params.webaddress)}" + queryString,
				dataType: 'json', 
				type: 'POST',
				beforeSubmit: function(formData, jqForm, options) {
					formData.push({ "name": "filterUrl", "value": filterUrl});
					formData.push({ "name": "source", "value": "${source}"});
					formData.push({ "name": "downloadObjectId", "value": "${downloadObjectId}"});
				}, 
	            success: function(data, statusText, xhr, form) {
	            	$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
	            	$('#download-options').hide();
	            	$("html, body").animate({ scrollTop: 0 });
	            	return false;
	            },
	            error:function (xhr, ajaxOptions, thrownError){
	            	//successHandler is used when ajax login succedes
	            	var successHandler = this.success, errorHandler = null;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
				} 
	     	});
	     	event.preventDefault();
     	});

});     	
</r:script>

