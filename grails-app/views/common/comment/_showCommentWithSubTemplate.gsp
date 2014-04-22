<%@page import="species.utils.Utils"%>

<div>
	<g:if test="${!params.webaddress}">
	<uGroup:showUserGroupSignature
				model="['userGroup':userGroupInstance]" />
	</g:if>
	<div class="prop">
     	<span class="name"  style="padding-right: 20px;">Subject</span>
            <div class="value">
            	${commentInstance.subject?:"No Subject"}
            </div>
	</div>
	<div class="prop">
     	<span class="name" style="padding-right: 15px;">Message</span>
            <div class="value">
            	${commentInstance.body}
            </div>
	</div>
</div>
