<sec:ifLoggedIn>
	<uGroup:isUserGroupMember>
	    <div class="post-to-groups">
	        <h5>Post to User Groups</h5>
	        <div>
	        	<div>
	        	<a class="select-all" href="#" title="Select all" onclick='updateListSelection($(this));return false;'>Select all</a> <b>|</b>
	        	<a class="reset" href="#" title="Reset" onclick='updateListSelection($(this));return false;'>Reset</a>
	        </div>
	        <hr/>
	            <div id="userGroups" name="userGroups" style="list-style:none;clear:both;">
	                <uGroup:getCurrentUserUserGroups model="[]"/>
	            </div>
	        </div>
	    <a onclick="submitToGroups('post', '${objectType}', '${uGroup.createLink(controller:'userGroup', action:'bulkPost', userGroup:userGroup)}');return false;" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Post </a>
	    <a onclick="submitToGroups('unpost', '${objectType}', '${uGroup.createLink(controller:'userGroup', action:'bulkPost', userGroup:userGroup)}');return false;" class="btn btn-danger"
                        style="float: right; margin-right: 5px;"> Unpost </a>
       </div>
	</uGroup:isUserGroupMember>
</sec:ifLoggedIn>
