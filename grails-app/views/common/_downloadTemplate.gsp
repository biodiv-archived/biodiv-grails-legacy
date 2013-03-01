<%@ page import="species.participation.DownloadLog.DownloadType"%>
<div id="download-box" class="btn-group pull-left"  style="z-index: 10; float: left; margin-left: 5px;">
		<a id="download-action" class="btn dropdown-toggle" data-toggle="dropdown"
			href="#"> <i class=" icon-download-alt"></i>
			Download
		</a>

		<div id="download-options" class="popup-form" style="display: none">
			<form id="download-form">
				<div><span class="label label-important" style="padding:5px;margin-bottom: 10px;">The download link will be available on your user profile page</span></div>
				<g:each in="${DownloadType.list()}" var="downloadType" status="i">
					<g:if test="${i > 0}">
						<input type="radio" style="margin-top: 0px;" name="downloadType" value="${downloadType}">
						${'Export as ' + downloadType.value()}</input>
						<br />
					</g:if>
					<g:else>
						<input type="radio" style="margin-top: 0px;" name="downloadType" value="${downloadType}" CHECKED>
						${'Export as ' + downloadType.value()}</input>
						<br />
					</g:else>
				</g:each>
				<br />
				<textarea class="comment-textbox" placeholder="Please let us know how you intend to use this data" name="notes"></textarea>
<%--				<input style="width:385px" type="text" name="notes"></input><br />--%>
				<input class="btn pull-right" type="submit" value="OK"></input>
				<div id="download-close" class="popup-form-close" value="close">
					<i class="icon-remove"></i>
				</div>
			</form>
			
			<div id="downloadMessage">
			</div>
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
	         	url:"${uGroup.createLink(controller:'observation', action:'requestExport')}" + queryString,
				dataType: 'json', 
				type: 'POST',
				beforeSubmit: function(formData, jqForm, options) {
					formData.push({ "name": "filterUrl", "value": filterUrl});
				}, 
	            success: function(data, statusText, xhr, form) {
	            	$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
	            	$('#download-options').hide();
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
