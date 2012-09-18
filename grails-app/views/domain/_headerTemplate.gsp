<div class="container outer-wrapper">
	<g:if test="${userGroupInstance  && userGroupInstance.id }">
		<div class="page-header clearfix"
			style="margin:5px 0px; padding:18px 0px;background-image:url(${resource(dir:'images', file:'species_canvas.png')})">
			<div style="width: 100%;">
				<uGroup:showHeader model=[ 'userGroupInstance':userGroupInstance] />
			</div>
		</div>
	</g:if>
	<div class="navbar navbar-static-top btn"
		style="width: 100%; margin-left: -10px; margin-bottom: 0px">

		<div class="navbar-inner">
			<ul class="nav">
				<g:if test="${userGroupInstance && userGroupInstance.id}">
					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li class="${(params.action == 'species')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"species",  id:userGroupInstance.id)}"
						title="Species">Species</a>
					</li>
					<li class="${(params.action == 'observations')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"observations",  id:userGroupInstance.id)}"
						title="Observations">Observations</a>
					</li>

					<li><a href="/maps" title="Maps">Maps</a></li>
					<li><a href="/checklists" title="Checklists">Checklists</a>
					</li>
					
					<li class="divider-vertical"></li>
					
					<li class="${(params.action == 'groups')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"list")}"
						title="Observations">Groups</a>
					</li>
					
					<li><a
						href="${createLink(controller:"userGroup", "action":"members",  id:userGroupInstance.id)}"
						title="Members">Members</a>
					</li>
					<li><a
						href="${createLink(controller:"userGroup", "action":"pages",  id:userGroupInstance.id)}"
						title="Pages">Pages</a>
					</li>
					<li><a href="/calendar" title="Events">Events</a></li>
					<li><a href="/biodiversity_news" title="News">News</a></li>
					<li class="divider-vertical"></li>
					<li class="${(params.action == 'aboutUs')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"aboutUs",  id:userGroupInstance.id)}"
						title="About Us">About Us</a>
					</li>
					<!-- li>
						<ul class="nav">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> More <b class="caret"></b> </a>
								<div class="submenu dropdown-menu">
									<ul class="navigation">


									</ul>
								</div></li>
						</ul></li-->
				</g:if>
				<g:else>

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
					
					<li class="divider-vertical"></li>
					<li class="${(params.action == 'groups')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"list")}"
						title="Groups">Groups</a>
					</li>
					<li><a href="${createLink(controller:'user', action:'list')}"
						title="Members">Members</a></li>
					<li><a href="/calendar" title="Events">Events</a></li>
					<li><a href="/biodiversity_news" title="News">News</a></li>
					<li class="divider-vertical"></li>
					<li><a href="/about" title="About Us">About Us </a></li>
					<!-- li>
						<ul class="nav">
							<li class="dropdown"><a href="#" class="dropdown-toggle"
								data-toggle="dropdown"> More <b class="caret"></b> </a>
								<div class="submenu dropdown-menu">
									<ul class="navigation">


									</ul>
								</div>
							</li>
						</ul>
					</li-->
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
