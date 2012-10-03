<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.ActivityFeedService"%>

<div class="sidebar left-sidebar">

	<div class="super-section">
		<h5 class="nav-header">Home</h5>
		<ul class="nav block-tagadelic">
			
			<sec:ifLoggedIn>
				<li><a
					href="${createLink(controller:'activityFeed', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.MY_FEEDS])}"><i
						class="icon-home"></i>My Feed</a></li>

				<li><a
					href="${createLink(controller:'user', action:'show', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-user"></i>My Profile</a></li>

				<li><a class="pull-left" style="margin-right:5px"
					href="${createLink(controller:'observation', action:'list', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-screenshot"></i>My Observations</a><a
					href="${createLink(action:'create')}"><i class="icon-plus"></i>
				</a></li>

				<li><a
					href="${createLink(controller:'userGroup', action:'list', params:['user':sUser.renderCurrentUserId()])}" title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality."><i
						class="icon-user"></i>My Groups<sup>Beta</sup></a>
					<uGroup:getCurrentUserUserGroupsSidebar />
				</li>
			</sec:ifLoggedIn>


			<sec:ifNotLoggedIn>
				<li><a
					href="${createLink(controller:'activityFeed', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.ALL])}"><i
						class="icon-home"></i>Activity</a></li>
				<li class="${(params.action=='create')?'active':'' }"
					style="clear: both;"><a href="${createLink( controller:'observation', action:'create')}"  style="display:inline;"><i
						class="icon-plus"></i>Add Observation <g:link
						controller='login' class="btn" style="margin-left:5px; display:inline;">Login</g:link></a></li>
			</sec:ifNotLoggedIn>


		</ul>
	</div>

	<div class="super-section">
		<uGroup:showSuggestedUserGroups />
	</div>

</div>