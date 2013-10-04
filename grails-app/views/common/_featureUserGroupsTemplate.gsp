<%@ page import="species.UserGroupTagLib"%>

<sec:ifLoggedIn>
<div class="feature-user-groups">
         
    	<a href="#" onclick="$(this).next('.show-user-groups').toggle(300);return false;">
	    		<h5>Feature Object : <span class="caret" style="margin-top: 3px;margin-left: 4px"></span></h5>
        </a>
        <div class="show-user-groups">
            <div id="userGroups" name="userGroups" style="list-style:none;clear:both;">
	        <uGroup:markFeaturedUserGroups model="['observationInstance':observationInstance,'featResult': featResult]"/>
            </div>
        <form>
            <textarea style='width:100%; max-width:90%; min-width:90%;' placeholder="Why Featuring??" name="notes" id="notes" maxlength="400"></textarea><br>
        </form>  

        <a onclick="feature('${observationInstance.id}', '${observationInstance.class.getCanonicalName()}','${uGroup.createLink(controller:'action', action:'featureIt', userGroup:userGroup)}');return false;" class="btn btn-primary"
	                        style="float: left; margin-right: 5px;"> Feature </a>
                                    
        <a onclick="unfeature('${observationInstance.id}', '${observationInstance.class.getCanonicalName()}', '${uGroup.createLink(controller:'action', action:'unfeatureIt', userGroup:userGroup)}');return false;" class="btn btn-primary"
	                        style="float: left; margin-right: 5px;"> unFeature </a>

        </div>

    </div>
    </sec:ifLoggedIn>
