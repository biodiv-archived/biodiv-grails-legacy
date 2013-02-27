<%@ page import="species.participation.DownloadLog.DownloadType"%>
<div>
	<div class="btn-group">
		<a id="download-action" class="btn dropdown-toggle" data-toggle="dropdown"
			href="#"> <i class="icon-download"></i>
			Download
		</a>

		<div id="download-options" class="popup-form" style="display: none">
			<form id="download-form">
				<g:each in="${DownloadType.list()}" var="downloadType" status="i">
					<g:if test="${i > 0}">
						<input type="radio" name="downloadType" value="${downloadType}">
						${downloadType.value()}</input>
						<br />
					</g:if>
					<g:else>
						<input type="radio" name="downloadType" value="${downloadType}" CHECKED>
						${downloadType.value()}</input>
						<br />
					</g:else>
				</g:each>
				<br />
				<input class="input-xlarge" type="text" name="notes" placeholder="Any comment"></input><br />
				<input class="btn pull-right" type="submit" value="OK"></input>
				<div id="download-close" class="popup-form-close" value="close">
					<i class="icon-remove"></i>
				</div>
			</form>
			
			<div id="downloadMessage">
			</div>
		</div>
	</div>
</div>

<r:script>

$('#download-action').click(function(){
	$('#download-options').show();
});
$('#download-close').click(function(){
	$('#download-options').hide();
});

$(document).ready(function(){
	$('#download-form').bind('submit', function(event) {
			var filterUrl = window.location.href
			var queryString =  window.location.search
     		$(this).ajaxSubmit({ 
	         	url:"${uGroup.createLink(controller:'observation', action:'export')}" + queryString,
				dataType: 'json', 
				type: 'POST',
				beforeSubmit: function(formData, jqForm, options) {
					formData.push({ "name": "filterUrl", "value": filterUrl});
				}, 
	            success: function(data, statusText, xhr, form) {
	            	//alert("Your request in under processing. Please check your user profile after some time.");
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
