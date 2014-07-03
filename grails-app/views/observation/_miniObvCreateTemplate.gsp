<%
def form_class = "addObservation"
def form_action = uGroup.createLink(action:'bulkSave', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
%>
<form class="${form_class}" action="${form_action}" method="POST" class="form-horizontal">

    <div class="span4" style="width:275px;border:1px solid black;">
        <div class="createdObv" style="display:none;">OBSERVATION CREATED</div>
        
        <div class="control-group ${hasErrors(bean: observationInstance, field: 'resource', 'error')}">
            <div class="image-resources-msg help-inline control-label">
                <g:renderErrors bean="${observationInstance}" as="list"
                field="resource" />
            </div>
        </div>    
        <div class="imageHolder ui-widget-header" style="position: relative; left: 0; top: 0; width: 150px; height: 250px; padding: 0.5em; float: left; margin: 10px;"></div>

        <g:render template="/common/speciesGroupHabitatDropdownTemplate" model="['observationInstance':observationInstance]"/> 
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

        <h5><label><i
                    class="icon-tags"></i>Tags <small><g:message code="observation.tags.message" default="" /></small></label>
        </h5>
        <div class="create_tags section-item" style="clear: both;">
            <ul class="obvCreateTags">
                <g:each in="${obvTags}" var="tag">
                <li>${tag}</li>
                </g:each>
            </ul>
        </div>
        <button type="button" class="btn toggleGrpsDiv" > Groups </button> 
        <div class="postToGrpsToggle" style="display:none;">
            <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
        </div>
        <input class="resourceListType" type="hidden" name='resourceListType' value= />
    </div>
</form>
<g:javascript>

$(document).ready(function(){
    $(".toggleGrpsDiv").click(function(){
        $(this).next().toggle();
    }); 
});

</g:javascript>
