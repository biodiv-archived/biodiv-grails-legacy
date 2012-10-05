
<%@page import="species.utils.ImageType"%>
<div style="margin: 5px 0px;position:relative;">
	<g:link mapping="userGroup" action="show" params="['webaddress':userGroup.webaddress, 'pos':pos]"
		style="display:inline-block;width:85%">
		<img class="logo small_profile_pic" style="vertical-align: middle;"
			src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
			alt="${userGroup.name}" />
		<span class="ellipsis" style="margin: 0px 5px;">
			${userGroup.name}
		</span>
	</g:link>
	<g:link mapping="userGroup" action="members" params="['webaddress':userGroup.webaddress]"
		title="No ofMembers"
		style="padding:5px;position:absolute;right:0px;display:inline-block;">
		<i class="icon-user"></i>
		${userGroup.getAllMembersCount()}
	</g:link>


</div>
