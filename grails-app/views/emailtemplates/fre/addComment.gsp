<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page contentType="text/html"%>

Bonjour
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
			</g:link> : ${activity.activityTitle} sur un

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
			Vous serez informé par message électronique de toute acitivté sur l'Observation.<br />
		</td>
	</tr>
	<tr>
		<td colspan="2">
			Si vous ne voulez pas recevoir de notification merci d'aller dans votre
			<a href="${userProfileUrl}">profil utilisateur</a> et de décocher la fonction de notification.<br />
		</td>
	</tr>
</table>
<br />
Merci,
<br />
L'équipe du portail "
