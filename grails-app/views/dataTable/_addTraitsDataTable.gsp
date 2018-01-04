<div>
    <%
    def form_id = "addDataTable"
    def form_action = uGroup.createLink(action:'save', controller:'dataTable', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    def form_button_name = "Add ${dataTableInstance.dataTableType}"
    def form_button_val = "Add "+dataTableInstance.dataTableType; //"${g.message(code:'button.add.checklist')}"
    if(params.action == 'edit' || params.action == 'update'){
    form_action = uGroup.createLink(action:'update', controller:'dataTable', id:dataTableInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    form_button_name = "Update ${dataTableInstance.dataTableType}"
    form_button_val = "Update "+dataTableInstance.dataTableType;
    }

    %>
    <form action="${form_action}" method="POST" class="form-horizontal ${form_id}">

        <input type="hidden" name="id" value="${dataTableInstance?.id}"/>
        <input type="hidden" name="dataset" value="${dataTableInstance?.dataset?.id}"/>
        <g:set var="dataset_contributor_autofillUsersId" value="contributor_id" />
        <g:render template="/dataset/collectionMetadataTemplate" model="['instance':dataTableInstance, autofillUserComp:dataset_contributor_autofillUsersId]"/>
        <div class="section">
            <h3><g:message code="default.dataTable.label" /> </h3>
    
                <div class="upload_file" style="display:${dataTableInstance?.uFile?.path?'none':'inline-block'}">
                        <div class="control-group ${hasErrors(bean: dataTableInstance, field: 'uFile', 'errors')}">
                            <label class="control-label"for="docUpload">${dataTableInstance.dataTableType} File*</label>
                            <div class="controls">
                            <%

                                def allowedExtensions = "['xls', 'xlsx']"
                                String uploadDir="";
                                if(dataTableInstance && dataTableInstance.uFile?.path) {
                                    uploadDir = (new File(dataTableInstance.uFile.path)).getParent()
                                } else if(dataTableInstance.dataset && dataTableInstance.dataset.uFile){
                                    uploadDir = (new File(dataTableInstance.dataset.uFile.path)).getAbsolutePath()+'/'+ UUID.randomUUID().toString()
                                } else {
                                    uploadDir = 'dataTables'+'/'+ UUID.randomUUID().toString();
                                }
                                def fileParams = [uploadDir:uploadDir]
                                def iconsFileParams = [uploadDir:uploadDir+"/icons"]
                            %>
                            
                            <g:render template='/UFile/docUpload' model="['name': 'dataTableFile', , 'path': dataTableInstance?.uFile?.path, 'size':dataTableInstance?.uFile?.size, fileParams:fileParams, allowedExtensions:allowedExtensions,uploadCallBack:'if(!responseJSON.success) {alert(responseJSON.msg);} else {showSampleDataTable()}']" />
                                <div class="help-inline">
                                    <g:hasErrors bean="${dataTableInstance}" field="uFile">
                                    </g:hasErrors>
                                </div>
                                
                            </div>
                        </div>	
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


                        <div class="control-group">
                            <label class="control-label" for="file">
                                <g:message code="default.trait.values.label" /> 
                                <span class="req">*</span>
                            </label>
                            <div class="controls" style="">

                            <g:render template='/UFile/docUpload' model="['name': 'tvFile', 'inputName':'tvFile' , 'path': dataTableInstance?.traitValueFile?.path, 'size':dataTableInstance?.traitValueFile?.size, fileParams:fileParams, allowedExtensions:allowedExtensions,uploadCallBack:'if(!responseJSON.success) {alert(responseJSON.msg);} else {showSampleDataTable()}']" />
                            <g:if test="${dataTableInstance?.traitValueFile?.errors}"> 
                            <div class="help-inline alert-danger">
                                <g:each in="${dataTableInstance.traitValueFile.errors}" var="err">
                                ${err}<br/>
                                </g:each>
                            </div>
                            </g:if>
                           </div>
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

                        <%def iconsAllowedExtensions="['zip']"%>
                        <div class="control-group">
                            <label class="control-label" for="file">
                                <g:message code="trait.icon.zip.label" /> 
                            </label>
                            <div class="controls" style="">
                                <g:render template='/UFile/docUpload'
                                model="['name': 'iconsPath', 'inputName': 'iconsFile', 'path': iconsFile?.path, 'size':iconsFile?.size,'fileParams':iconsFileParams, uploadCallBack:'if(!responseJSON.success) {alert(responseJSON.msg);} else {showSampleDataTable()}', 'allowedExtensions':iconsAllowedExtensions, retainOriginalFileName:true]" />
 
                                <% def upload_file_text="${g.message(code:'default.upload.file.label')}"%>
                                <script type="text/javascript">
                                    $(document).ready(function(){
                                        $('.qq-upload-button').contents().first().textContent = '${upload_file_text}';
                                    });
                                </script>
                            </div>
                        </div>



                </div>
                <div id="gridSection" class="section" style="display:none; width:100%;margin-left:0px;">
                    <div id="myGrid" class=" ${hasErrors(bean: dataTableInstance, field: 'sciNameColumn', 'errors')}" style="width:100%;overflow-x:scroll"></div>
                    <div class="section" style="clear:both;margin:0;">
                        <div class="row control-group ${hasErrors(bean: dataTableInstance, field: 'sciNameColumn', 'errors')}">
                            <div class="controls" style="clear:both;margin:0;">
                                <input type="hidden" id="dataTableType" name="dataTableType" value="${dataTableInstance.dataTableType.ordinal()}"/>
                                <input type="hidden" id="dataTableFilePath" name="dataTableFilePath" value=""/>
                                <input type="hidden" id="speciesGroupTraits" name="speciesGroupTraits" value=""/>
                                <input type="hidden" id="columns" name="columns" value="${dataTableInstance?.columns}"/>
                                <div class="help-inline">
                                    <g:hasErrors bean="${dataTableInstance}" field="sciNameColumn">
                                    <g:message code="checklist.scientific_name.validator.invalid" />
                                    </g:hasErrors>
                                </div>
                                
                            </div>
                        </div>	

                    </div> 
                </div> 
        </div>
                            
        <div id="restOfForm">
            <div class="" style="margin-top: 20px; margin-bottom: 40px;">
                <g:if test="${dataTableInstance?.id}">
                <a href="${createLink(controller:'dataTable', action:'show', id:dataTableInstance.id)}" class="btn"
                style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
                </g:if>
                <g:else>
                <a href="${createLink(controller:'dataTable', action:'list')}" class="btn"
                style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
                </g:else>

                <g:if test="${dataTableInstance?.id}">
                <div class="btn btn-danger"
                style="float: right; margin-right: 5px;">
                <a
                href="${createLink(mapping:'dataTable', action:'delete', id:dataTableInstance?.id)}"
                onclick="return confirm('${message(code: 'default.delete.confirm.message', args:['dataset'])}');"><g:message code="button.delete.dataTable" /></a>
                </div>
                </g:if>
                <a id="createDataTableSubmit" class="btn btn-primary" style="float: right; margin-right: 5px;" ${params.action=='dataTableTypeChanged'?'disabled=true':''}>
                ${form_button_val} </a>

                <g:checkBox style="margin-left:0px;" name="agreeTerms" value="${dataTableInstance?.agreeTerms}"/>
                                    <span class="policy-text"> <g:message code="checklist.create.submit.form" /></span></label>

            </div>
        </div>

    </form>

</div>
