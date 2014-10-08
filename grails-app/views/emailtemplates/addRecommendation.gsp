<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page contentType="text/html"%>

Hi
${username},
<br />
<br />
<table>
	<tr>
		<td style="width: 40px;"><g:link
				url="${uGroup.createLink(controller:'user', action:'show', id:actor.id, userGroupWebaddress:userGroupWebaddress, absolute:true) }">
				<img
					style="max-height: 32px; min-height: 16px; max-width: 32px; width: auto;"
					src="${actor.profilePicture(ImageType.SMALL)}" title="${actor.name}" />

			</g:link>
		</td>
		<td><g:link
				url="${uGroup.createLink(controller:'user', action:'show', id:actor.id, userGroupWebaddress:userGroupWebaddress, absolute:true) }">
				${actor.name}
			</g:link> : ${activity.activityTitle} <g:message code="msg.on.the" /><a href="${obvUrl}"><g:message code="msg.Observation" /></a><br />
			<g:if test="${activity.text }">
				${activity.text }
			</g:if></td>
	</tr>
	<tr>
		<td colspan="2">
			<g:message code="msg.notified.mail" /><br />
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<g:message code="msg.receive.notifications" />
			<a href="${userProfileUrl}"><g:message code="msg.user.profile" /></a> <g:message code="msg.switch.off" /><br />
		</td>
	</tr>
</table>
<br />
<g:message code="msg.Thank.you" />
<br />
<g:message code="msg.-The.portal.team" />
