<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.ActivityFeedService"%>

<ul class="nav sidebar left-sidebar span3">

	<li class="dropdown">
		<a href="#" class="dropdown-toggle" data-toggle="dropdown">
				Home<b class="caret" style="border-top-color: black;border-bottom-color: black;"></b>
		</a>	
		<!--h5 class="nav-header">Home</h5-->
		<ul class="dropdown-menu">
			
			<sec:ifLoggedIn>
				<li><a
					href="${uGroup.createLink(controller:'activityFeed', absolute:'true', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.MY_FEEDS])}"><i
						class="icon-home"></i>My Feed</a></li>

				<li><a
					href="${uGroup.createLink(controller:'user', absolute:'true', action:'show', id:sUser.renderCurrentUserId())}"><i
						class="icon-user"></i>My Profile</a></li>

				<li><a style="margin-right:5px;display:inline-block;"
					href="${uGroup.createLink(controller:'observation', absolute:'true', action:'list', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-screenshot"></i>My Observations</a><!-- a class="pull-right" style="display:inline-block;"
					href="${uGroup.createLink(controller:'observation', action:'create', absolute:'true')}"><i class="icon-plus"></i>
				</a--></li>

				<li><a
					href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list', params:['user':sUser.renderCurrentUserId()])}" title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality."><i
						class="icon-user"></i>My Groups<sup>Beta</sup></a>
					<uGroup:getCurrentUserUserGroupsSidebar />
				</li>
			</sec:ifLoggedIn>


			<sec:ifNotLoggedIn>
				<li><a
					href="${uGroup.createLink(controller:'activityFeed', absolute:'true', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.ALL])}"><i
						class="icon-home"></i>Activity</a></li>
				<!-- li class="${(params.action=='create')?'active':'' }"
					style="clear: both;"><a href="${createLink( controller:'observation', action:'create', absolute:'true')}"  style="display:inline;"><i
						class="icon-plus"></i>Add Observation <g:link
						controller='login' absolute='true' class="btn" style="margin-left:5px; display:inline;">Login</g:link></a></li-->
			</sec:ifNotLoggedIn>


		</ul>
	</li>

	<li class="dropdown">
		<a href="#" class="dropdown-toggle" data-toggle="dropdown">
				Groups<b class="caret" style="border-top-color: black;border-bottom-color: black;"></b>
		</a>	
		<uGroup:showSuggestedUserGroups />
	<li>

</ul>
