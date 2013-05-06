<% def allowedExtensions = "[ 'pdf']"  %>


<uploader:uploader id="${name}_uploader"
	url="${uGroup.createLink(controller:'UFile', action:'fileUpload', userGroupWebaddress:params.webaddress)}"
	multiple="false"
	allowedExtensions="${allowedExtensions}" 
	params="">

	<uploader:onComplete>
				//Available variables: id, fileName, responseJSON
		
				if(responseJSON.success) {
				
					$('#${name}_uploaded').show();
					$('#${name}_file').attr('href',responseJSON.fileURL);
					$('#${name}_file').html(fileName);
					$('#${name}_path').val(responseJSON.filePath);
					$('#${name}_name').val(fileName);
					
				}			

		</uploader:onComplete>	
</uploader:uploader>
<div id="${name}_uploaded" style="display: ${path?'':'none'};">
<%
def fileName=""
if(path) {
int idx = path.lastIndexOf("/");
fileName = idx >= 0 ? path.substring(idx + 1) : path;
}
 %>
	<i class="icon-file"></i> <a id="${name}_file" href="${path}">${fileName}</a>
	
	<input type="hidden" name="uFile.path"  id="${name}_path" value="${path}">
		<input type="hidden" name="uFile.size"  id="${name}_size" value="${size}">
	
</div>