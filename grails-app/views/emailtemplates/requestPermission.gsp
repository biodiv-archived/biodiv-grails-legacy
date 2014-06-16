<%@ page contentType="text/html"%>

Hi ${admin.name.capitalize()},
<br/><br/> 
<a href="${requesterUrl}" title="${requester.name}">${requester.name.capitalize()}</a> has requested for permission as a ${invitetype} for ${rankLevel} : ${taxon.name} on the ${domain}.<br/>
Please <a href="${uri}" title="Confirmation code">click here</a> to confim granting a ${invitetype} permission.<br/>
<br/><br/>
Thank you,<br/>
The Portal Team
