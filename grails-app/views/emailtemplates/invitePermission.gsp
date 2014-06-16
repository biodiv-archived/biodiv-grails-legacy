<%@ page contentType="text/html"%>

Hi ${curator.name.capitalize()},
<br/><br/> 
You have been invited as a ${invitetype} for ${rankLevel} : ${taxon.name} on the ${domain}.<br/>
<g:if test="${message}">
<br/>
Message : ${message}
<br/>
</g:if>
Please <a href="${uri}" title="Confirmation code">click here</a> to accept being a ${invitetype}.<br/>
<br/><br/>
Thank you,<br/>
The Portal Team
