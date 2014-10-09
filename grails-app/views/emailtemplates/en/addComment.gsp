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
			</g:link> : ${activity.activityTitle} on a
			<g:if test="${domainObjectTitle}">
				${domainObjectType} <a href="${obvUrl}">${domainObjectTitle}</a>
			</g:if>
			<g:else>
				<a href="${obvUrl}">${domainObjectType}</a>
			</g:else> 
			<br />
			<g:if test="${activity.text }">
				${activity.text }
			</g:if></td>
	</tr>
	<tr>
		<td colspan="2">
			You will be notified by mail on any social activity on the
			observation.<br />
		</td>
	</tr>
	<tr>
		<td colspan="2">
			If you do not want to receive notifications please go to your
			<a href="${userProfileUrl}">user profile</a> and switch it off.<br />
		</td>
	</tr>
</table>
<br />
Thank you,
<br />
-The portal team
