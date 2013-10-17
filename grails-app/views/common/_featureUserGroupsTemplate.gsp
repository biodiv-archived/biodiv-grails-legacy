<%@ page import="species.UserGroupTagLib"%>
<style>
.show-user-groups {
	display: none;
}
</style>

<sec:ifLoggedIn>

<div id = "featureIn" class="feature-user-groups">
         
	    		<h5 onclick="$(this).next('.show-user-groups').slideToggle(150);return false;">Feature Item in Groups<span class="caret" style="margin-top: 8px;margin-left: 5px"></span></h5>
        <div class="show-user-groups" >

            <div id="userGroups" class="userGroups" >
	        <uGroup:markFeaturedUserGroups model="['observationInstance':observationInstance,'featResult': featResult]"/>
            </div>
        <form>
            <textarea placeholder="Why Featuring??" name="featureNotes" id="featureNotes" maxlength="400"></textarea>
        </form>  

        <a onclick="feature('${observationInstance.id}', '${observationInstance.class.getCanonicalName()}','${uGroup.createLink(controller:'action', action:'featureIt', userGroup:userGroup)}');return false;" class="btn btn-primary"
	                        style="float: right; margin-right: 5px;"> Feature </a>
                                    
        <a onclick="unfeature('${observationInstance.id}', '${observationInstance.class.getCanonicalName()}', '${uGroup.createLink(controller:'action', action:'unfeatureIt', userGroup:userGroup)}');return false;" class="btn btn-danger"
	                        style="float: right; margin-right: 5px;"> Unfeature </a>

        </div>

    </div>
    </sec:ifLoggedIn>
