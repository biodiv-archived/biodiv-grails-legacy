<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.utils.Utils"%>

<html>
<head>
<g:set var="title" value="${g.message(code:'showusergroupsig.title.observations')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_create"/>
<style>
    .latlng{
        margin-left: 285px !important;
    }
</style>
</head>
<body>

    <div class="observation_create">
        <div class="span12">

            <g:render template="/observation/addObservationMenu" model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Observation']"/>

            <%
            def form_class = "addObservation"
            def form_action = uGroup.createLink(action:'save', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            def form_button_name = "Add Observation"
            def form_button_val = "${g.message(code:'link.add.observation')}"
            if(params.action == 'edit' || params.action == 'update'){
            //form_class = "updateObservation"
            form_action = uGroup.createLink(action:'update', controller:'observation', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            form_button_name = "Update Observation"
            form_button_val = "${g.message(code:'default.update.observation')}"
            }

            %>

            <form class="${form_class} form-horizontal" action="${form_action}" method="POST">
                <div class="span12 super-section">
                    <div class="section">
                        <h3><g:message code="checklist.create.what.observe" /> </h3>
                        <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'resourceListType':'ofObv']"></obv:addPhotoWrapper>
                         <div class="section" style="margin:0px;">
                            <g:render template="selectGroupHabitatDate" model="['observationInstance':observationInstance]"/>
                        </div>


                        <div class="section" style="margin:40px 0 0;">
                            <g:if
                            test="${observationInstance?.fetchSpeciesCall() == 'Unknown'}">
                            <div class="help-identify" class="control-label">
                                <label class="checkbox" style="text-align: left;"> <input
                                    type="checkbox" name="help_identify" /> <g:message code="link.help.identify" /> </label>
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
                        style="float: right; margin-right: 30px;"><g:message code="button.cancel" /></a>
                    </g:if>
                    <g:else>
                    <a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
                        style="float: right; margin-right: 30px;"> <g:message code="button.cancel" /> </a>
                    </g:else>

                    <g:if test="${observationInstance?.id}">
                    <div class="btn btn-danger"
                        style="float: right; margin-right: 5px;">

                        <a class="btn btn-danger btn-primary pull-right" style="margin-right: 5px;"
                            href="#"
                            onclick="return deleteObservation();"><i class="icon-trash"></i><g:message code="button.delete.observation" /></a>
                        <form action="${uGroup.createLink(controller:'observation', action:'flagDeleted')}" method='POST' name='deleteForm'>
                            <input type="hidden" name="id" value="${observationInstance.id}" />
                        </form>
                    </div>
                    </g:if>
                    <a id="addObservationSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> ${form_button_val} </a>

                    <div class="control-group">
                        <label class="checkbox" style="text-align: left;"> 
                            <g:checkBox style="margin-left:0px;"
                            name="agreeTerms" value="${observationInstance?.agreeTerms}"/>
                            <span class="policy-text"><g:message code="checklist.create.submit.form" />  </span></label>
                    </div>

                </div>
            </form>
            <%

            String obvTmpFileName = (observationInstance?.resource?.iterator()?.hasNext() ) ? (observationInstance.resource.iterator().next()?.fileName) : ''
            int index = obvTmpFileName.lastIndexOf("/");
            def obvDir = obvTmpFileName ?  obvTmpFileName.substring(0, index>0?index:obvTmpFileName.length()) : ""
            %>
            <form class="upload_resource ${hasErrors(bean: observationInstance, field: 'resource', 'errors')}" 
                title="${g.message(code:'title.checklist.create')}"
                method="post">

                <span class="msg" style="float: right"></span>
                <input class="videoUrl" type="hidden" name='videoUrl' value="" />
                <input class="audioUrl" type="hidden" name='audioUrl' value="" />
                <input type="hidden" name='obvDir' value="${obvDir}" />
                <input type="hidden" name='resType' value='${observationInstance?observationInstance.class.name:""}'>
            </form>

        </div>
    </div>

<r:script>	
    var add_file_button = '<li class="add_file addedResource" style="display:none;z-index:10;"><div class="add_file_container"><div class="add_image"></div><div class="add_video editable"></div></div><div class="progress"><div class="translucent_box"></div><div class="progress_bar"></div ><div class="progress_msg"></div ></div></li>';



$(document).ready(function(){
    var uploadResource = new $.fn.components.UploadResource($('.observation_create'));
    uploadResource.POLICY = "${policy}";
    uploadResource.SIGNATURE = "${signature}";
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

function deleteObservation(){
    var test="${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}";
                                        
    if(confirm(test)){
        document.forms.deleteForm.submit();
    }                       
}

</r:script>

</body>
</html>
