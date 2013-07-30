<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.utils.Utils"%>

<html>
<head>
<g:set var="title" value="Observations"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_create"/>
</head>
<body>

    <div class="observation_create">
        <div class="span12">

            <obv:showSubmenuTemplate model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Observation']"/>
            <g:render template="/observation/addObservationMenu"/>

            <%
            def form_id = "addObservation"
            def form_action = uGroup.createLink(action:'save', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            def form_button_name = "Add Observation"
            def form_button_val = "Add Observation"
            if(params.action == 'edit' || params.action == 'update'){
            //form_id = "updateObservation"
            form_action = uGroup.createLink(action:'update', controller:'observation', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            form_button_name = "Update Observation"
            form_button_val = "Update Observation"
            }

            %>

            <form id="${form_id}" action="${form_action}" method="POST"
                class="form-horizontal">

                <div class="span12 super-section">
                    <div class="section">
                        <h3>What did you observe?</h3>

                        <g:render template="addPhoto" model="['observationInstance':observationInstance]"/>

                        <div class="section" style="margin:0px;">
                            <g:if
                            test="${observationInstance?.fetchSpeciesCall() == 'Unknown'}">
                            <div id="help-identify" class="control-label">
                                <label class="checkbox" style="text-align: left;"> <input
                                    type="checkbox" name="help_identify" /> Help identify </label>
                            </div>
                            </g:if>
                            <reco:create />
                        </div>
                        <div class="section" style="margin:0px";>
                            <g:render template="selectGroupHabitatDate" model="['observationInstance':observationInstance]"/>
                        </div>

                        <div class="section" style="margin:0px";>
                            <%
                            def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                            %>
                            <obv:showMapInput model="[observationInstance:observationInstance, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
                        </div>
                    </div>
                </div>
                <div class="span12 super-section"  style="clear: both">
                    <g:render template="addNotes" model="['observationInstance':observationInstance]"/>
                </div>

                <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
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
            <%

            def obvTmpFileName = (observationInstance?.resource?.iterator()?.hasNext() ) ? (observationInstance.resource.iterator().next()?.fileName) : false 
            def obvDir = obvTmpFileName ?  obvTmpFileName.substring(0, obvTmpFileName.lastIndexOf("/")) : ""
            %>


            <form id="upload_resource" 
                title="Add a photo for this observation"
                method="post"
                class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">

                <span class="msg" style="float: right"></span>
                <input id="videoUrl" type="hidden" name='videoUrl'value="" />
                <input type="hidden" name='obvDir' value="${obvDir}" />
            </form>

        </div>
    </div>
</div>
<script type="text/javascript" src="//api.filepicker.io/v1/filepicker.js"></script>
<r:script>	
    var add_file_button = '<li id="add_file" class="addedResource" style="display:none;z-index:10;"><div id="add_file_container"><div id="add_image"></div><div id="add_video" class="editable"></div></div><div class="progress"><div id="translucent_box"></div><div id="progress_bar"></div ><div id="progress_msg"></div ></div></li>';



$(document).ready(function(){
     <%
           if(observationInstance?.group) {
           out << "jQuery('#group_${observationInstance.group.id}').addClass('active');";
           }
           if(observationInstance?.habitat) {
           out << "jQuery('#habitat_${observationInstance.habitat.id}').addClass('active');";
           }
    %>
    filepicker.setKey("${grailsApplication.config.speciesPortal.observations.filePicker.key}");
});


</r:script>

</body>
</html>
