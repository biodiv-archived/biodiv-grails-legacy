<g:if test="${showJoin}">
	<uGroup:isNotAMember model="['userGroupInstance':userGroupInstance]">
	
		<g:if test="${userGroupInstance.allowUsersToJoin}">
			<a class="btn btn-success joinUs"
				data-group-id="${userGroupInstance.id }"> <i class="icon-plus"></i>
				Join Us</a>
		</g:if>
		<g:else>
			<a class="btn btn-success requestMembership"
				data-group-id="${userGroupInstance.id}"> <i class="icon-plus"></i>
				Join Us</a>
		</g:else>
	</uGroup:isNotAMember>
</g:if>
<g:if test="${showLeave}">
	<uGroup:isAMember model="['userGroupInstance':userGroupInstance]">
		<a class="btn btn-primary leaveUs"
			data-group-id="${userGroupInstance.id }"><i class="icon-minus"></i>Leave</a>
	</uGroup:isAMember>
</g:if>

<script type="text/javascript">
//IMP:Header is loaded in drupal pages as well. Any code in this block is not run when loaded by ajax
	//So please don't put any code here. Put it in init_header function in membership.js
</script>
