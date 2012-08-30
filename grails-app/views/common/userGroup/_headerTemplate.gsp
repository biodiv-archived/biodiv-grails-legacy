<div class="header">
	<div class="top_nav_bar navbar">
		<div class="container">
			<!-- Logo -->
			<div class="span3">
				<a href="${createLink(action:"show", id:userGroupInstance.id)}">
					<img class="logo" alt="${userGroupInstance.name}"
					src="${createLink(url: userGroupInstance.mainImage()?.fileName)}">
				</a>
			</div>
			<!-- Logo ends -->
			<!-- h1 class="span8">
							${userGroupInstance.name}
			</h1-->
			<ul class="nav">
				<li><a href="${createLink(action:'show', id:params.id)}">Home</a>
				</li>
				<li><a
					href="${createLink(action:'observations', id:params.id)}">Observations</a>
				</li>
				<li><a href="${createLink(action:'members', id:params.id)}">Members</a>
				</li>

				<li><a href="${createLink(action:'species', id:params.id)}">Species</a>
				</li>
				<li><a href="${createLink(action:'maps', id:params.id)}">Maps</a>
				</li>
				<li><a href="${createLink(action:'pages', id:params.id)}">Pages</a>
				</li>
				<li><a href="${createLink(action:'aboutUs', id:params.id)}">About
						Us</a>
				</li>
				<sec:permitted className='species.groups.UserGroup'
					id='${userGroupInstance.id}'
					permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

					<li><a href="${createLink(action:'settings', id:params.id)}">Settings</a>
					</li>
				</sec:permitted>
			</ul>
		</div>
	</div>


	<div id="actionsHeader" style="position: relative; overflow: visible;">
		<uGroup:showActionsHeaderTemplate model="['userGroupInstance':userGroupInstance]"/>
	</div>

	<g:if test="${flash.error}">
		<div class="alertMsg alert alert-error" style="clear:both;">
			${flash.error}
		</div>
	</g:if>
	
	<div class="alertMsg ${(flash.message)?'alert':'' }" style="clear:both;">
		${flash.message}
	</div>

</div>
