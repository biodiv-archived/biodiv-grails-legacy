<% def allowedExtensions = "[ 'pdf']"  %>


<uploader:uploader id="${name}_uploader"
	url="${uGroup.createLink(controller:'UFile', action:'fileUpload', userGroupWebaddress:params.webaddress)}"
	multiple="false"
	allowedExtensions="${allowedExtensions}" 
	params="${fileParams}"
	sizeLimit="${grailsApplication.config.speciesPortal.content.MAX_DOC_SIZE}">

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
def fileURL = ""
if(path) {
int idx = path.lastIndexOf("/");
fileName = idx >= 0 ? path.substring(idx + 1) : path;
fileURL = g.createLinkTo(base:grailsApplication.config.speciesPortal.content.serverURL,	file: path)
}
 %>
	<i class="icon-file"></i> <a id="${name}_file" href="${fileURL}">${fileName}</a>
	
	<input type="hidden" name="uFile.path"  id="${name}_path" value="${path}">
		<input type="hidden" name="uFile.size"  id="${name}_size" value="${size}">
	
</div>