<%@page import="species.utils.Utils"%>
<%@ page import="species.dataset.Dataset"%>
<%@ page import="species.dataset.DataPackage"%>
<%@ page import="species.participation.Checklists"%>
<%@ page import="species.auth.SUser"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'dataset.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style>
            textarea {
            max-width:100%;
            min-width:100%;
            }

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
            width: 99%;
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

            span.cke_skin_kama {
                border : 1px solid #D3D3D3 !important;
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
            width: 98%;
            min-width: 98%;
            max-width: 98%;
            }
            .section {
                border:solid 1px lightgrey;
            }
            select {
                height:46px;
            }
            .upload_file div {
                display:inline-block;
            }
            .mapColumns.divider {
                height:20px;
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
        <div class="row-fluid namelist-wrapper observation_create">
            <div class="span12">
                <uGroup:showSubmenuTemplate  model="['entityName':entityName]"/>


                <g:hasErrors bean="${datasetInstance}">
                <i class="icon-warning-sign"></i>
                <span class="label label-important"> <g:message
                    code="fix.errors.before.proceeding" default="Fix errors" /> </span>
                </g:hasErrors>


                <form id="${form_id}" action="${form_action}" method="POST"
                    class="form-horizontal">
                    <input type="hidden" name="id" value="${datasetInstance?.id}"/>
                    <div class="super-section">

                        <div class="section">
                            <h3>Select Workflow</h3>
                            <div class="control-group ${hasErrors(bean: datasetInstance, field: 'dataPackage', 'error')}">
                                <label for="dataPackage" class="control-label"><g:message code="dataPackage.name.label" default="${g.message(code:'dataPackage.name.label')}" />*</label>
                                <div class="controls textbox">
                                    <div class="btn-group" style="z-index: 3;">
                                        <g:select name="dataPackage"
                                        from="${DataPackage.list()}"
                                        noSelection="${['null':'Select One...']}"
                                        value="${dataPackage?:(datasetInstance?.dataPackage?.id)}"
                                        optionKey="id" optionValue="title"
                                        onchange="dataPackageChangedForDataset(event, this.value);" />


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
                        </div>
                        <div id="datasetEditSection" class="section">
                                <g:set var="dataset_contributor_autofillUsersId" value="contributor_id" />
                                <g:render template="/dataset/collectionMetadataTemplate" model="['instance':datasetInstance, 'autofillUserComp':dataset_contributor_autofillUsersId]"/>
                        </div>
                   </div>

                    <div class="" style="margin-top: 20px; margin-bottom: 40px;">

                    <g:if test="${datasetInstance?.id}">
                    <a href="${createLink(controller:'dataPacakage', action:'show', id:datasetInstance.dataPackage.id)}" class="btn"
                    style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
                    </g:if>
                    <g:else>
                    <a href="${createLink(controller:'dataPacakage', action:'list')}" class="btn"
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
        CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );
 
        $(document).ready(function() {	
            dataset_contributor_autofillUsersComp = $("#userAndEmailList_contributor_id").autofillUsers({
                        usersUrl : window.params.userTermsUrl
            });
        
            <g:if test="${datasetInstance.isAttached() }">
            if(dataset_contributor_autofillUsersComp.length > 0) {
                <%        def user = SUser.read(datasetInstance.party.contributorId);%>
                dataset_contributor_autofillUsersComp[0].addUserId({'item':{'userId':'${user.id}', 'value':'${user.name}'}});
            }
            </g:if>

            
                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
                CKEDITOR.replace('summary', config);
                CKEDITOR.replace('description', config);
        });

    </asset:script>

</body>

</html>
