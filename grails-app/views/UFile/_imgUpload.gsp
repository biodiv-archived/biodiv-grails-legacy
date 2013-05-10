

<% def allowedExtensions = "[ 'png', 'gif','jpg', 'jpeg']"  %>



<uploader:uploader id="${name}_uploader"
	url="${uGroup.createLink(controller:'UFile', action:'fileUpload', userGroupWebaddress:params.webaddress)}"
	multiple="false"
	allowedExtensions="${allowedExtensions}"
	params="${fileParams}"
	sizeLimit="${grailsApplication.config.speciesPortal.content.MAX_IMG_SIZE}">

	<uploader:onComplete>
				//Available variables: id, fileName, responseJSON
		
				if(responseJSON.success) {
				
					$('#${name}_uploaded').show();
					$('#${name}_thumbnail').attr('src',responseJSON.fileURL);
					$('#${name}_thumbnail').attr('alt', fileName);
					$('#${name}_path').val(responseJSON.filePath);
				}			

		</uploader:onComplete>
</uploader:uploader>
<div id="${name}_uploaded" style="display: ${path?'':'none'};">
	<img id="${name}_thumbnail" class="logo" src="" />
	<input type="hidden" name="${name}"  id="${name}_path" value="${path}">
</div>