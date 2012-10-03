
<%@page import="species.utils.ImageType"%>
<div style="margin: 5px 0px;position:relative;">
	<g:link controller="userGroup" action="show" id="${userGroup.id}"
		style="display:inline-block;width:85%">
		<img class="logo small_profile_pic" style="vertical-align: middle;"
			src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
			alt="${userGroup.name}" />
		<span class="ellipsis" style="margin: 0px 5px;">
			${userGroup.name}
		</span>
	</g:link>
	<g:link controller="userGroup" action="members" id="${userGroup.id}"
		title="No ofMembers"
		style="padding:5px;position:absolute;right:0px;display:inline-block;">
		<i class="icon-user"></i>
		${userGroup.getAllMembersCount()}
	</g:link>


</div>
