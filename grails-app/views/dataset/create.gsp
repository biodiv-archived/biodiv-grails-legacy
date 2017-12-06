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
                                        onchange="dataPackageChanged(this.value);" />


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
                            <h3>Save Dataset As</h3>
                        	<g:set var="dataset_contributor_autofillUsersId" value="contributor_id" />
                            <g:render template="/dataset/collectionMetadataTemplate" model="['instance':datasetInstance, autofillUserComp:dataset_contributor_autofillUsersId]"/>
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

                    <!--div id="workspace" class="section" style="overflow:auto;background-color:white;border:solid 1px;display:none;">
                            <h3 style="color:lightgray;">Add Datatables</h3>
                            <div id="allowedDataTableTypes" class="span2" style="margin-left:0px;"></div>
                            <div id="addDataTable" class="span10"></div>
                    </div-->


                    </div>

            </div>
        </div>

    </div>

    <asset:script>
    $(document).ready(function() {	
        
        var dataset_contributor_autofillUsersComp = $("#userAndEmailList_${dataset_contributor_autofillUsersId}").autofillUsers({
            usersUrl : '${createLink(controller:'user', action: 'terms')}'
        });

        <g:if test="${datasetInstance.isAttached() }">
        if(dataset_contributor_autofillUsersComp.length > 0) {
            <%        def user = SUser.read(datasetInstance.party.contributorId);%>
                dataset_contributor_autofillUsersComp[0].addUserId({'item':{'userId':'${user.id}', 'value':'${user.name}'}});
                }
        </g:if>

        
        $("#createDatasetSubmit").click(function(){

            var speciesGroups = getSelectedGroupArr();

            $.each(speciesGroups, function(index, element){
                var input = $("<input>").attr("type", "hidden").attr("name", "group").val(element);
                $("#${form_id}").append($(input));	
            })


           var locationpicker = $(".map_class").data('locationpicker'); 
            if(locationpicker && locationpicker.mapLocationPicker.drawnItems) {
                var areas = locationpicker.mapLocationPicker.drawnItems.getLayers();
                if(areas.length > 0) {
                    var wkt = new Wkt.Wkt();
                    wkt.fromObject(areas[0]);
                    $("input.areas").val(wkt.write());
                }
            }

           
            for ( instance in CKEDITOR.instances ) {
                CKEDITOR.instances[instance].updateElement();
            }

		    $('input[name="contributorUserIds"]').val(dataset_contributor_autofillUsersComp[0].getEmailAndIdsList().join(","));

            $("#${form_id}").ajaxSubmit({ 
                dataType: 'json', 
                success: function(data, statusText, xhr) {
                   console.log(data);
                   if(data.success) {
                        $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
                        window.location.href = data.url;
                        $(".datasetEditSection").hide();
                        $(".datasetShowSection").slideDown();
                   } else {
                        $(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
                        $.each(data.errors, function(index, value) {
                             $("#${form_id}").find('[name='+value.field+']').parents(".control-group").addClass("error");
                             $("#${form_id}").find('[name='+value.field+']').nextAll('.help-inline').append("<li>"+value.message+"</li>")
                        });
                    }    
                }, error:function (xhr, ajaxOptions, thrownError){
                    //successHandler is used when ajax login succedes
                    var successHandler = this.success;
                    handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                        var response = $.parseJSON(xhr.responseText);
                        console.log(response);
                    });
                } 
            });	
        });
CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );
var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
CKEDITOR.replace('description', config);

        });

    </asset:script>

</body>

</html>
