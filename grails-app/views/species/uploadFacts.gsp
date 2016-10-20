<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'facts.label')}"/>
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
        def form_id = "createFacts"
        def form_action = uGroup.createLink(controller:'species', action:'saveFacts')
        def form_button_name = "Create Facts"
        def form_button_val = "${g.message(code:'button.create.facts')}"
        entityName="Create Facts"	
        if(params.action == 'edit' || params.action == 'update'){
        //form_id = "updateGroup"
        form_action = uGroup.createLink(controller:'species', action:'uploadFacts')
        form_button_name = "Update Facts"
        form_button_val = "${g.message(code:'button.update.facts')}"
        entityName = "Edit Facts"
        }
        String uploadDir = "facts/"+ UUID.randomUUID().toString()	

        %>
        <div class="observation_create">
            <div class="span12">
                <uGroup:showSubmenuTemplate  model="['entityName':entityName]"/>


                <form id="${form_id}" action="${form_action}" method="POST"  enctype="multipart/form-data"
                    class="form-horizontal">

                        <div
                            class="row control-group left-indent ">
                            <label class="control-label" for="file"><g:message code="default.resource.label" /> <span
                                    class="req">*</span></label>
                            <div class="controls" style="">

                                <%def allowedExtensions="['tsv']"%>
                                <g:render template='/UFile/docUpload'
                                model="['name': 'ufilepath', 'path': uFile?.path, 'size':uFile?.size,'fileParams':['uploadDir':uploadDir, 'retainOriginalFileName':true], uploadCallBack:'facts_upload_callback()', 'allowedExtensions':allowedExtensions, retainOriginalFileName:true]" />
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
                                        value="${uFile?.path}" />

                            </div>
                        </div>
                    </div>
                </div>

                           <div class="" style="margin-top: 20px; margin-bottom: 40px;">

                               <a href="${createLink(controller:'species', action:'list')}" class="btn"
                                   style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>

                            <a id="createFactsSubmit"
                                class="btn btn-primary" style="float: right; margin-right: 5px;">
                                ${form_button_val} </a>
                            <span class="policy-text"> <g:message code="default.create.submitting.for.new" args="['facts']"/> <a href="/terms"><g:message code="link.terms.conditions" /></a> <g:message code="register.index.use.of.site" /> </span>
                        </div>

                    </form>

                </div>

            </div>
        </div>

    </div>

    <asset:script>
    function facts_upload_callback() {
        $('#uFilePath').val('');        
        $('#mappingFile, #multimediaFile, #multimediaMappingFile').val('');
    }

    $('#mappingFileUpload, #multimediaFileUpload, #multimediaMappingFileUpload').change(function(e) {
        $('#mappingFile, #multimediaFile, #multimediaMappingFile').val('');
    });

    $(document).ready(function() {	
    $("#createFactsSubmit").click(function(){
    $("#${form_id}").submit();
    return false;
    });
    });
    </asset:script>

</body>

</html>
