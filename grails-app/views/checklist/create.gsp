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
                <obv:showSubmenuTemplate model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Observation']"/>
                <%
                def allowedExtensions = "['csv']"
                %>
                <g:render template='/UFile/docUpload' model="['name': 'checklistStartFile', allowedExtensions:allowedExtensions,uploadCallBack:'showGrid()']" />
                <%
                def form_id = "addChecklist"
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

                        <div class="section checklist-slickgrid">
                            <div id="myGrid" style="width:620px;height:350px;display:none;"></div>
                            <input id="checklistData" name="checklistData" type="hidden" value='' />
                            <input id="checklistColumns" name="checklistColumns" type="hidden" value='' />
                        </div>

                        <div class="section">
                            <g:render template="/observation/selectGroupHabitatDate" model="['observationInstance':observationInstance]"/>
                        </div>
                    </div>
                    <div class="span12 super-section" style="clear: both;">
                        <%
                        def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                        %>
                        <obv:showMapInput model="[observationInstance:observationInstance, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
                    </div>
                    <div class="span12 super-section"  style="clear: both">
                        <g:render template="/observation/addNotes" model="['observationInstance':observationInstance]"/>
                    </div>
                    <g:render template="/observation/postToUserGroups" model="['observationInstance':obervationInstance]"/>
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
                                href="${uGroup.createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}"
                                onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');">Delete
                                Observation </a>
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

                </form>

            </div>
        </div>

        <r:script>
        $(document).ready(function(){
                $("#addObservationSubmit").click(function(event){
                $("#checklistColumns").val(JSON.stringify(grid.getColumns()))
                $("#checklistData").val(JSON.stringify(grid.getData()))
                $("#addChecklist").submit();  
            });
        });


        </r:script>
    </body>
</html>
