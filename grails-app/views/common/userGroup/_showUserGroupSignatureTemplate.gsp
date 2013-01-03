<%@page import="species.utils.ImageType"%>

<div class="media signature thumbnail clearfix ${showDetails ? '' : 'span3'}"
	style="margin-left: 0px;width:auto;">
	<div class="snippet tablet "
						style="display: table; height: ${showDetails ? '100px;width:100%;':'40px;'}">
	<div class="figure pull-left" style="display: table; height: ${showDetails ? '100px;':'40px;'};">
		<a
			href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}">
			<img
			class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
			src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
			alt="${userGroup.name}" /> </a>
	</div>

	<a
		href="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', base:userGroup.domainName, 'userGroup':userGroup, 'pos':pos)}">
		<span class="ellipsis multiline" style="display: block;"
		title="${userGroup.name}"> ${userGroup.name} </span> </a>

	<g:if test="${!showDetails}">
		<div class="pull-left">
			
				<i class="icon-user"></i>
				${userGroup.getAllMembersCount()}
		</div>
	</g:if>
	</div>
</div>
