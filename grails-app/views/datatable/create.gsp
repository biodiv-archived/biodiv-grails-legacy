<%@page import="species.utils.Utils"%>
<%@ page import="species.dataset.Dataset"%>
<%@ page import="species.dataset.DataPackage"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'dataset.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style>
            .btn-group.open .dropdown-menu {
            top: 43px;
            }

            .btn-large .caret {
            margin-top: 13px;
            position: absolute;
            right: 10px;
            }

            .btn-group .btn-large.dropdown-toggle {
            width: 300px;
            height: 44px;
            text-align: left;
            padding: 5px;
            }

            .textbox input {
            text-align: left;
            width: 290px;
            height: 34px;
            padding: 5px;
            }

            .form-horizontal .control-label {
            padding-top: 15px;
            }

            .block {
            border-radius: 5px;
            background-color: #a6dfc8;
            margin: 3px;
            }

            .block label {
            float: left;
            text-align: left;
            padding: 10px;
            width: auto;
            }

            .block small {
            color: #444444;
            }

            .left-indent {
            margin-left: 100px;
            }

            .cke_skin_kama .cke_editor {
            display: table !important;
            }

            input.dms_field {
            width: 19%;
            display: none;
            }

            .userOrEmail-list {
            clear:none;
            }
            #cke_description {
            width: 100%;
            min-width: 100%;
            max-width: 100%;
            }
        </style>


    </head>
    <body>
        <%
        def form_id = "createDataset"
        def form_action = uGroup.createLink(controller:'dataset', action:'save')
        def form_button_name = "Create Dataset"
        def form_button_val = "${g.message(code:'button.create.dataset')}"
        entityName="Create Dataset"	
        if(params.action == 'edit' || params.action == 'update'){
        //form_id = "updateGroup"
        form_action = uGroup.createLink(controller:'dataset', action:'update')
        form_button_name = "Update Dataset"
        form_button_val = "${g.message(code:'button.update.dataset')}"
        entityName = "Edit Dataset"
        }
        String uploadDir = "datasets/"+ UUID.randomUUID().toString()	

        %>
        <div class="observation_create">
            <div class="span12">
                <uGroup:showSubmenuTemplate  model="['entityName':entityName]"/>


                <g:hasErrors bean="${datasetInstance}">
                <i class="icon-warning-sign"></i>
                <span class="label label-important"> <g:message
                    code="fix.errors.before.proceeding" default="Fix errors" /> </span>
                </g:hasErrors>


                <form id="${form_id}" action="${form_action}" method="POST"  enctype="multipart/form-data"
                    class="form-horizontal">
                    <input type="hidden" name="id" value="${datasetInstance?.id}"/>
                    <div class="super-section">
                        <div class="section"
                            style="position: relative; overflow: visible;">
                            <div
                                class="row control-group left-indent ${hasErrors(bean: datasetInstance, field: 'title', 'error')}">

                                <label for="name" class="control-label"><g:message
                                    code="dataset.name.label" default="${g.message(code:'dataset.name.label')}" />*</label>
                                <div class="controls textbox">
                                    <div class="btn-group" style="z-index: 3;">
                                        <g:textField name="title" value="${datasetInstance?.title}" placeholder="${g.message(code:'button.create.dataset')}" />
                                        <div class="help-inline">
                                            <g:hasErrors bean="${datasetInstance}" field="title">
                                            <g:eachError bean="${datasetInstance}" field="title">
                                            <li><g:message error="${it}" /></li>
                                            </g:eachError>
                                            </g:hasErrors>
                                        </div>
                                    </div>
                                </div>
                            </div>


                            <div
                                class="row control-group left-indent ${hasErrors(bean: datasetInstance, field: 'description', 'error')}">
                                <label for="description" class="control-label"><g:message code="default.description.label" />*</label>
                                <div class="controls  textbox">

                                    <textarea id="description" name="description" placeholder="${g.message(code:'dataset.small.description')}">${datasetInstance?.description?.replaceAll('(?:\r\n|\r|\n)', '<br />')}</textarea>

                                    <script type='text/javascript'>
                                        CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );

                                        var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
CKEDITOR.replace('description', config);
</script>
<div class="help-inline">
    <g:hasErrors bean="${datasetInstance}" field="description">
    <g:eachError bean="${datasetInstance}" field="description">
    <li><g:message error="${it}" /></li>
    </g:eachError>
    </g:hasErrors>
