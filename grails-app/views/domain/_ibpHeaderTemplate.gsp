<div id="ibp-header" class="header gradient-bg navbar navbar-static-top"
	style="display: none;">

	<div class="navbar-inner" style="padding-left: 0px">
		<!-- Logo -->
		<div class="span3" style="position: relative;">
			<a href="/"> <img class="logo" alt="western ghats"
				src="/sites/all/themes/ibp/images/map-logo.gif"> </a>
			<div id="myGroups"
				style="width:190px;min-width:190px;margin:0px;position:absolute;bottom:-35px;background: url('${resource(dir:'images',file:'vc_botton_2012.jpg')}')  no-repeat;	">
				<g:link controller="userGroup" action="list"
					style="background-color: transparent;display:inline;">All Groups</g:link>
				<sec:ifLoggedIn>
					<g:link controller="userGroup" action="list" params="['user':2]"
						style="background-color: transparent;display:inline;">My Groups</g:link>
				</sec:ifLoggedIn>
			</div>
		</div>


		<div id="mainMenu" class="span12 nav-collapse collapse">
			<g:if test="${userGroupInstance}">
				<div class="span3 logo" style="margin-top: -5px;">
					<a href="${createLink(action:"show", id:userGroupInstance.id)}">
						<img class="logo" alt="${userGroupInstance.name}"
						src="${createLink(url: userGroupInstance.mainImage()?.fileName)}">
					</a>
				</div>
			</g:if>
			<ul class="nav links primary-links">
				<g:if test="${userGroupInstance}">
					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li class="menu-450"><a
						href="${createLink(controller:"userGroup", "action":"species",  id:userGroupInstance.id)}"
						title="Species">Species</a>
					</li>
					<li class="menu-450"><a
						href="${createLink(controller:"userGroup", "action":"observations",  id:userGroupInstance.id)}"
						title="Observations">Observations</a></li>

					<li class="menu-450"><a href="/maps" title="Maps">Maps</a></li>
					<li class="menu-451"><a href="/checklists" title="Checklists">Checklists</a>
					</li>
					<li><a
						href="${createLink(controller:"userGroup", "action":"aboutUs",  id:userGroupInstance.id)}"
						title="About Us">About Us</a>
					</li>
					<li>
						<ul class="nav">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> More <b class="caret"></b> </a>
								<ul class="dropdown-menu">
									<li><a
										href="${createLink(controller:"userGroup", "action":"members",  id:userGroupInstance.id)}"
										title="Members">Members</a>
									</li>
									<li class="menu-450"><a
										href="${createLink(controller:"userGroup", "action":"pages",  id:userGroupInstance.id)}"
										title="Pages">Pages</a>
									</li>
									<li><a href="/calendar" title="Events">Events</a></li>
									<li><a href="/biodiversity_news" title="News">News</a></li>

								</ul>
							</li>
						</ul>
					</li>
				</g:if>
				<g:else>

					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li class="menu-450"><a
						href="${createLink("controller":"species")}" title="Species">Species</a>
					</li>
					<li class="menu-450"><a
						href="${createLink("controller":"observation")}"
						title="Observations">Observations</a></li>
					<li class="menu-450"><a href="/maps" title="Maps">Maps</a></li>
					<li class="menu-451"><a href="/checklists" title="Checklists">Checklists</a>
					</li>
					<li>
						<ul class="nav">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> More <b class="caret"></b> </a>
								<ul class="dropdown-menu">
									<li><a href="${createLink(controller:"user")}"
										title="Members">Members</a></li>
									<li><a href="/calendar" title="Events">Events</a></li>
									<li><a href="/biodiversity_news" title="News">News</a>
									</li>
									<li><a href="/about" title="About Us">About Us </a></li>

								</ul>
							</li>
						</ul>
					</li>
				</g:else>
			</ul>
			<div class="pull-right">
				<search:searchBox />
			</div>

			<div id="menu" class="submenu">

				<g:if
					test="${params.controller == 'species' || params.controller == 'search'}">
					<sNav:render group="species_dashboard" subitems="false" />
				</g:if>
				<g:if test="${params.controller == 'observation'}">
					<sNav:render group="observation_dashboard" subitems="false" />
				</g:if>
			</div>

		</div>



		<div class="header_userInfo span2 pull-right">
			<sUser:userLoginBox />
		</div>
	</div>

</div>

<sec:ifLoggedIn>
	<div class="navbar">
		<div class="navbar-inner super-section" style="overflow: hidden;">
			<uGroup:getCurrentUserUserGroupsSidebar />
		</div>

		<div class="navbar-inner super-section"
			style="clear: both; overflow: hidden;">
			<uGroup:getSuggestedUserGroups />
		</div>
	</div>
</sec:ifLoggedIn>



