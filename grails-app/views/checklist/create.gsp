<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.utils.ImageType"%>
<%@page	import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="java.util.Arrays"%>

<html>
    <head>
        <g:set var="title" value="Checklist"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="checklist_create"/>
        <uploader:head />
        <style>
            .upload_file div {
                display:inline-block;
            }
        </style>

    </head>
    <body>
        <div class="observation_create">
            <div class="span12">
                <g:render template="/observation/addObservationMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit List':'Add List']"/>

                <%
                def allowedExtensions = "['csv', 'xls', 'xlsx']"
                def fileParams = [uploadDir:'checklist',fileConvert:true, fromChecklist: true]
                def form_id = "addObservation"
                def form_action = uGroup.createLink(action:'save', controller:'checklist', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
                def form_button_name = "Add Checklist"
                def form_button_val = "Add Checklist"
                if(params.action == 'edit' || params.action == 'update'){
                form_action = uGroup.createLink(action:'update', controller:'checklist', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
                form_button_name = "Update Checklist"
                form_button_val = "Update Checklist"
                }

                %>
                <form id="${form_id}" action="${form_action}" method="POST" class="form-horizontal ${form_id}">
                    <div class="span12 super-section">
                        <h3><g:message code="checklist.create.what.observe" /></h3>

                        <div id="textAreaSection" class="section ${params.action != 'create'?'hide':''}">
                            <div>
                                <g:if test="${ params.action != 'create'}">
                                <g:render template="/checklist/showEditGrid" model="['observationInstance':observationInstance, 'checklistData':checklistData, 'checklistColumns':checklistColumns, 'sciNameColumn':sciNameColumn, 'commonNameColumn':commonNameColumn]"/>
                                </g:if>
                                
                                <g:else>

                                </g:else>
                            </div>

                            <div class="tabbable checklist-tabs">
                                <ul class="nav nav-tabs" id="checklist-tabs" style="margin:0px;background-color:transparent;">
                                    <li id ="tab_grid"class="active"><a href="#tab0" class="btn" data-toggle="tab"><g:message code="checklist.create.spreadsheet" /></a></li>
                                    <li id ="tab_up_file"><a href="#tab1" class="btn" data-toggle="tab"><g:message code="checklist.create.upload.file" /></a></li>
                                    <li id ="tab_type_list"><a href="#tab2" class="btn" data-toggle="tab"><g:message code="checklist.create.text.area" /></a></li>
                                </ul>

                                <div class="tab-content ">
                                    <div class="tab-pane active" id="tab0">
                                            
                                    </div>

                                    <div class="tab-pane " id="tab1">

                                        <div class="upload_file" style="display:inline-block">
                                            <g:render template='/UFile/docUpload' model="['name': 'checklistStartFile', fileParams:fileParams, allowedExtensions:allowedExtensions,uploadCallBack:'showGrid()']" />
                                        </div>
                                    </div>
                                    <div class="tab-pane " id="tab2">

                                        <div
                                            class="row control-group ${hasErrors(bean: observationInstance, field: 'checklistColumns', 'error')}">
                                            <label for="checklistColumns" class="control-label"><g:message
                                                code="checklist.checklistColumns.label" default="Headers" />
                                            </label>
                                            <div class="controls">
                                                <input id="checklistColumns" name="checklistColumns" class="input-block-level" value='' placeholder="${g.message(code:'placeholder.create.checklist')}"                                                title="${g.message(code:'checklist.create.enter.column.headers')}"/>
                                                <small class="help-inline">
                                                    <g:message code="checklist.create.enter.column.headers" />
                                                </small> 

                                            </div>
                                        </div>
                                        <div
                                            class="row control-group ${hasErrors(bean: observationInstance, field: 'checklistData', 'error')}">
                                            <label for="checklistData" class="control-label"><g:message
                                                code="checklist.checklistData.label" default="Data" /></label>
                                            <div class="controls">
                                                <input type=textArea id="checklistData" name="checklistData" rows="5" class="input-block-level" placeholder="${g.message(code:'placeholder.checklist.create')}" title="${g.message(code:'checklist.create.enter.one.line')}" />
                                                <small class="help-inline"> <g:message code="checklist.create.enter.one.line" /> </small> 
                                                <input id="rawChecklist" name="rawChecklist" type="hidden" value='' />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div id="gridSection" class="section checklist-slickgrid ${params.action=='create'?'hide':''}">
                            <span id="addNewColumn" class="btn-link"><g:message code="checklist.create.add.new.column" /></span>
                            <span class="help-inline"> <g:message code="checklist.create.mark.name" /> <img src="${resource(dir:'images', file:'dropdown_active.gif',absolute:'true')}"/>)</span>
                            
                            <div id="myGrid" class="" style="width:100%;height:350px;"></div>
                            <div id="nameSuggestions" style="display: block;"></div>
                            <div id="legend" class="hide">
                                <span class="incorrectName badge"><g:message code="error.incorrect.names" /></span>
                            </div>

                            <div class="section" style="clear:both;margin:0;">
                                <div class="row control-group ${hasErrors(bean: observationInstance, field: 'sciNameColumn', 'error')}">
                                    <!--span class="pull-left span3"><g:message
                                        code="observation.mark.sciNameColumn.label" default="Marked Scientific & Common Name Columns:" /></span-->
                                    <div class="controls">
                                        <input type="hidden" id="sciNameColumn" class="markColumn" name="sciNameColumn" value="${observationInstance.sciNameColumn}"/>
                                        <input type="hidden" id="commonNameColumn" class="markColumn" name="commonNameColumn" value="${observationInstance.commonNameColumn}"/>
                                        <div class="help-inline">
                                            <g:hasErrors bean="${observationInstance}" field="sciNameColumn">
                                            <g:message code="checklist.scientific_name.validator.invalid" />
                                            </g:hasErrors>
                                        </div>
                                      
                                    </div>
   
                                </div>	
                            </div> 
                                <a id="parseNames" class="btn btn-primary"
                                            style="float: right; margin: 5px;display:none;"><g:message code="button.validate.names" /></a>
         
                        </div>
                    </div>

                <div id="restOfForm" class="pull-left" style="${(params.action == 'create')?'display:none;':''}">
                    <div class="span12 super-section" style="clear:both">
                            <h3><g:message code="checklist.create.what.list" /></h3>

                            <div class="section" style="clear:both;">
                                <g:render template="/observation/selectGroupHabitatDate" model="['observationInstance':observationInstance]"/>
                            </div>
                            <div class="section" style="clear:both;">
                            <g:render template="/observation/dateInput" model="['observationInstance':observationInstance]"/>
                            <%
                            def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                            %>
                            <obv:showMapInput model="[observationInstance:observationInstance, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find these observations?']"></obv:showMapInput>
                            </div>
                        </div>

                        <div class="span12 super-section"  style="clear: both">
                            <g:render template="/observation/addNotes" model="['observationInstance':observationInstance]"/>
                        </div>


                        <div class="span12 super-section" style="clear:both">
                            <h3><g:message code="checklist.create.save.list" /></h3>

                            <div class="section">
                                <g:render template="/checklist/details" model="['observationInstance':observationInstance]"/>
                            </div>
                        </div>

                        <g:render template="/observation/postToUserGroups" model="['observationInstance':observationInstance]"/>
                        <div class="span12 submitButtons">

                            <g:if test="${observationInstance?.id}">
                            <a href="${uGroup.createLink(controller:params.controller, action:'show', id:observationInstance.id)}" class="btn"
                                style="float: right; margin-right: 30px;"><g:message code="button.cancel" />  </a>
                            </g:if>
                            <g:else>
                            <a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
                                style="float: right; margin-right: 30px;"><g:message code="button.cancel" />  </a>
                            </g:else>

                            <g:if test="${observationInstance?.id}">
                            <div class="btn btn-danger"
                                style="float: right; margin-right: 5px;">
                                <a
                                    href="${uGroup.createLink(controller:'checklist', action:'flagDeleted', id:observationInstance.id)}"
                                    onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');"><g:message code="button.delete.checklist" /> </a>
                            </div>
                            </g:if>

                            <a id="addObservationSubmit" class="btn btn-primary"
                                style="float: right; margin-right: 5px;"> ${form_button_val} </a>

                            <div class="control-group">
                                <label class="checkbox" style="text-align: left;"> 
                                    <g:checkBox style="margin-left:0px;"
                                    name="agreeTerms" value="${observationInstance?.agreeTerms}"/>
                                    <span class="policy-text"> <g:message code="checklist.create.submit.form" /></span></label>
                            </div>

                        </div>
                    </div>

                    <div id="wizardButtons" class="span12" style="margin-top: 20px; margin-bottom: 40px;${params.action=='create'?:'display:none;'}">
                        <a id="addNames" class="btn btn-primary"
                            style="float: right; margin-right: 5px;"><g:message code="button.load.list" /></a>
                        <a id="createChecklist" class="btn btn-primary"
                            style="float: right; margin-right: 5px;display:none;"> <g:message code="button.create.checkist" /> </a>
                   </div>

                </form>
                
                <g:render template="/checklist/addPhoto" model="['observationInstance':observationInstance, 'resourceListType':'ofChecklist']"/>
                <form id="upload_resource" 
                    title="${g.message(code:'title.checklist.create')}"
                    method="post"
                    class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">

                    <span class="msg" style="float: right"></span>
                    <input id="videoUrl" type="hidden" name='videoUrl'value="" />
                    <input type="hidden" name='obvDir' value="${obvDir}" />
                    <input type="hidden" name='resType' value='${observationInstance.class.name}'>
                </form>


            </div>
        </div>

        <r:script>
        $(document).ready(function(){
                        <%
            if(observationInstance?.group) {
            out << "jQuery('#group_${observationInstance.group.id}').addClass('active');";
            }
            if(observationInstance?.habitat) {
            out << "jQuery('#habitat_${observationInstance.habitat.id}').addClass('active');";
            }
            %>
            function initBlankSpreadsheet() {
            
            var rowDataForBlankSheet = new Array();
            for (var i =0 ; i<10 ; i++ ){
                rowDataForBlankSheet.push({S_No:"",Scientific_Name:"",Common_Name:""});
            }

            var columnDataForBlankSheet = [{id: "S_No", name:"S_No", field:"S_No",editor: Slick.Editors.Text, width:50},
                {id: "Scientific_Name", name: "Scientific_Name", field: "Scientific_Name",editor: Slick.Editors.Text,  width:150, header:getHeaderMenuOptions()},
                {id: "Common_Name", name: "Common_Name", field: "Common_Name",editor: Slick.Editors.Text,  width:150, header:getHeaderMenuOptions()}
                ]

            columnDataForBlankSheet.push(getMediaColumnOptions());
            loadDataToGrid(rowDataForBlankSheet, columnDataForBlankSheet, "checklist", "Scientific_Name", "Common_Name"); 
            }

            if(${params.action=="create"}){
                initBlankSpreadsheet();
                }

            $("#textAreaSection").show();
            $("#parseNames").click(function(){
                $("#textAreaSection").hide();
            });
            $('#addResourcesModal').modal({show:false});
            $("#tab_grid").click(function(){
                $("#gridSection").show();
                $("#addNames").hide();
            });
            $("#tab_up_file").click(function(){
                $("#gridSection").hide();
                $("#addNames").hide();
            });
            $("#tab_type_list").click(function(){
                $("#gridSection").hide();
                $('#checklistColumns').val('');
                $("#checklistData").val('');
                $("#addNames").show();
            });
            var uploadResource = new $.fn.components.UploadResource($('.observation_create'));
        });
        </r:script>

    </body>
</html>
