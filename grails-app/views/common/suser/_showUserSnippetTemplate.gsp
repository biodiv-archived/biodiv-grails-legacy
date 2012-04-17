
<div class="span11">
	<div class="row">
		<g:set var="mainImage" value="${userInstance.icon()}" />
		<div class="figure span3"
			style="float: left; max-height: 220px; max-width: 200px">
			<div class="wrimg">
				<div class="thumbnail">
					<g:link action="show" controller="SUser" id="${userInstance.id}">
						<img src="${userInstance.icon()}" class="normal_profile_pic"
							title="${userInstance.name}" />
					</g:link>
				</div>
			</div>
		</div>

		<div class="span8">
			<sUser:showUserStory model="['userInstance':userInstance]"></sUser:showUserStory>
		</div>
	</div>
</div>
