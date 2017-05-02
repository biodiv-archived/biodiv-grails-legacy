<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'traits.label')}"/>
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
        def form_id = "createTraits"
        def form_action = uGroup.createLink(controller:'trait', action:'upload')
        def form_button_name = "Create Traits"
        def form_button_val = "${g.message(code:'button.create.traits')}"
        entityName="Upload Traits"
        if(params.action == 'edit' || params.action == 'update'){
        //form_id = "updateGroup"
        form_action = uGroup.createLink(controller:'trait', action:'upload')
        form_button_name = "Update Traits"
        form_button_val = "${g.message(code:'button.update.traits')}"
        entityName = "Edit Traits"
        }
        String uploadDir = "trait/"+ UUID.randomUUID().toString()	

        %>
        <div class="observation_create">
            <div class="span12">
                <g:render template="/trait/addTraitMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Trait':'Upload Traits']"/>

                <form id="${form_id}" class="super-section form-horizontal" action="${form_action}" method="POST"  enctype="multipart/form-data"
                    class="form-horizontal">
                    <div class="section">
                        <div class="row control-group left-indent">
                            <label class="control-label" for="file">
                                <g:message code="default.trait.definition.label" /> 
                                <span class="req">*</span>
                            </label>
                            <div class="controls" style="">
                                <%def allowedExtensions="['tsv','csv','txt']"%>
                                <g:render template='/UFile/docUpload'
                                model="['name': 'traitsPath', 'inputName': 'tFile', 'path': tFile?.path, 'size':tFile?.size,'fileParams':['uploadDir':uploadDir, 'retainOriginalFileName':true], uploadCallBack:'traits_upload_callback()', 'allowedExtensions':allowedExtensions, retainOriginalFileName:true]" />
                                <small class="help-block"><g:message code="default.trait.definition.fileFormat" /> </small>
                            </div>
                        </div>


                        <div class="row control-group left-indent">
                            <label class="control-label" for="file">
                                <g:message code="default.trait.values.label" /> 
                                <span class="req">*</span>
                            </label>
                            <div class="controls" style="">
                                <g:render template='/UFile/docUpload'
                                model="['name': 'traitValuesPath', 'inputName': 'tvFile', 'path': tvFile?.path, 'size':tvFile?tvFile.size:'','fileParams':['uploadDir':uploadDir, 'retainOriginalFileName':true], uploadCallBack:'traits_upload_callback()', 'allowedExtensions':allowedExtensions, retainOriginalFileName:true]" />
 
                                <small class="help-block"><g:message code="default.trait.values.fileFormat" /> </small>
                                <% def upload_file_text="${g.message(code:'default.upload.file.label')}"%>
                                <script type="text/javascript">
                                    $(document).ready(function(){
                                        $('.qq-upload-button').contents().first().textContent = '${upload_file_text}';
                                    });
                                </script>
                            </div>
                        </div>
                        </div>
                        <div class="" style="margin-top: 20px; margin-bottom: 40px;">

                            <a href="${createLink(controller:'trait', action:'list')}" class="btn"
                            style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>

                            <a id="createTraitsSubmit" class="btn btn-primary" style="float: right; margin-right: 5px;">
                                ${form_button_val} 
                            </a>
                            <span class="policy-text"> <g:message code="default.create.submitting.for.new" args="['facts']"/> <a href="/terms"><g:message code="link.terms.conditions" /></a> <g:message code="register.index.use.of.site" /> </span>
                        </div>

                    </form>
                </div>
            </div>


    <asset:script>
    function traits_upload_callback() {
        $('#uFilePath').val('');        
        $('#traitsPath').val('');        
    }


    $(document).ready(function() {	
        $("#createTraitsSubmit").click(function(){
            $("#${form_id}").submit();
            return false;
        });
    });
    </asset:script>

</body>

</html>
