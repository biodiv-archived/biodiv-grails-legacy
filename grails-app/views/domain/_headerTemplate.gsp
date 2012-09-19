<div class="container outer-wrapper">
	<g:if test="${userGroupInstance  && userGroupInstance.id }">
		<div class="page-header clearfix"
			style="margin:5px 0px; padding:18px 0px;background-image:url(${resource(dir:'images', file:'species_canvas.png')})">
			<div style="width: 100%;">
				<uGroup:showHeader model=[ 'userGroupInstance':userGroupInstance] />
			</div>
		</div>
	</g:if>
	<g:else>
		<div class="page-header clearfix"
			style="margin:5px 0px; padding:18px 0px;background-image:url(${resource(dir:'images', file:'species_canvas.png')})">
			<div style="width: 100%;">

				<div class="span3 logo">
					<a href="/"> <img class="logo"
						src="/sites/all/themes/ibp/images/map-logo.gif"
						title="India Biodiversity Portal" alt="India Biodiversity Portal">
					</a>
				</div>
				<h1>India Biodiversity Portal</h1>

			</div>
		</div>
	</g:else>

	<div class="navbar navbar-static-top btn"
		style="width: 100%; margin-left: -10px; margin-bottom: 0px">

		<div class="navbar-inner">
			<ul class="nav">
				<g:if test="${userGroupInstance && userGroupInstance.id}">
					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li class="${(params.action == 'index')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"index",  id:userGroupInstance.id)}"
						title="Activity">Activity</a>
					</li>
					<li class="${(params.action == 'aboutUs')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"aboutUs",  id:userGroupInstance.id)}"
						title="About Us">About Us</a>
					</li>

					<li class="divider-vertical"></li>

					<li class="${(params.controller == 'species')?' active':''}"><a
						href="${createLink(controller:"species", "action":"list")}"
						title="Species">Species</a>
					</li>
					<li class="${(params.action == 'observations')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"observations",  id:userGroupInstance.id)}"
						title="Observations">Observations</a>
					</li>

					<li><a href="/maps" title="Maps">Maps</a></li>
					<li><a href="/checklists" title="Checklists">Checklists</a>
					</li>
					<li>
						<ul class="nav">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> More <b class="caret"></b> </a>
								<div class="dropdown-menu">
									<ul class="nav">

										<li><a
											href="${createLink(controller:"userGroup", "action":"pages",  id:userGroupInstance.id)}"
											title="Pages">Pages</a>
										</li>
										<li><a href="/calendar" title="Events">Events</a></li>
										<li><a href="/biodiversity_news" title="News">News</a></li>
										<li><a href="/collaborate-wg" title="Collaborate">Collaborate</a>
										</li>
										<li><a href="/themepages/list" title="Themes">Themes</a>
										</li>
									</ul>
								</div></li>
						</ul></li>
					<li class="divider-vertical"></li>



					<li
						class="${(params.controller == 'user' || params.controller == 'SUser')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"members",  id:userGroupInstance.id)}"
						title="Members">Members</a>
					</li>



				</g:if>
				<g:else>
					<li class=" ${(params.controller == 'activityFeed')?'active':''}"><a
						href="${createLink("controller":"activityFeed")}" title="Activity">Activity</a>
					</li>

					<li><a href="/about" title="About Us">About Us </a></li>

					<li class="divider-vertical"></li>

					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li class=" ${(params.controller == 'species')?'active':''}"><a
						href="${createLink("controller":"species")}" title="Species">Species</a>
					</li>
					<li class="${(params.controller == 'observation')?'active':''}"><a
						href="${createLink("controller":"observation")}"
						title="Observations">Observations</a></li>
					<li><a href="/maps" title="Maps">Maps</a></li>
					<li><a href="/checklists" title="Checklists">Checklists</a>
					</li>

					<li>
						<ul class="nav">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> More <b class="caret"></b> </a>
								<div class="dropdown-menu">
									<ul class="nav">
										<li><a href="/calendar" title="Events">Events</a></li>
										<li><a href="/biodiversity_news" title="News">News</a></li>
										<li><a href="/collaborate-wg" title="Collaborate">Collaborate</a>
										</li>
										<li><a href="/themepages/list" title="Themes">Themes</a>
										</li>

									</ul>
								</div></li>
						</ul></li>

					<li class="divider-vertical"></li>
					<li
						class="${(params.controller == 'user' || params.controller == 'SUser')?' active':''}"><a
						href="${createLink(controller:'user', action:'list')}"
						title="Members">Members</a></li>

				</g:else>
			</ul>

		</div>
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
	padding: 0;
	border: 0;
	background-color: transparent;
	box-shadow: none;
}
</style>
