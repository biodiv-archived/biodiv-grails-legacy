
<div class="span4 sidebar left-sidebar">

	<div class="super-section">
		<div class="section">
			<h5>
				<i class="icon-user"></i>Founders
			</h5>
			<g:each in="${userGroupInstance.getFounders()}" var="sUser">
				<g:link controller="SUser" action="show" id="${sUser.id}">
					${sUser.name}
				</g:link>
			</g:each>
		</div>

		<div class="section">
			<h5>
				<i class="icon-user"></i>Members
			</h5>
			<g:each in="${userGroupInstance.getMembers()}" var="sUser">
				<g:link controller="SUser" action="show" id="${sUser.id}">
					${sUser.name}
				</g:link>,
							</g:each>
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<uGroup:showAllTags
				model="['tagFilterByProperty':'All' , 'params':params, 'isAjaxLoad':true]" />
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<div class="prop">
				<span class="name"><i class="icon-time"></i>Founded</span>
				<obv:showDate
					model="['userGroupInstance':userGroupInstance, 'propertyName':'foundedOn']" />
			</div>
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<g:link action="aboutUs">More about us here</g:link>
			or<br />
			<g:link action="contactUs">Contact us here</g:link>
		</div>
	</div>

</div>
