

<%@ page import="utils.Newsletter"%>
<html>
<head>
<g:set var="title" value="Newsletter"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="core" />
</head>
<body>
	<div class="span9">
		<div class="page-header">
			<h1>Edit</h1>
		</div>
		
		<div class="tabbable">

			<g:hasErrors bean="${newsletterInstance}">
				<div class="errors">
					<g:renderErrors bean="${newsletterInstance}" as="list" />
				</div>
			</g:hasErrors>

			<form
				action="${uGroup.createLink(controller:'newsletter', action:'update', userGroupWebaddress:params.webaddress)}"
				method="POST">
				<g:hiddenField name="id" value="${newsletterInstance?.id}" />
				<g:hiddenField name="version" value="${newsletterInstance?.version}" />

				<table>
					<tbody>
						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'title', 'errors')}">
								<g:textField name="title" value="${newsletterInstance?.title}" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'date', 'errors')}">
								<g:datePicker name="date" precision="day"
									value="${newsletterInstance?.date}" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'newsitem', 'errors')}">
								<ckeditor:editor name="newsitem" height="400px" userSpace="${params.webaddress }">
									${newsletterInstance?.newsitem}
								</ckeditor:editor>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'sticky', 'errors')}">

								<g:checkBox style="margin-left:0px;" name="sticky"
									checked="${newsletterInstance.sticky}" /> <g:message
									code="newsletter.sticky"
									default="Check this option to make this page available in sidebar?" />
							</td>
						</tr>
						<g:if test="${newsletterInstance.userGroup}">
							<input type="hidden" name="userGroup"
								value="${newsletterInstance.userGroup.webaddress}" />
						</g:if>
					</tbody>
				</table>

				<div class="buttons">
					<span class="button"> <input class="btn btn-primary"
						style="float: right; margin-right: 5px;" type="submit"
						value="Update" /> </span> <span class="button"> <a
						class="btn btn-danger" style="float: right; margin-right: 5px;"
						href="${uGroup.createLink(controller:'newsletter', action:'delete', id:newsletterInstance.id)}"
						onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">Delete
					</a> </span>
				</div>
			</form>
		</div>
	</div>

            <r:script type='text/javascript'> 
                CKEDITOR.plugins.addExternal( 'confighelper', '${request.contextPath}/js/ckeditor/plugins/confighelper/' );

                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[
                    { name: 'document', groups: [ 'mode', 'document', 'doctools' ], items: [ 'Source', '-', 'Save', 'Preview'  ] },
                    { name: 'clipboard', groups: [ 'clipboard', 'undo' ], items: [ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo' ] },
                    { name: 'editing', groups: [ 'find', 'selection', 'spellchecker' ], items: [ 'Find', 'Replace', '-', 'SelectAll', '-', 'Scayt' ] },
                    '/',
                    { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ], items: [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'RemoveFormat' ] },
                    { name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ], items: [ 'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote', 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', 'BidiLtr', 'BidiRtl', 'Language' ] },
                    { name: 'links', items: [ 'Link', 'Unlink', 'Anchor' ] },
                    { name: 'insert', items: [ 'Image', 'Table'] }
                    ],
                    filebrowserImageBrowseUrl: "/${grailsApplication.metadata['app.name']}/ck/ofm?fileConnector=/${grailsApplication.metadata['app.name']}/ck/ofm/filemanager&viewMode=grid&space=newsletters/${params.webaddress}&type=Image",
                    filebrowserImageUploadUrl: "/biodiv/ck/standard/uploader?Type=Image&userSpace=${params.webaddress}",

                        height: '400px'
                };

		$(document).ready(function(){
                    CKEDITOR.replace('newsitem', config);
		});
	</r:script>
</body>
</html>
