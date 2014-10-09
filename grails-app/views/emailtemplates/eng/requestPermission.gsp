<%@ page contentType="text/html"%>

Hi ${admin.name.capitalize()},
<br/><br/> 
<a href="${requesterUrl}" title="${requester.name}">${requester.name.capitalize()}</a> has requested for permission as a ${invitetype} for ${rankLevel} : ${taxon.name} on the ${domain}.<br/>
<g:if test="${message}">
<br/>
Message : ${message}
<br/>
</g:if>
Please <a href="${uri}" title="Confirmation code">click here</a> to confirm granting a ${invitetype} permission.<br/>
<br/><br/>
Thank you,<br/>
The Portal Team
