<% def allowedExtensions = "[ 'pdf']"  %>


<uploader:uploader id="${name}_uploader"
	url="${uGroup.createLink(controller:'UFile', action:'fileUpload', userGroupWebaddress:params.webaddress)}"
	multiple="false"
	allowedExtensions="${allowedExtensions}">

	<uploader:onComplete>
				//Available variables: id, fileName, responseJSON
		
				if(responseJSON.success) {
				
					$('#${name}_uploaded').show();
					$('#${name}_file').attr('href',responseJSON.fileURL);
					$('#${name}_file').html(fileName);
					$('#${name}_path').val(responseJSON.filePath);
				}			

		</uploader:onComplete>
</uploader:uploader>
<div id="${name}_uploaded" style="display: ${path?'':'none'};">
	<i class="icon-file"></i> <a id="${name}_file" href="${path}">${fileName}</a>
	<input type="hidden" name="${name}"  id="${name}_path" value="${path}">
</div>