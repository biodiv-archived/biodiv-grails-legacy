<div class="header">
	<div class="top_nav_bar navbar">
		<div class="container">
			<!-- Logo -->
			<div class="span3">
				<a href="${createLink(action:"show", id:userGroupInstance.id)}">
					<img class="logo" alt="${userGroupInstance.name}"
					src="/sites/all/themes/wg/images/map-logo.gif"> </a>
			</div>
			<!-- Logo ends -->
			<!-- h1 class="span8">
							${userGroupInstance.name}
			</h1-->
			<ul class="nav">
				<li><a href="${createLink(action:'home')}" data-toggle="tab">Home</a>
				</li>
				<li><a href="${createLink(action:'members')}" data-toggle="tab">Members</a>
				</li>
				<li><a href="${createLink(action:'observations')}"
					data-toggle="tab">Observations</a></li>
				<li><a href="${createLink(action:'pages')}" data-toggle="tab">Pages</a>
				</li>
				<li><a href="${createLink(action:'aboutUs')}" data-toggle="tab">About
						Us</a></li>
				<li><a href="${createLink(action:'contactUs')}"
					data-toggle="tab">Contact Us</a></li>
			</ul>
		</div>
	</div>
	<div class="observation-icons">
		<a class="btn btn-large btn-success"
			href="${createLink(action:'joinUs')}">Join Us</a> <a
			class="btn btn-large btn-success"
			href="${createLink(action:'inviteFriends')}">Invite Friends</a>
	</div>

</div>
