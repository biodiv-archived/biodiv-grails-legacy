<%@page import="species.utils.Utils"%>

<div>
    <g:if test="${!params.webaddress}">
    <div class="prop">
        <span class="name"  style="padding-right: 20px;"><g:message code="msg.Group" /></span>
        <div class="value">
            <uGroup:showUserGroupSignature model="['userGroup':userGroupInstance]" />
        </div>
    </div>
    </g:if>
    <g:if test="${commentInstance.subject}">
    <div class="prop">
        <span class="name"  style="padding-right: 20px;"><g:message code="msg.Subject" /></span>
        <div class="value">
            ${commentInstance.subject?:"No Subject"}
        </div>
    </div>
    </g:if>
    <div class="prop">
        <span class="name" style="padding-right: 15px;"><g:message code="msg.Message" /></span>
        <div class="value">
            ${raw(commentInstance.body)}
        </div>
    </div>
</div>
