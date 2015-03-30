<%
def form_class = "addObservation"
def form_action = uGroup.createLink(action:'bulkSave', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
%>
<form class="${form_class}" action="${form_action}" method="POST">
    <g:hasErrors bean="${observationInstance}">
    <div class="error-message">
    <i class="icon-warning-sign"></i>
        <span class="label label-important"> <g:message
        code="fix.errors.before.proceeding" default="Fix errors" /> </span>
    </div>
    </g:hasErrors>

   <span class="createdObv label label-success" style="display:none;"><i class="icon-check"></i><g:message code="miniobvcreate.successfully.created" />  </span>
    <div class="obvTemplate">
        <div class="control-group ${hasErrors(bean: observationInstance, field: 'resource', 'error')}">
            <div class="image-resources-msg help-inline control-label">
                <g:renderErrors bean="${observationInstance}" as="list"
                field="resource" />
            </div>
        </div>    
        <div class="imageHolder" style="position: relative; left: 50px; top: 0; width: 150px; height: 250px; padding: 0.5em; margin: 10px;background: url(${createLinkTo(dir: 'images', file: 'dragndropgrey.png', absolute:true)})"></div>

        <g:render template="/common/speciesGroupDropdownTemplate" model="['observationInstance':observationInstance]"/> 
        <g:render template="/common/speciesHabitatDropdownTemplate" model="['observationInstance':observationInstance]"/> 
        <div style="margin:40px 0 0 0px;">
            <div class="help-identify" class="control-label">
                <label class="checkbox" style="text-align: left;"> <input
                    type="checkbox" name="help_identify" /> <g:message code="link.help.identify" /> </label>
            </div>
            <reco:create />
        </div>

        <div style="margin:0px;">
            <g:render template="dateInput" model="['observationInstance':observationInstance]"/>
            <%
            def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
            %>
            <div>
                <obv:showMapInput model="[observationInstance:obvInfoFeeder, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
            </div>
        </div>
        
        <h5 style="margin-top:20px;"><label><i
                    class="icon-pencil"></i><g:message code="default.notes.label" /> <small><g:message code="observation.notes.message" default="Description" /></small></label>
        </h5>
        <div class="section-item" style="margin-right: 10px;">
            <textarea name="notes" style="margin: 0px 0px 10px; width:257px;max-width: 257px; height: 40px;">            
            </textarea>
        </div>
        <h5><label>
                <i class="icon-tags"></i><g:message code="default.tags.label" /> <small><g:message code="observation.tags.message" default="" /></small></label>
        </h5>
        <div class="create_tags section-item" style="clear: both;">
            <ul class="obvCreateTags" rel="${g.message(code:'placeholder.add.tags')}">
                <g:each in="${obvTags}" var="tag">
                <li>${tag}</li>
                </g:each>
            </ul>
        </div>
        <button type="button" class="btn toggleGrpsDiv" style="margin-left:11px;" > <g:message code="button.user.groups" /></button> 
        <div class="postToGrpsToggle" style="display:none;">
            <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
        </div>
        <input class="resourceListType" type="hidden" name='resourceListType' value= />
        <input class="agreeTerms" type="checkbox" name='agreeTerms' style ="display:none;"/>
    </div>
</form>
<r:script>

$(document).ready(function(){
    $(".toggleGrpsDiv").unbind("click").click(function(){
        var me = this;
        $(me).next().toggle();
    });
    $(".close_user_group").unbind("click").click(function(){
        $(this).closest(".postToGrpsToggle").toggle();      
    });
});

</r:script>