</div>
                            </div>

                        </div>

                        <div
                            class="row control-group left-indent ${hasErrors(bean: datasetInstance, field: 'dataPackage', 'error')}">

                            <label for="dataPackage" class="control-label"><g:message
                                code="dataPackage.name.label" default="${g.message(code:'dataPackage.name.label')}" />*</label>
                            <div class="controls textbox">
                                <div class="btn-group" style="z-index: 3;">
                                    <g:select name="dataPackage"
                                    from="${DataPackage.list()}"
                                    noSelection="${['null':'Select One...']}"
                                    value="${dataPackage?:(datasetInstance?.dataPackage?.id)}"
                                    optionKey="id" optionValue="title"/>


                                    <div class="help-inline">
                                        <g:hasErrors bean="${datasetInstance}" field="dataPackage">
                                        <g:eachError bean="${datasetInstance}" field="dataPackage">
                                        <li><g:message error="${it}" /></li>
                                        </g:eachError>
                                        </g:hasErrors>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div
                            class="row control-group left-indent ${hasErrors(bean: datasetInstance, field: 'source', 'error')}">

                            <label for="source" class="control-label"><g:message
                                code="default.externalId.label" default="${g.message(code:'default.externalId.label')}" /></label>
                            <div class="controls textbox">
                                <div class="btn-group" style="z-index: 3;">
                                    <g:textField name="externalUrl" value="${datasetInstance?.externalUrl}" placeholder="Add external url for the dataPackage" />
                                    <div class="help-inline">
                                        <g:hasErrors bean="${datasetInstance}" field="externalUrl">
                                        <g:eachError bean="${datasetInstance}" field="externalUrl">
                                        <li><g:message error="${it}" /></li>
                                        </g:eachError>
                                        </g:hasErrors>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div
                            class="row control-group left-indent ${hasErrors(bean: datasetInstance, field: 'attribution', 'error')}">

                            <label for="attribution" class="control-label"><g:message
                                code="default.attribution.label" default="${g.message(code:'default.attribution.label')}" />*</label>
                            <div class="controls textbox">
                                <div class="btn-group" style="z-index: 3;">
                                    <textarea class="input-block-level" name="attribution" placeholder="${g.message(code:'checklist.details.enter.attribution')}">${datasetInstance?.attribution}</textarea>
                                    <div class="help-inline">
                                        <g:hasErrors bean="${datasetInstance}" field="attribution">
                                        <g:eachError bean="${datasetInstance}" field="attribution">
                                        <li><g:message error="${it}" /></li>
                                        </g:eachError>
                                        </g:hasErrors>
                                    </div>
                                </div>
                            </div>
                        </div>



                        <div
                            class="row control-group left-indent ${hasErrors(bean: datasetInstance, field: 'uFile', 'error')}">
                            <label class="control-label" for="file"><g:message code="default.resource.label" /> <span
                                    class="req">*</span></label>
                            <div class="controls" style="">

                                <%def allowedExtensions="['csv', 'tsv', 'xlx', 'xlsx','zip']"%>
                                <g:render template='/UFile/docUpload'
                                model="['name': 'ufilepath', 'path': datasetInstance?.uFile?.path, 'size':datasetInstance?.uFile?.size,'fileParams':['uploadDir':uploadDir, 'retainOriginalFileName':true], uploadCallBack:'dataset_upload_callback()', 'allowedExtensions':allowedExtensions, retainOriginalFileName:true]" />
                                <% def upload_file_text="${g.message(code:'default.upload.file.label')}"
                                %>
                                <script type="text/javascript">

                                    $(document).ready(function(){
                                            $('.qq-upload-button').contents().first()[0].textContent = '${upload_file_text}';
                                            });

                                        </script>

                                        <g:message code="loginformtemplate.or" />
                                        <input type="text" id="uFilePath" class="input-block-level" name="path"
                                        placeholder="${g.message(code:'placeholder.document.enter.url')}"
                                        value="${datasetInstance?.uFile?.path}" />
                                        <input id="multimediaFile" type="text" name="multimediaFile" value="${multimediaFile}" placeholder="Enter media file">
                                        <input id="multimediaFileUpload" type="file" name="multimediaFileUpload" placeholder="Enter media file">

                            </div>
                        </div>

                        <div
                            class="row control-group left-indent">
                            <label class="control-label" for="file">Mapping File</label>
                            <div class="controls" style="">
                                <input id="mappingFile" type="text" name="mappingFile" value="${mappingFile}" placeholder="Enter mapping file">
                                <input id="mappingFileUpload" type="file" name="mappingFileUpload" placeholder="Enter mapping file">
                                <input id="multimediaMappingFile" type="text" name="multimediaMappingFile" value="${multimediaMappingFile}" placeholder="Enter multimedia mapping file">
                                <input id="multimediaMappingFileUpload" type="file" name="multimediaMappingFileUpload" placeholder="Enter multimedia mapping file">

                            </div>
                        </div>



                    </div>
                </div>

                           <div class="" style="margin-top: 20px; margin-bottom: 40px;">

                               <g:if test="${datasetInstance?.id}">
                               <a href="${createLink(controller:'dataPackage', action:'show', id:datasetInstance.dataPackage.id)}" class="btn"
                                   style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
                               </g:if>
                               <g:else>
                               <a href="${createLink(controller:'dataPackage', action:'list')}" class="btn"
                                   style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
                               </g:else>

                               <g:if test="${datasetInstance?.id}">
                               <div class="btn btn-danger"
                                   style="float: right; margin-right: 5px;">
                                <a
                                    href="${createLink(mapping:'dataset', action:'delete', id:datasetInstance?.id)}"
                                    onclick="return confirm('${message(code: 'default.delete.confirm.message', args:['dataset'])}');"><g:message code="button.delete.dataset" /></a>
                            </div>
                            </g:if>
                            <a id="createDatasetSubmit"
                                class="btn btn-primary" style="float: right; margin-right: 5px;">
                                ${form_button_val} </a>
                            <span class="policy-text"> <g:message code="default.create.submitting.for.new" args="['dataset']"/> <a href="/terms"><g:message code="link.terms.conditions" /></a> <g:message code="register.index.use.of.site" /> </span>
                        </div>

                    </form>

                </div>

            </div>
        </div>

    </div>

    <asset:script>
    function dataset_upload_callback() {
        $('#uFilePath').val('');        
        $('#mappingFile, #multimediaFile, #multimediaMappingFile').val('');
    }

    $('#mappingFileUpload, #multimediaFileUpload, #multimediaMappingFileUpload').change(function(e) {
        $('#mappingFile, #multimediaFile, #multimediaMappingFile').val('');
    });

    $(document).ready(function() {	
    $("#createDatasetSubmit").click(function(){
    $("#${form_id}").submit();
    return false;
    });
    });
    </asset:script>

</body>

</html>
