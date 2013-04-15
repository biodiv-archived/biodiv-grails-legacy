<%@ page import="content.fileManager.UFile"%>
<%@ page import="org.grails.taggable.Tag"%>

<%
	def canUploadFile = true //based on configuration
	def allowedExtensions = "[ 'pdf']" //should be taken from attributes
%>
<g:if test="${canUploadFile}">

	<uploader:uploader id="${name}"
		url="${uGroup.createLink(controller:'UFile', action:'upload', userGroupWebaddress:params.webaddress)}"
		multiple="true" allowedExtensions="${allowedExtensions}">

		<uploader:onComplete>
				//Available variables: id, fileName, responseJSON
		
				if(responseJSON.success) {
				
					var uploader = $('#au-${name}')
					files = [];
					files.push({i:id, fileName:responseJSON.fileName, filePath:responseJSON.filePath, fileId:responseJSON.fileId, fileSize:responseJSON.fileSize});
					
					var html = $('#${name}Tmpl').render(files);					
					var metaDataEle = $(html);
					uploader.append(metaDataEle);

				}			
				$('#' + responseJSON.fileId +'-tags').tagit({
        			select:true, 
        			allowSpaces:true, 
        			fieldName: responseJSON.fileId + '.tags',
        			placeholderText:'Add some tags',
        			autocomplete:{
        				source: '/UFile/tags'
        			}, 
        			triggerKeys:['enter', 'comma', 'tab'], 
        			maxLength:30
        		});	

		</uploader:onComplete>
	</uploader:uploader>
	<!--====== Template ======-->
	<script id="${name}Tmpl" type="text/x-jquery-tmpl">

        <g:render template='/UFile/fileBlock' model="[name:name,  'fileId':'{{=fileId}}','filePath':'{{=filePath}}', 'fileName':'{{=fileName}}', 'fileSize':'{{=fileSize}}']"/>

	</script>

	<g:if test='${uFiles}'>
		<g:each var="fileAssist" in="${uFiles}" status="i">
		
			<%
			// To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
			 def uFile = UFile.get(fileAssist.id)
%>
			<g:render template='/UFile/fileBlock'
				model="[name:name,   'fileId':uFile.id,'filePath':uFile.path, 'fileName':uFile.name, 'fileSize':uFile.size, 'uFileInstance':uFile]" />

		</g:each>

	</g:if>


</g:if>