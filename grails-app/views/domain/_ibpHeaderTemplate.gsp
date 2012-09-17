
<div id="ibp-header" class="header gradient-bg navbar navbar-static-top"
	style="display: none;">

	<div class="navbar-inner" style="padding-left: 0px">
		<!-- Logo -->
		<div class="span3" style="position: relative;">
			<a href="/"> <img class="logo" alt="western ghats"
				src="/sites/all/themes/ibp/images/map-logo.gif"> </a>
			<div id="myGroups" class=" header gradient-bg "
				style="width: 100%; min-width: 100%; margin: 0px; position: absolute; bottom: -35px; text-align: center;">
				<a href="${createLink(controller:'userGroup', action:'list') }"
					id="allGroups" style="display: inline;">All Groups</a>
				<sec:ifLoggedIn>
					<a
						href="${createLink(controller:'userGroup', action:'list', params:['user':2])}"
						id="myGroups" style="display: inline;">My Groups</a>
				</sec:ifLoggedIn>
			</div>
		</div>

		<div id="mainMenu" class="span12 nav-collapse collapse"
			style="position: relative;">
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
					<li
						class="menu-450 ${(params.action == 'species')?' dropdown active':''}"><a
						href="${createLink(controller:"userGroup", "action":"species",  id:userGroupInstance.id)}"
						title="Species">Species</a> <g:if
							test="${params.action == 'species'}">
							<div class="submenu gradient-bg dropdown-menu"
								style="width: 390px">
								<ul class="navigation"
									id="navigation_userGroup_species_dashboard">
									<li class="navigation_active navigation_first"><a
										href="${createLink(controller:'userGroup', action:'species', id:userGroupInstance.id)}">Species
											Gallery</a></li>
									<li><a
										href="${createLink(controller:'species', action:'taxonBrowser')}">Taxonomy
											Browser</a></li>
									<li class="navigation_last"><a
										href="${createLink(controller:'species', action:'contribute')}">Contribute</a>
									</li>
								</ul>
							</div>
						</g:if>
					</li>
					<li
						class="menu-450  ${(params.action == 'observations')?'active dropdown':''}"><a
						href="${createLink(controller:"userGroup", "action":"observations",  id:userGroupInstance.id)}"
						title="Observations">Observations</a> <g:if
							test="${params.action == 'observations'}">
							<div class="submenu gradient-bg dropdown-menu"
								style="width: 290px">
								<ul class="navigation"
									id="navigation_userGroup_observation_dashboard">
									<li class="navigation_active navigation_first"><a
										href="${createLink(controller:'userGroup', action:'observations', id:userGroupInstance.id) }">Browse
											Observations</a>
									</li>
									<li class="navigation_last"><uGroup:isAMember
											model="['userGroupInstance':userGroupInstance]">
											<g:link controller="observation" action="create"
												params="['userGroup':userGroupInstance.id]">
													Add an Observation</g:link>
										</uGroup:isAMember>
									</li>
								</ul>
							</div>
						</g:if>
					</li>

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
								<div class="submenu gradient-bg dropdown-menu"
									style="width: 500px; left: -400px;">
									<ul class="navigation">
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
								</div>
							</li>
						</ul>
					</li>
				</g:if>
				<g:else>

					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li
						class="menu-450 ${(params.controller == 'species' || params.controller == 'search')?' dropdown active':''}"><a
						href="${createLink("controller":"species")}" title="Species">Species</a>
						<g:if
							test="${params.controller == 'species' || params.controller == 'search'}">
							<div class="submenu gradient-bg dropdown-menu"
								style="width: 390px">
								<sNav:render group="species_dashboard" subitems="false" />
							</div>
						</g:if></li>
					<li
						class="menu-450  ${(params.controller == 'observation')?'active dropdown':''}"><a
						href="${createLink("controller":"observation")}"
						title="Observations">Observations</a> <g:if
							test="${params.controller == 'observation'}">
							<div class="submenu gradient-bg dropdown-menu"
								style="width: 290px;">
								<sNav:render group="observation_dashboard" subitems="false" />
							</div>
						</g:if></li>
					<li class="menu-450"><a href="/maps" title="Maps">Maps</a></li>
					<li class="menu-451"><a href="/checklists" title="Checklists">Checklists</a>
					</li>
					<li>
						<ul class="nav">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> More <b class="caret"></b> </a>
								<div class="submenu gradient-bg dropdown-menu"
									style="width: 500px; left: -400px;">
									<ul class="navigation">
										<li><a href="${createLink(controller:'user', action:'list')}"
											title="Members">Members</a></li>
										<li><a href="/calendar" title="Events">Events</a></li>
										<li><a href="/biodiversity_news" title="News">News</a>
										</li>
										<li><a href="/about" title="About Us">About Us </a></li>

									</ul>
								</div>
							</li>
						</ul>
					</li>
				</g:else>
			</ul>
			<div class="pull-right">
				<search:searchBox />
			</div>
		</div>



		<div class="header_userInfo span2 pull-right">
			<sUser:userLoginBox />
		</div>
	</div>

</div>

<div class="navbar container">
	<sec:ifLoggedIn>
		<div id="myGroupsInfo" class="navbar-inner super-section row"
			style="overflow: auto; display: none; background-image: none;">
			<button type="button" class="close" data-dismiss="alert">×</button>
			<uGroup:getCurrentUserUserGroupsSidebar />
		</div>
	</sec:ifLoggedIn>
	<div id="allGroupsInfo" class="navbar-inner super-section row"
		style="clear: both; overflow: auto; display: none; background-image: none;">
		<button type="button" class="close" data-dismiss="alert">×</button>
		<uGroup:getSuggestedUserGroups />
	</div>
</div>

<r:script>
$(document).ready(function(){
	$("#allGroups").click(function(){
		
			$("#myGroupsInfo").slideUp('fast');
			$("#allGroupsInfo").slideDown('slow');
		
		return false;
	});
	$("#myGroups").click(function(){
		
			$("#allGroupsInfo").slideUp('fast');
			$("#myGroupsInfo").slideDown('slow');
		
		return false;
	});
	
	$(".close").click(function(){
		$(this).parent().slideUp('fast');
		return false;
	})
	$(".active .submenu").show()
});
</r:script>


<style>
.submenu {
	padding: 0; <%--
	background-color: transparent;
	--%>
}
</style>