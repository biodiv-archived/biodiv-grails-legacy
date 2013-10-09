	<div class="post-to-groups" >
	  	<a href="#" onclick="$(this).next('.post-main-content').toggle(150);return false;">
	  		<h5>Post to User Groups<span class="caret" style="margin-top: 8px;margin-left:5px;"></span></h5>
	  	</a>
	  	<div class="post-main-content" style="display:none;" >
	       <div>
	       		<g:if test="${isBulkPull}">
			       	<div>
			       		<a class="select-all" href="#" title="Select all" onclick='updateListSelection($(this));return false;'>Select all</a> <b>|</b>
			       		<a class="reset" href="#" title="Reset" onclick='updateListSelection($(this));return false;'>Reset</a>
			       	</div>
			   	</g:if>
		       	<hr/>
	       		<div id="userGroups" name="userGroups" style="list-style:none;clear:both;">
	               <uGroup:getCurrentUserUserGroups model="[observationInstance:observationInstance, onlyExpertGroups:onlyExpertGroups]"/>
	           	</div>
	        </div>
	       	<a onclick="submitToGroups('post', '${objectType}', '${uGroup.createLink(controller:'userGroup', action:'bulkPost', userGroup:userGroup)}', ${isBulkPull} ,'${observationInstance?.id}');return false;" class="btn btn-primary"
	                     style="float: right; margin-right: 5px;"> Post </a>
	  		<a onclick="submitToGroups('unpost', '${objectType}', '${uGroup.createLink(controller:'userGroup', action:'bulkPost', userGroup:userGroup)}', ${isBulkPull}, '${observationInstance?.id}');return false;" class="btn btn-danger"
	                     style="float: right; margin-right: 5px;"> Unpost </a>
		</div>
	</div>
