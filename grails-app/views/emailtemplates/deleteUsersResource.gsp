<%@ page contentType="text/html"%>

<g:message code="msg.Dear" /> ${username},<br/><br/>
<g:message code="msg.images.uploaded" /> ${uploadedDate}<g:message code="msg.expire" /> ${toDeleteDate}.<br/>
<g:message code="msg.view.uploads" /> <a href="${uGroup.createLink(controller:'SUser', action:'myuploads', absolute:true)}"><g:message code="here" /></a>.
<br/><br/>
<g:message code="msg.Thank.you" /><br/>
<g:message code="msg.-The.portal.team" />
