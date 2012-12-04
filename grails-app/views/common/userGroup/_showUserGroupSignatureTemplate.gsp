<%@page import="species.utils.ImageType"%>
<div class=" ${showDetails ? 'pull-left' : 'span4'}" style="margin: 5px 0px;position:relative;${showDetails ? 'width:100%;' : ''}">
	<a href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}"
		style="display:inline-block;float:left; ${showDetails ? 'width:100%;' : ''}">
		<img class="logo ${showDetails ? 'normal_profile_pic' : 'small_profile_pic'}" style="vertical-align: middle;"
			src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
			alt="${userGroup.name}" />
		<span class="" style="margin: 0px 5px;">
			${userGroup.name}
		</span>
<%--		<g:if test="${showDetails}">--%>
<%--			<span class="ellipsis" style="margin: 0px 5px;">--%>
<%--				${userGroup.description}--%>
<%--			</span>--%>
<%--		</g:if>--%>
	</a>
	<g:if test="${!showDetails}">
		<g:link url="${uGroup.createLink(mapping:"userGroup", action:"members", absolute:'true', params=['webaddress':userGroup.webaddress])}"
			title="No ofMembers"
			style="float:right;margin-right:10px;display:inline-block;">
			<i class="icon-user"></i>
			${userGroup.getAllMembersCount()}
		</g:link>
	</g:if>
</div>
