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

            <g:render template="/observation/addObservationMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Observation']"/>

            <%
            def form_class = "addObservation"
            def form_action = uGroup.createLink(action:'save', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            def form_button_name = "Add Observation"
            def form_button_val = "Add Observation"
            if(params.action == 'edit' || params.action == 'update'){
            //form_class = "updateObservation"
            form_action = uGroup.createLink(action:'update', controller:'observation', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            form_button_name = "Update Observation"
            form_button_val = "Update Observation"
            }

            %>

            <form class="${form_class} form-horizontal" action="${form_action}" method="POST">
                <div class="span12 super-section">
                    <div class="section">
                        <h3>What did you observe?</h3>
                        <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'resourceListType':'ofObv']"></obv:addPhotoWrapper>
                         <div class="section" style="margin:0px;">
                            <g:render template="selectGroupHabitatDate" model="['observationInstance':observationInstance]"/>
                        </div>


                        <div class="section" style="margin:40px 0 0;">
                            <g:if
                            test="${observationInstance?.fetchSpeciesCall() == 'Unknown'}">
                            <div class="help-identify" class="control-label">
                                <label class="checkbox" style="text-align: left;"> <input
                                    type="checkbox" name="help_identify" /> Help identify </label>
                            </div>
                            </g:if>
                            <reco:create />
                        </div>
                        <div class="section" style="margin:0px;">
                            <g:render template="dateInput" model="['observationInstance':observationInstance]"/>
                            <%
                            def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                            %>
                            <div>
                            	<obv:showMapInput model="[observationInstance:obvInfoFeeder, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="span12 super-section"  style="clear: both">
                    <g:render template="addNotes" model="['observationInstance':observationInstance]"/>
                </div>

                <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
                <div class="span12 submitButtons">

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

                    <div class="control-group">
                        <label class="checkbox" style="text-align: left;"> 
                            <g:checkBox style="margin-left:0px;"
                            name="agreeTerms" value="${observationInstance?.agreeTerms}"/>
                            <span class="policy-text"> By submitting this form, you agree that the photos or videos you are submitting are taken by you, or you have permission of the copyright holder to upload them on creative commons licenses. </span></label>
                    </div>

                </div>
            </form>
            <%

            String obvTmpFileName = (observationInstance?.resource?.iterator()?.hasNext() ) ? (observationInstance.resource.iterator().next()?.fileName) : ''
            int index = obvTmpFileName.lastIndexOf("/");
            def obvDir = obvTmpFileName ?  obvTmpFileName.substring(0, index>0?index:obvTmpFileName.length()) : ""
            %>
            <form id="upload_resource" 
                title="Add a photo for this observation"
                method="post"
                class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">

                <span class="msg" style="float: right"></span>
                <input class="videoUrl" type="hidden" name='videoUrl' value="" />
                <input class="audioUrl" type="hidden" name='audioUrl' value="" />
                <input type="hidden" name='obvDir' value="${obvDir}" />
                <input type="hidden" name='resType' value='${observationInstance.class.name}'>
            </form>

        </div>
    </div>

<r:script>	
    var add_file_button = '<li class="add_file addedResource" style="display:none;z-index:10;"><div class="add_file_container"><div class="add_image"></div><div class="add_video editable"></div></div><div class="progress"><div class="translucent_box"></div><div class="progress_bar"></div ><div class="progress_msg"></div ></div></li>';



$(document).ready(function(){
    var uploadResource = new $.fn.components.UploadResource($('.observation_create'));
     <%
           if(observationInstance?.group) {
           out << "jQuery('#group_${observationInstance.group.id}').addClass('active');";
           }
           if(observationInstance?.habitat) {
           out << "jQuery('#habitat_${observationInstance.habitat.id}').addClass('active');";
           }
    %>
    initializeLanguage();
});


</r:script>

</body>
</html>
