<%@ page import="species.UserGroupTagLib"%>
<sec:ifLoggedIn>

<div id = "featureIn" class="feature-user-groups">

    <div class="show-user-groups" >

        <div id="userGroups" class="userGroups" style="margin-bottom: 0px;">
            <uGroup:markFeaturedUserGroups model="['observationInstance':observationInstance,'featResult': featResult]"/>
        </div>
        <form>
            <small id='remainingC'><g:message code="featureusergroup.remaining.characters" /></small>
            <textarea style="height:100px;" placeholder="${g.message(code:'featureusergroup.placeholder.why')}" name="featureNotes" id="featureNotes" maxlength="400"></textarea>
        </form>  

        <div id="featureMsg" class="alert alert-success" style="display:none;"></div>
        
        <g:if test="${params.controller == 'discussion'}">
            <label class="control-label"><i class="icon-calendar"></i>
                <g:message code="discussion.announce.till"/><span class="req">*</span>
            </label>
            <input name="expireTime" type="text" class ="expireTime date input-block-level" value="" style="width:172px;" placeholder="${g.message(code:'placeholder.dateinput.select.date')}" />
        </g:if>
        
        <a onclick="feature('feature', '${observationInstance.id}', '${observationInstance.class.getCanonicalName()}','${uGroup.createLink(controller:'action', action:'featureIt', userGroup:userGroup)}');return false;" class="btn btn-primary"
            style="float: right; margin-right: 5px;"><g:message code="button.feature" />  </a>

        <a onclick="feature('unfeature','${observationInstance.id}', '${observationInstance.class.getCanonicalName()}', '${uGroup.createLink(controller:'action', action:'unfeatureIt', userGroup:userGroup)}');return false;" class="btn btn-danger"
            style="float: right; margin-right: 5px;"> <g:message code="button.unfeature" /> </a>

    </div>

</div>
</sec:ifLoggedIn>
