<%@page import="species.utils.Utils"%>
<%@ page import="species.dataset.DataTable"%>
<%@ page import="species.dataset.DataPackage"%>
<%@ page import="species.participation.Checklists"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'dataTable.label')}"/>
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
            /*#cke_description {
            width: 100%;
            min-width: 100%;
            max-width: 100%;
            }*/
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
        <div class="row-fluid namelist-wrapper observation_create">
            <div class="span12">
                <uGroup:showSubmenuTemplate  model="['entityName':entityName]"/>

                   <div class="super-section">

                        <div id="dataTableShowSection" class="section">
                            <!--h3>Dataset</h3-->
                            <%--
                            <g:render template="/dataset/showDatasetStoryTemplate" model="['datasetInstance':dataTableInstance.dataset, showDetails:true, userLanguage:userLanguage]"/>
                            --%>
                        </div>

                        <!--div class="section">
                            <h3>Select Workflow</h3>
                            <div class="control-group ${hasErrors(bean: dataTableInstance, field: 'dataPackage', 'error')}">
                                <label for="dataPackage" class="control-label"><g:message code="dataPackage.name.label" default="${g.message(code:'dataPackage.name.label')}" />*</label>
                                <div class="controls textbox">
                                    <div class="btn-group" style="z-index: 3;">
                                        <g:select name="dataPackage" disabled="disabled"
                                        from="${DataPackage.list()}"
                                        noSelection="${['null':'Select One...']}"
                                        value="${dataPackage?:(dataTableInstance?.dataset?.dataPackage?.id)}"
                                        optionKey="id" optionValue="title"
                                        onchange="dataPackageChanged(this.value);" />


                                        <div class="help-inline">
                                            <g:hasErrors bean="${dataTableInstance?.dataset}" field="dataPackage">
                                            <g:eachError bean="${dataTableInstance?.dataset}" field="dataPackage">
                                            <li><g:message error="${it}" /></li>
                                            </g:eachError>
                                            </g:hasErrors>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div-->

                        <div class="section">
                            <h3>Add Datatables</h3>
                            <div id="workspace" style="overflow:auto;background-color:white;border:solid 1px;">
                                <div id="allowedDataTableTypes" class="span2" style="margin-left:0px;">
                                    <g:render template="/dataTable/selectDataTable" model="[dataTableTypes : dataTableInstance.dataset.dataPackage.allowedDataTableTypes(), datasetInstanceId:dataTableInstance.dataset.id]"/>
                                </div>
                                <div id="addDataTable" class="span10">
                                </div>
                            </div>
                        </div>


                   </div>

                    <!--div class="" style="margin-top: 20px; margin-bottom: 40px;">

                    <g:if test="${dataTableInstance?.id}">
                    <a href="${createLink(controller:'dataPackage', action:'show', id:dataTableInstance.dataPackage.id)}" class="btn"
                    style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
                    </g:if>
                    <g:else>
                    <a href="${createLink(controller:'dataPackage', action:'list')}" class="btn"
                    style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
                    </g:else>

                    <g:if test="${dataTableInstance?.id}">
                    <div class="btn btn-danger"
                    style="float: right; margin-right: 5px;">
                    <a
                    href="${createLink(mapping:'dataTable', action:'delete', id:dataTableInstance?.id)}"
                    onclick="return confirm('${message(code: 'default.delete.confirm.message', args:['dataTable'])}');"><g:message code="button.delete.dataTable" /></a>
                    </div>
                    </g:if>
                    <a id="createDataTableSubmit"
                    class="btn btn-primary" style="float: right; margin-right: 5px;">
                    ${form_button_val} </a>
                    <span class="policy-text"> <g:message code="default.create.submitting.for.new" args="['dataTable']"/> <a href="/terms"><g:message code="link.terms.conditions" /></a> <g:message code="register.index.use.of.site" /> </span>
                    </div-->

                    </div>

            </div>

            <script type='text/javascript'>
                CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );

                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
            </script>


    <asset:script>

    $(document).ready(function() {	
        });
    </asset:script>

</body>

</html>
