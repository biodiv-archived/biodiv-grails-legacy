<%@ page import="content.eml.UFile"%>
<%@ page import="org.grails.taggable.Tag"%>

<%
	def canUploadFile = true //based on configuration
	def allowedExtensions = "[ 'pdf']" //should be taken from attributes
%>

<g:if test="${canUploadFile}">

	<uploader:uploader id="${name}"
		url="${uGroup.createLink(controller:'UFile', action:'upload', userGroupWebaddress:params.webaddress)}"
		multiple="true" allowedExtensions="${allowedExtensions}" params="${fileParams}">

		<uploader:onComplete>
				//Available variables: id, fileName, responseJSON
		
				if(responseJSON.success) {
				
					var uploader = $('#au-${name}')
					files = [];
					files.push({i:id, docName:responseJSON.fileName, filePath:responseJSON.filePath, docId:responseJSON.docId, fileSize:responseJSON.fileSize});
					
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
        				source: '/document/tags'
        			}, 
        			triggerKeys:['enter', 'comma', 'tab'], 
        			maxLength:30
        		});	

		</uploader:onComplete>
	</uploader:uploader>
	<!--====== Template ======-->
	<script id="${name}Tmpl" type="text/x-jquery-tmpl">

        <g:render template='/document/projectDoc' model="[name:name,  'docId':'{{=docId}}','filePath':'{{=filePath}}', 'docName':'{{=docName}}', 'fileSize':'{{=fileSize}}']"/>

	</script>

	<g:if test='${docs}'>
		<g:each var="fileAssist" in="${docs}" status="i">
		
			<%
			// To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
			 def doc = Document.get(fileAssist.id)
%>
			<g:render template='/document/projectDoc'
				model="[name:name,   'docId':doc.id,'filePath':doc.uFile.path, 'docName':doc.title, 'fileSize':doc.uFile.size, 'documentInstance':doc]" />

		</g:each>

	</g:if>


</g:if>