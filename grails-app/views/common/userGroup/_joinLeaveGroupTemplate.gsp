<g:if test="${showJoin}">
	<uGroup:isNotAMember model="['userGroupInstance':userGroupInstance]">
	
		<g:if test="${userGroupInstance.allowUsersToJoin}">
			<a class="btn btn-large btn-success joinUs"
				data-group-id="${userGroupInstance.webaddress }"> <i class="icon-plus"></i>
				Join Us</a>
		</g:if>
		<g:else>
			<a class="btn btn-large btn-success requestMembership"
				data-group-id="${userGroupInstance.webaddress }"> <i class="icon-plus"></i>
				Join Us</a>
		</g:else>
	</uGroup:isNotAMember>
</g:if>
<g:if test="${showLeave}">
	<uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
		<a class="btn btn-large btn-primary leaveUs"
			data-group-id="${userGroupInstance.webaddress }"><i class="icon-minus"></i>Leave</a>
	</uGroup:isAMember>
</g:if>
