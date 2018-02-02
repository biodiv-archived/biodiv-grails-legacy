<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'link.upload.trait')}"/>
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
        def form_action = uGroup.createLink(controller:'trait', action:'upload')
        def form_button_name = "${g.message(code:'button.create.traitvalues')}"
        def form_button_val = "${g.message(code:'button.create.traitvalues')}"
        entityName="Upload Trait/Values"
        if(params.action == 'edit' || params.action == 'update'){
        //form_id = "updateGroup"
        form_action = uGroup.createLink(controller:'trait', action:'upload')
        form_button_name = "${g.message(code:'button.create.traitvalues')}"
        form_button_val = "${g.message(code:'button.update.traits')}"
        entityName = "Edit Traits"
        }
        String uploadDir = "trait/"+ UUID.randomUUID().toString()	
        String iconsUploadDir = uploadDir+"/icons"	

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
                                <%def allowedExtensions="['xls','xlsx']"%>
                                <g:render template='/UFile/docUpload'
                                model="['name': 'traitsPath', 'inputName': 'tFile', 'path': tFile?.path, 'size':tFile?.size,'fileParams':['uploadDir':uploadDir, 'retainOriginalFileName':true], uploadCallBack:'traits_upload_callback()', 'allowedExtensions':allowedExtensions, retainOriginalFileName:true]" />
                                <g:if test="${tFile.errors}"> 
                                <div class="help-inline alert-danger">
                                <g:each in="${tFile.errors}" var="err">
                                ${err}<br/>
                                </g:each>
                                </div>
                                </g:if> 
                                <small class="help-block">
                                <g:message code="default.trait.upload.fileFormat" /> 
                                <ul>
                                <li><b>Trait*</b>: Name of the trait</li>
                                <li><b>TraitType*</b>: The categorical type of values that the trait can hold. Choose from Single categorical (one value), Multiple  categorical (multiple values) or range (a range of values)</li>
                                <li><b>DataType*</b>: The data type of the trait. Choose from String, Numeric, Boolean, Date or Color</li>
                                <li><b>Units</b>: The unit of the values</li>
                                <li><b>Trait source</b>: The source of the trait</li>
                                <li><b>Trait definition</b>: The definition of the trait</li>
                                <li><b>Trait Icon</b>: File name for the icon representing the trait  (files should be uploaded separately)</li>
                                <li><b>SPM*</b>: The Concept, catergory and subcatergory (if any) from the species profile model separated with the "|" symbol. Eg: Overview|Diagnostic|Description</li>
                                <li><b>Taxon name</b>: The highest level of taxa for which the trait applies . Ie: the taxonomic scope</li>
                                <li><b>TaxonID</b>: The taxon ID of the taxa</li>
                                <li><b>new/update</b>: Denotes whether the trait being uploaded is new or an update of an existing trait.</li>
                                <li><b>TraitID</b>: If updating an existing trait, mention the ID of the trait to be updated</li>
                                <li><b>isObvTrait</b>: Mark "True" if the trait can potentially be used within observations. (as opposed to IUCN status which is never an observed trait)</li>
                                <li><b>isParticipatory</b>: Mark as "True" If it is enabled in observations, and the trait should be available for participants other than the observer to edit.</li>
                                <li><b>showInObservation</b>: Mark "True" if the trait should be displayed for input during observation upload.</li>

                                </ul>
                               </small>
                               <small>
                                <g:message code="default.trait.upload.samplefileFormat" /> 
                                <a href="${createLinkTo(dir: '/../static/templates/', file:'traitTemplate_v1.tsv' , base:grailsApplication.config.speciesPortal.resources.serverURL)}"><g:message code="msg.here" /></a>
                                </small>
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
                            <g:if test="${tvFile.errors}"> 
                            <div class="help-inline alert-danger">
                                <g:each in="${tvFile.errors}" var="err">
                                ${err}<br/>
                                </g:each>
                            </div>
                            </g:if>
                                <small class="help-block">
                                <g:message code="default.trait.upload.fileFormat" /> 
                                <ul>
                                    <li><b>Value*</b>: The Name of the value</li>
                                    <li><b>Trait*</b>: The Trait name for which this value is applicable</li>
                                    <li><b>TraitID</b>: If appending to existing trait values, mention the ID of the Trait</li>
                                    <li><b>Value Icon</b>: File name for the icon representing the value (files should be uploaded separately)</li>
                                    <li><b>Value Definition</b>: Definition of the trait value</li>
                                    <li><b>Value Source</b>: Source of the trait value</li>
                                </ul>
                                </small><small>
                                <g:message code="default.trait.values.upload.samplefileFormat" /> 
                                <a href="${createLinkTo(dir: '/../static/templates/', file:'traitValuesTemplate_v1.tsv' , base:grailsApplication.config.speciesPortal.resources.serverURL)}"><g:message code="msg.here" /></a>

                                </small>
                            </div>
                        </div>

                        <%def iconsAllowedExtensions="['zip']"%>
                        <div class="row control-group left-indent">
                            <label class="control-label" for="file">
                                <g:message code="trait.icon.zip.label" /> 
                            </label>
                            <div class="controls" style="">
                                <g:render template='/UFile/docUpload'
                                model="['name': 'iconsPath', 'inputName': 'iconsFile', 'path': iconsFile?.path, 'size':iconsFile?.size,'fileParams':['uploadDir':iconsUploadDir, 'retainOriginalFileName':true], uploadCallBack:'traits_upload_callback()', 'allowedExtensions':iconsAllowedExtensions, retainOriginalFileName:true]" />
 
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
