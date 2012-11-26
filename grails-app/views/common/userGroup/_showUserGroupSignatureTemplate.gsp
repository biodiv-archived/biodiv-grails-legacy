<%@page import="species.utils.ImageType"%>
<div class="media" style="${showDetails ? 'width:100%;' : ''}">
	<a href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}"
		class="pull-left">
		<img class="logo media-object ${showDetails ? 'normal_profile_pic' : 'small_profile_pic'}"
			src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
			alt="${userGroup.name}" />
		
		
<%--		<g:if test="${showDetails}">--%>
<%--			<span class="ellipsis" style="margin: 0px 5px;">--%>
<%--				${userGroup.description}--%>
<%--			</span>--%>
<%--		</g:if>--%>
	</a>
	<div class="media-body">
		<span class="media-heading">${userGroup.name}</span>
		<g:if test="${!showDetails}">
			<g:link url="${uGroup.createLink(mapping:"userGroup", action:"members", absolute:'true', params=['webaddress':userGroup.webaddress])}"
				title="No ofMembers" style="float:right;margin-right:10px;display:inline-block;">
				<i class="icon-user"></i>
				${userGroup.getAllMembersCount()}
			</g:link>
		</g:if>
	</div>
	
</div>
