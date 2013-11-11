<%@ page import="species.UserGroupTagLib"%>
<sec:ifLoggedIn>

<div id = "featureIn" class="feature-user-groups">

    <div class="show-user-groups" >

        <div id="userGroups" class="userGroups" style="margin-bottom: 0px;">
            <uGroup:markFeaturedUserGroups model="['observationInstance':observationInstance,'featResult': featResult]"/>
        </div>
        <form>
            <small id='remainingC'>Remaining characters : 400</small>
            <textarea style="height:100px;" placeholder="Why Featuring??" name="featureNotes" id="featureNotes" maxlength="400"></textarea>
        </form>  

        <div id="featureMsg" class="alert alert-success" style="display:none;"></div>
        <a onclick="feature('feature', '${observationInstance.id}', '${observationInstance.class.getCanonicalName()}','${uGroup.createLink(controller:'action', action:'featureIt', userGroup:userGroup)}');return false;" class="btn btn-primary"
            style="float: right; margin-right: 5px;"> Feature </a>

        <a onclick="feature('unfeature','${observationInstance.id}', '${observationInstance.class.getCanonicalName()}', '${uGroup.createLink(controller:'action', action:'unfeatureIt', userGroup:userGroup)}');return false;" class="btn btn-danger"
            style="float: right; margin-right: 5px;"> Unfeature </a>

    </div>

</div>
</sec:ifLoggedIn>
