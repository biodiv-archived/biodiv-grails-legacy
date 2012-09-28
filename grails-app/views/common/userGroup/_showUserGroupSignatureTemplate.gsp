
<%@page import="species.utils.ImageType"%>
<div class="prop tablet user_signature">
	<div class="figure user-icon" style="float: left;">

		<g:link controller="userGroup" action="show" id="${userGroup.id}">
			<img class="logo small_profile_pic"
				src="${userGroup.mainImage()?.fileName}" title="${userGroup.name}"
				alt="${userGroup.name}" />
		</g:link>

	</div>
	<div class="story" style="margin-left: 35px">
		<g:link controller="userGroup" action="show" id="${userGroup.id}">
			${userGroup.name}
		</g:link>

		<div class="story-footer" style="position: static;">
			<div class="footer-item" title="Founders">
				<g:link controller="userGroup" action="members" id="${userGroup.id}"
					title="No ofMembers"
					style="padding:5px;float:left;display:inline-block;">
					<i class="icon-user"></i>
					${userGroup.getAllMembersCount()}
				</g:link>
			</div>
		</div>
	</div>
</div>