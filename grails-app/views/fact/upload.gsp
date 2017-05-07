<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.fact.label')}"/>
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
            .help-block {
                height: 100px;
                overflow-y: scroll;
            }
        </style>


    </head>
    <body>
        <%
        def form_id = "createTraits"
        def form_action = uGroup.createLink(controller:'fact', action:'upload')
        def form_button_name = "Create Traits"
        def form_button_val = "${g.message(code:'button.create.facts')}"
        entityName="Upload Traits"
        if(params.action == 'edit' || params.action == 'update'){
        //form_id = "updateGroup"
        form_action = uGroup.createLink(controller:'fact', action:'upload')
        form_button_name = "Update Traits"
        form_button_val = "${g.message(code:'button.update.facts')}"
        entityName = "Edit Traits"
        }
        String uploadDir = "fact/"+ UUID.randomUUID().toString()	

        %>
        <div class="observation_create">
            <div class="span12">
                <g:render template="/fact/addFactMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Trait':'Upload Traits']"/>

                <form id="${form_id}" class="super-section form-horizontal" action="${form_action}" method="POST"  enctype="multipart/form-data"
                    class="form-horizontal">
                    <div class="section">
                        <div class="row control-group left-indent">
                            <label class="control-label" for="file">
                                <g:message code="default.fact.label" /> 
                                <span class="req">*</span>
                            </label>
                            <div class="controls" style="">
                                <%def allowedExtensions="['xls','xlsx']"%>
                                <g:render template='/UFile/docUpload'
                                    model="['name': 'factsPath', 'inputName': 'fFile', 'path': fFile?.path, 'size':fFile?.size,'fileParams':['uploadDir':uploadDir, 'retainOriginalFileName':true], uploadCallBack:'facts_upload_callback()', 'allowedExtensions':allowedExtensions, retainOriginalFileName:true]" />
                                <g:if test="${fFile.errors}"> 
                                <div class="help-inline alert-danger">
                                    <g:each in="${fFile.errors}" var="err">
                                    ${err}<br/>
                                    </g:each>
                                </div>
                                </g:if>
    
                                <small class="help-block"><g:message code="default.fact.upload.fileFormat" />
                                <ul>
                                    <li><b>Taxon name</b>: The name of the species for which the trait applies</li>
                                    <li><b>TaxonId*</b>: The taxon ID for the species</li>
                                    <li><b>Attribution*</b>: Attribution for the fact</li>
                                    <li><b>Contributor*</b>: Email of the registered user who has provided the fact</li>
                                    <li><b>License*</b>: Creative Commons Licence for the fact (eg: BY)</li>
                                </ul>
                                </small>
                                <small>
                                <g:message code="default.fact.upload.samplefileFormat" /> 
                                <a href="${createLinkTo(dir: '/../static/templates/', file:'factTemplate_v1.xlsx' , base:grailsApplication.config.speciesPortal.resources.serverURL)}"><g:message code="msg.here" /></a>


                                </small>
                            </div>
                        </div>

                       </div>
                        <div class="" style="margin-top: 20px; margin-bottom: 40px;">

                            <a href="${createLink(controller:'trait', action:'list')}" class="btn"
                            style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>

                            <a id="createFactsSubmit" class="btn btn-primary" style="float: right; margin-right: 5px;">
                                ${form_button_val} 
                            </a>
                            <span class="policy-text"> <g:message code="default.create.submitting.for.new" args="['facts']"/> <a href="/terms"><g:message code="link.terms.conditions" /></a> <g:message code="register.index.use.of.site" /> </span>
                        </div>

                    </form>
                </div>
            </div>


    <asset:script>
    function facts_upload_callback() {
        $('#uFilePath').val('');        
        $('#factsPath').val('');        
    }


    $(document).ready(function() {	
        $("#createFactsSubmit").click(function(){
            $("#${form_id}").submit();
            return false;
        });
    });
    </asset:script>

</body>

</html>
