<%@ page import="species.UserGroupTagLib"%>
<style>
.show-user-groups {
	display: none;
}
</style>

<sec:ifLoggedIn>
<div class="sidebar_section" style="clear:both;overflow:hidden;border:1px solid #CECECE;">

<div id = "featureIn" class="feature-user-groups">
         
    	<a href="#" onclick="$(this).next('.show-user-groups').toggle(300);return false;">
	    		<h5>Feature Item in Groups<span class="caret" style="margin-top: 8px;margin-left: 5px"></span></h5>
        </a>
        <div class="show-user-groups" >
            <div id="userGroups" name="userGroups" style="list-style:none;clear:both;">
	        <uGroup:markFeaturedUserGroups model="['observationInstance':observationInstance,'featResult': featResult]"/>
            </div>
        <form>
            <textarea placeholder="Why Featuring??" name="notes" id="notes" maxlength="400"></textarea><br>
        </form>  

        <a onclick="feature('${observationInstance.id}', '${observationInstance.class.getCanonicalName()}','${uGroup.createLink(controller:'action', action:'featureIt', userGroup:userGroup)}');return false;" class="btn btn-primary"
	                        style="float: right; margin-right: 5px;"> Feature </a>
                                    
        <a onclick="unfeature('${observationInstance.id}', '${observationInstance.class.getCanonicalName()}', '${uGroup.createLink(controller:'action', action:'unfeatureIt', userGroup:userGroup)}');return false;" class="btn btn-danger"
	                        style="float: right; margin-right: 5px;"> unFeature </a>

        </div>

    </div>
    </div>
    </sec:ifLoggedIn>
