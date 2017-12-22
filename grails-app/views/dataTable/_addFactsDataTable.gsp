<div>
    <%
    def form_id = "addDataTable"
    def form_action = uGroup.createLink(action:'save', controller:'dataTable', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    def form_button_name = "Add ${dataTableInstance.dataTableType}"
    def form_button_val = "Add "+dataTableInstance.dataTableType; //"${g.message(code:'button.add.checklist')}"
    if(params.action == 'edit' || params.action == 'update'){
    form_action = uGroup.createLink(action:'update', controller:'dataTable', id:dataTableInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    form_button_name = "Update Checklist"
    form_button_val = "${g.message(code:'button.update.checklist')}"
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
                            %>
                            
                            <g:render template='/UFile/docUpload' model="['name': 'dataTableFile', 'path': dataTableInstance?.uFile?.path, 'size':dataTableInstance?.uFile?.size, fileParams:fileParams, allowedExtensions:allowedExtensions,uploadCallBack:'if(!responseJSON.success) {alert(responseJSON.msg);} else {}']" />
                                <div class="help-inline">
                                    <g:hasErrors bean="${dataTableInstance}" field="uFile">
                                    </g:hasErrors>
                                </div>
                                
                            </div>
                        </div>	
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
                <a id="createDataTableSubmit" class="btn btn-primary" style="float: right; margin-right: 5px;">
                ${form_button_val} </a>

                <g:checkBox style="margin-left:0px;" name="agreeTerms" value="${dataTableInstance?.agreeTerms}"/>
                                    <span class="policy-text"> <g:message code="checklist.create.submit.form" /></span></label>

            </div>
        </div>

    </form>

</div>
