<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.groups.UserGroup"%>
<%@ page import="species.participation.ActivityFeedService"%>
<script>
<sec:ifLoggedIn>
$(function() {
	$('.login-box').mouseover(function() {
		$('.login-box-options').show();
	});

	$('.login-box').mouseout(function() {
		$('.login-box-options').hide();
	});
});
</sec:ifLoggedIn>
</script>

<ul class="nav header_userInfo pull-right">
	<sec:ifNotLoggedIn>
		<li><a id="loginLink"
			href="${uGroup.createLink(controller:'login', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }">Login</a>
		</li>
	</sec:ifNotLoggedIn>
	<sec:ifLoggedIn>
		<li class="dropdown"><a href="#" class="dropdown-toggle" style="padding:0"
			data-toggle="dropdown"> 
<%--			<i class="icon-home" title="Home"></i>--%>
			<b
				class="caret"
				style="border-top-color: black; border-bottom-color: black;display:none;"></b>
				
				 </a> <!--h5 class="nav-header">Home</h5-->
			<ul class="dropdown-menu"  style="margin:0px;padding:0px;">


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
					href="${uGroup.createLink(controller:'logout', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }"><i
						class="icon-off"></i>Logout</a></li>

			</ul>
			<div class="login-box">
				<sUser:renderProfileLink />
			</div></li>

	</sec:ifLoggedIn>
</ul>

<%--<div class="login-box">--%>
<%--	<div class="register">--%>
<%--		<sec:ifNotLoggedIn>--%>
<%--			<g:link controller='login'>Login</g:link> | <g:link--%>
<%--				controller='register'>Register</g:link>--%>
<%--		</sec:ifNotLoggedIn>--%>
<%--	</div>--%>
<%----%>
<%--	<span class='loginLink'> <sec:ifLoggedIn>--%>
<%--			<sUser:renderProfileLink />--%>
<%--		</sec:ifLoggedIn> </span>--%>
<%--	<div class="login-box-options" style="display: none;">--%>
<%--		<sec:ifLoggedIn>--%>
<%--			<a id="logout" href="${createLink(controller:'logout')}">Logout</a>--%>
<%--		</sec:ifLoggedIn>--%>
<%--	</div>--%>
<%--</div>--%>
