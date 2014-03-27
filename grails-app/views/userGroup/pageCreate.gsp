
<%@page import="species.utils.Utils"%>
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="entityName"
	value="${(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)}" />

<g:set var="title" value="Pages"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="userGroups_show" />
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate />
		<div class="userGroup-section">
			
			<g:include controller="newsletter" action="create"
				id="${newsletterId }" params="['userGroup':userGroupInstance?:null, 'webaddress':userGroupInstance?.webaddress]" />
				
			<div class="btn-group pull-right" style="z-index: 10;clear:both;margin-top:5px;">

				<g:if test="${userGroupInstance}">
					<g:link url="${uGroup.createLink(mapping:'userGroup', action:'pages', id:userGroupInstance.id, userGroup:userGroupInstance)}"
						class="btn btn-info">< Back to Pages</g:link>
				</g:if>
				<g:else>
					<g:link url="${uGroup.createLink(mapping:'userGroupGeneric', action:'pages')}" class="btn btn-info">< Back to Pages</g:link>
				</g:else>


			</div>

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
