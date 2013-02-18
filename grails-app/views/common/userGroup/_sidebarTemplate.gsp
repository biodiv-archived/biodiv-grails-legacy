<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.ActivityFeedService"%>

<ul class="nav left-sidebar pull-right">

	<li><div class="header_group span4" style="height: 50px;">
			<obv:showRelatedStory
				model="['controller':'userGroup', 'observationId': 1, 'action':'getFeaturedUserGroups', 'id':'uG', hideShowAll:true]" />
		</div>
	</li>
	<li><a
		href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}">
			<i class="icon-group" title="Groups"></i><sup>Beta</sup> </a>
	</li>
	<li><search:searchBox /></li>
	<sec:ifLoggedIn>
	<li class="dropdown"><a href="#" class="dropdown-toggle"
		data-toggle="dropdown"> <i class="icon-home" title="Home"></i><b class="caret"
			style="border-top-color: black; border-bottom-color: black;"></b> </a> <!--h5 class="nav-header">Home</h5-->
		<ul class="dropdown-menu">

			
				<li><a
					href="${uGroup.createLink(controller:'activityFeed', absolute:'true', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.MY_FEEDS])}"><i
						class="icon-home"></i>My Feed</a>
				</li>

				<li><a
					href="${uGroup.createLink(controller:'user', absolute:'true', action:'show', id:sUser.renderCurrentUserId())}"><i
						class="icon-user"></i>My Profile</a>
				</li>

				<li><a style="margin-right: 5px; display: inline-block;"
					href="${uGroup.createLink(controller:'observation', absolute:'true', action:'list', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-screenshot"></i>My Observations</a> <!-- a class="pull-right" style="display:inline-block;"
					href="${uGroup.createLink(controller:'observation', action:'create', absolute:'true')}"><i class="icon-plus"></i>
				</a-->
				</li>

				<li><a
					href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list', params:['user':sUser.renderCurrentUserId()])}"
					title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality."><i
						class="icon-user"></i>My Groups<sup>Beta</sup> </a> <uGroup:getCurrentUserUserGroupsSidebar />
				</li>
				<li><a id="logout"
					href="${uGroup.createLink(controller:'logout', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }">Logout</a>
				</li>

		</ul></li>
			</sec:ifLoggedIn>


			<sec:ifNotLoggedIn>
				<li><sUser:userLoginBox
								model="['userGroup':userGroupInstance]" />
				</li>
			</sec:ifNotLoggedIn>


	<%--	<li class="dropdown"><a href="#" class="dropdown-toggle"--%>
	<%--		data-toggle="dropdown"> Groups<b class="caret"--%>
	<%--			style="border-top-color: black; border-bottom-color: black;"></b> </a>--%>
	<%--		<ul class="dropdown-menu">--%>
	<%--			<li><a--%>
	<%--				href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list')}">--%>
	<%--					<i class="icon-user"></i>All Groups<sup>Beta</sup> </a></li>--%>
	<%--			<li><uGroup:showSuggestedUserGroups /></li>--%>
	<%----%>
	<%--		</ul>--%>
	<%--	<li>--%>

</ul>
