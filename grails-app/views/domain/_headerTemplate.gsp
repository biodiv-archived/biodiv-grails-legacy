<div class="container" style="width:100%">
	<div style="padding: 10px 0px;">
	<g:if test="${userGroupInstance  && userGroupInstance.id }">
		<uGroup:showHeader model=[ 'userGroupInstance':userGroupInstance] />
	</g:if>
	<g:else>
		<a href="/" class="span3 logo" style="margin-left: 0px;"> <img
			class="logo" src="/sites/all/themes/ibp/images/map-logo.gif"
			title="India Biodiversity Portal" alt="India Biodiversity Portal">
		</a>
		<h1>India Biodiversity Portal</h1>
	</g:else>
</div>
</div>
<div class="navbar navbar-static-top" style="margin-bottom:0px;">

	<div class="navbar-inner"
		style="background-color: transparent; background-image: none;">
		<div class="container outer-wrapper" style="background-color:transparent;padding:0px">
			<ul class="nav btn" style="width: 100%; margin: 0px;padding:4px 0px;">
				<g:if test="${userGroupInstance && userGroupInstance.id}">
					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li class="${(params.action == 'show')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"show",  id:userGroupInstance.id)}"
						title="Activity">Activity</a>
					</li>
					<li class="${(params.action == 'observations')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"observations",  id:userGroupInstance.id)}"
						title="Observations">Observations</a>
					</li>

					<li
						class="${((params.controller == 'user' || params.controller == 'SUser')&& params.action != 'header')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"members",  id:userGroupInstance.id)}"
						title="Members">Members</a>
					</li>

					<li class="${(params.action == 'aboutUs')?' active':''}"><a
						href="${createLink(controller:"userGroup", "action":"aboutUs",  id:userGroupInstance.id)}"
						title="About Us">About Us</a>
					</li>
					<li class="dropdown"><a href="#" class="dropdown-toggle"
						data-toggle="dropdown"> More <b class="caret"></b> </a>
						<ul class="dropdown-menu" style="text-align: left;">

							<li class="${(params.action == 'pages')?' active':''}"><a
								href="${createLink(controller:"userGroup", "action":"pages",  id:userGroupInstance.id)}"
								title="Pages">Pages</a>
							</li>
							<li
								class="${(request.forwardURI.contains('/calendar'))?' active':''}"><a
								href="/calendar" title="Events">Events</a></li>
							<li
								class="${(request.forwardURI.contains('/biodiversity_news'))?' active':''}"><a
								href="/biodiversity_news" title="News">News</a></li>
							<li
								class="${(request.forwardURI.contains('/cepf_grantee_database'))?' active':''}"><a
								href="/cepf_grantee_database"
								title="Western Ghats CEPF Projects">Western Ghats CEPF
									Projects</a></li>
							<li
								class="${(request.forwardURI.contains('/themepages/list'))?' active':''}"><a
								href="/themepages/list" title="Themes">Themes</a></li>
						</ul></li>
					<li style="float: right;">
						<ul class="nav">

							<li class="${(params.controller == 'species')?' active':''}"><a
								href="${createLink(controller:"species", "action":"list")}"
								title="Species">All Species</a>
							</li>
							<li class="${(request.forwardURI.contains('/map'))?' active':''}"><a
								href="/map" title="Maps">All Maps</a></li>
							<li
								class="${(request.forwardURI.contains('/checklists'))?' active':''}"><a
								href="/checklists" title="Checklists">All Checklists</a></li>
							<li
								class="${(params.controller == 'userGroup' && params.action== 'list')?' active':''}"><a
								href="${createLink(controller:"userGroup", "action":"list")}"
								title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality.">All Groups<sup >Beta</sup></a>
							</li>

						</ul>
					</li>
				</g:if>
				<g:else>
					<li class=" ${(params.controller == 'activityFeed')?'active':''}"><a
						href="${createLink("controller":"activityFeed")}" title="Activity">Activity</a>
					</li>

					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->

					<li class="${(params.controller == 'observation')?'active':''}"><a
						href="${createLink("controller":"observation")}"
						title="Observations">Observations</a></li>

					<li
						class="${((params.controller == 'user' || params.controller == 'SUser') && params.action != 'header')?' active':''}"><a
						href="${createLink(controller:'user', action:'list')}"
						title="Members">Members</a></li>
					<li class="${(request.forwardURI.contains('/about'))?' active':''}"><a
						href="/about" title="About Us">About Us </a></li>
					<li class="dropdown"><a href="#" class="dropdown-toggle"
						data-toggle="dropdown"> More <b class="caret"></b> </a>

						<ul class="dropdown-menu" style="text-align: left; color: #000">
							<li
								class="${(request.forwardURI.contains('/calendar'))?' active':''}"><a
								href="/calendar" title="Events">Events</a></li>
							<li
								class="${(request.forwardURI.contains('/biodiversity_news'))?' active':''}"><a
								href="/biodiversity_news" title="News">News</a></li>
							<li
								class="${(request.forwardURI.contains('/cepf_grantee_database'))?' active':''}"><a
								href="/cepf_grantee_database"
								title="Western Ghats CEPF Projects">Western Ghats CEPF
									Projects</a></li>
							<li
								class="${(request.forwardURI.contains('/themepages/list'))?' active':''}"><a
								href="/themepages/list" title="Themes">Themes</a></li>

						</ul>
					</li>
					<li style="float: right;">
						<ul class="nav">
							<li class=" ${(params.controller == 'species')?'active':''}"><a
								href="${createLink("controller":"species")}" title="Species">All
									Species</a></li>
							<li class="${(request.forwardURI.contains('/map'))?' active':''}"><a
								href="/map" title="Maps">All Maps</a></li>
							<li
								class="${(request.forwardURI.contains('/checklists'))?' active':''}"><a
								href="/checklists" title="Checklists">All Checklists</a></li>

							<li
								class="${(params.controller == 'userGroup'  && params.action== 'list')?' active':''}"><a
								href="${createLink(controller:"userGroup", "action":"list")}"
								title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality.">All Groups<sup>Beta</sup></a>
							</li>
						</ul>
					</li>

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
