<%@page import="species.utils.ImageType"%>
<div class="span4" style="margin: 5px 0px;position:relative;">
	<g:link base="${userGroup.domainName}" mapping="userGroup" action="show" absolute='true' params="['webaddress':userGroup.webaddress, 'pos':pos]"
		style="display:inline-block;width:135%">
		<img class="logo ${showDetails ? 'profile_pic' : 'small_profile_pic'}" style="vertical-align: middle;"
			src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
			alt="${userGroup.name}" />
		<span class="ellipsis" style="margin: 0px 5px;">
			${userGroup.name}
		</span>
<%--		<g:if test="${showDetails}">--%>
<%--			<span class="ellipsis" style="margin: 0px 5px;">--%>
<%--				${userGroup.description}--%>
<%--			</span>--%>
<%--		</g:if>--%>
	</g:link>
	<g:if test="${!showDetails}">
		<g:link mapping="userGroup" action="members" absolute='true', params="['webaddress':userGroup.webaddress]"
			title="No ofMembers"
			style="padding:5px;position:absolute;right:0px;display:inline-block;">
			<i class="icon-user"></i>
			${userGroup.getAllMembersCount()}
		</g:link>
	</g:if>
</div>
