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
        <%-- <style>--%>
            <%--    .cell-title {--%>
            <%--      font-weight: bold;--%>
            <%--    }--%>
            <%----%>
            <%--    .cell-effort-driven {--%>
            <%--      text-align: center;--%>
            <%--    }--%>
            <%--  </style>--%>
    </head>
    <body>
        <div class="observation_create">
            <div class="span12">
                <obv:showSubmenuTemplate model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Checklist':'Add Checklist']"/>

                <g:render template="/observation/addObservationMenu"/>
                <%
                def allowedExtensions = "['csv']"
				def fileParams = [uploadDir:'checklist']
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
               <form id="${form_id}" action="${form_action}" method="POST" class="form-horizontal">
                    <div class="span12 super-section">
                        <h3>What did you observe?</h3>

                        <div id="textAreaSection" class="section">
                            <i class="icon-picture"></i><span>
                                Put in a line for each species and any other information associated for it or 
                            </span>
                            <g:if test="${ params.action != 'create'}">
                                <g:render template="/checklist/showEditGrid" model="['observationInstance':observationInstance]"/>
                            </g:if>
                            <g:else>
                                <g:render template='/UFile/docUpload' model="['name': 'checklistStartFile', fileParams:fileParams, allowedExtensions:allowedExtensions,uploadCallBack:'showGrid()']" />
                            </g:else>

                            <div>
                                <input id="checklistColumns" name="checklistColumns" class="input-block-level" value='' placeHolder="Headers : Scientific Name, Common Name, ...."/>
                            </div>
                            <g:textArea id="checklistData" name="checklistData" rows="5" class="input-block-level" placeholder="Data : ..."/>
                            <input id="rawChecklist" name="rawChecklist" type="hidden" value='' />
                        </div>
                      
                        <div id="gridSection" class="section checklist-slickgrid" style="display:none;">
                            <span id="addNewColumn" class="btn-link">+ Add New Column</span>
                            <div id="myGrid" class="" style="width:100%;height:350px;"></div>
                            <div id="nameSuggestions" style="display: block;"></div>
                        
                            <div class="section ${hasErrors(bean: observationInstance, field: 'sciNameColumn', 'error')}" style="clear:both;">
                                <div class="row control-group span5">
                                    <label for="group" class="control-label"><g:message
                                        code="observation.mark.sciNameColumn.label" default="Mark Scientific Name Column" /> </label>
                                    <div class="controls">
                                        <select id="sciNameColumn" class="markColumn" name="sciNameColumn" value="${observationInstance.sciNameColumn}"></select>
                                        <div class="help-inline">
                                            <g:hasErrors bean="${observationInstance}" field="sciNameColumn">
                                            <g:message code="checklist.scientific_name.validator.invalid" />
                                            </g:hasErrors>
                                        </div>
                                    </div>
                                </div>	
                                <div class="row control-group span5">
                                    <label for="group" class="control-label"><g:message
                                        code="observation.mark.commonNameColumn.label" default="Mark Common Name Column" /> </label>
                                    <div class="controls">
                                        <select id="commonNameColumn" class="markColumn" name="commonNameColumn" value="${observationInstance.commonNameColumn}"></select>
                                    </div>
                                </div>	
                            </div>
                            <a id="parseNames" class="btn btn-primary"
                                style="float: right; margin-right: 5px;display:none;">Validate Names</a>
 
                        </div>
                    </div>

                    <div id="restOfForm" class="pull-left" style="${(params.action == 'create')?'display:none;':''}">
                        <div class="span12 super-section" style="clear:both">
                            <h3>What is this list about</h3>

                            <div class="section" style="clear:both;">
                                <g:render template="/observation/selectGroupHabitatDate" model="['observationInstance':observationInstance]"/>
                            </div>

                        </div>

                        <div class="span12 super-section" style="clear: both;">
                            <%
                            def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                            %>
                            <obv:showMapInput model="[observationInstance:observationInstance, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find these observations?']"></obv:showMapInput>
                        </div>

                        <div class="span12 super-section"  style="clear: both">
                            <g:render template="/observation/addNotes" model="['observationInstance':observationInstance]"/>
                        </div>


                        <div class="span12 super-section" style="clear:both">
                            <h3>Save this list as ...</h3>

                            <div class="section">
                                <g:render template="/checklist/details" model="['observationInstance':observationInstance]"/>
                            </div>
                        </div>

                        <g:render template="/observation/postToUserGroups" model="['observationInstance':observationInstance]"/>
                        <div class="span12" style="margin-top: 20px; margin-bottom: 40px;">

                            <g:if test="${observationInstance?.id}">
                            <a href="${uGroup.createLink(controller:params.controller, action:'show', id:observationInstance.id)}" class="btn"
                                style="float: right; margin-right: 30px;"> Cancel </a>
                            </g:if>
                            <g:else>
                            <a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
                                style="float: right; margin-right: 30px;"> Cancel </a>
                            </g:else>

                            <g:if test="${observationInstance?.id}">
                            <div class="btn btn-danger"
                                style="float: right; margin-right: 5px;">
                                <a
                                    href="${uGroup.createLink(controller:'checklist', action:'flagDeleted', id:observationInstance.id)}"
                                    onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');">Delete
                                    Checklist </a>
                            </div>
                            </g:if>

                            <a id="addObservationSubmit" class="btn btn-primary"
                                style="float: right; margin-right: 5px;"> ${form_button_val} </a>

                            <div class="row control-group">
                                <label class="checkbox" style="text-align: left;"> 
                                    <g:checkBox style="margin-left:0px;"
                                    name="agreeTerms" value="${observationInstance?.agreeTerms}"/>
                                    <span class="policy-text"> By submitting this form, you agree that the photos or videos you are submitting are taken by you, or you have permission of the copyright holder to upload them on creative commons licenses. </span></label>
                            </div>

                        </div>
                    </div>

                    <div id="wizardButtons" class="span12" style="margin-top: 20px; margin-bottom: 40px;${params.action=='create'?:'display:none;'}">
                        <a id="addNames" class="btn btn-primary"
                            style="float: right; margin-right: 5px;">Add names</a>
                        <a id="createChecklist" class="btn btn-primary"
                            style="float: right; margin-right: 5px;display:none;"> Create Checklist </a>
                   </div>

                </form>

            </div>
        </div>

<script type="text/javascript" src="//api.filepicker.io/v1/filepicker.js"></script>
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
            var data = []
            var columns = [{id: "sciName", name: "Scientific Name", field: "sciName", editor: AutoCompleteEditor, width:250, formatter:sciNameFormatter},
            {id: "commonName", name: "Common Name", field: "commonName", editor: AutoCompleteEditor, width:250},
            {id: "addMedia", name: "Add Media", field: "addMedia", width:100},
            {id: "notes", name: "Notes", field: "notes", editor: Slick.Editors.LongText, width:200}
            ]

//            initGrid(data, columns);
        });
        </r:script>

    </body>
</html>
