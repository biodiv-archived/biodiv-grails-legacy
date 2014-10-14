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
			href="${uGroup.createLink(controller:'login', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }"><g:message code="button.login" /></a>
		</li>
	</sec:ifNotLoggedIn>
	<sec:ifLoggedIn>
		<li><div class="login-box">
				<sUser:renderProfileLink model="['hideDetails':true]"/>
			</div></li>
		<li class="dropdown"  style="height:32px;">
		
		<a href="#" class="dropdown-toggle"  style="height:30px;"
			data-toggle="dropdown"> <%--			<i class="icon-home" title="Home"></i>--%>
                        <i class="icon-cog icon-gray"></i>
				 </a> <!--h5 class="nav-header">Home</h5-->
			<ul class="dropdown-menu">


				<li><a
					href="${uGroup.createLink(controller:'activityFeed', absolute:'true', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.MY_FEEDS])}"><i
						class="icon-home"></i><g:message code="link.my.feed" /></a>
				</li>

				<li><a
					href="${uGroup.createLink(controller:'user', absolute:'true', action:'show', id:sUser.renderCurrentUserId())}"><i
						class="icon-user"></i><g:message code="link.my.profile" /></a>
				</li>

				<li><a style="margin-right: 5px; display: inline-block;"
					href="${uGroup.createLink(controller:'observation', absolute:'true', action:'list', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-screenshot"></i><g:message code="link.my.observations" /> </a> <!-- a class="pull-right" style="display:inline-block;"
					href="${uGroup.createLink(controller:'observation', action:'create', absolute:'true')}"><i class="icon-plus"></i>
				</a-->
				</li>

				<li><a
					href="${uGroup.createLink(controller:'userGroup', absolute:'true', action:'list', params:['user':sUser.renderCurrentUserId()])}"
					title="Groups"><i
						class="icon-user"></i><g:message code="link.my.groups" /> <sup><g:message code="msg.beta" /></sup> </a> <uGroup:getCurrentUserUserGroupsSidebar />
				</li>
				<li><a id="logout"
					href="${uGroup.createLink(controller:'logout', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }"><i
						class="icon-off"></i><g:message code="button.logout" /></a></li>

			</ul></li>


	</sec:ifLoggedIn>
</ul>

<%--<div class="login-box">--%>
<%--	<div class="register">--%>
<%--		<sec:ifNotLoggedIn>--%>
<%--			<g:link controller='login'><g:message code="button.login" /></g:link> | <g:link--%>
<%--				controller='register'><g:message code="button.register" /></g:link>--%>
<%--		</sec:ifNotLoggedIn>--%>
<%--	</div>--%>
<%----%>
<%--	<span class='loginLink'> <sec:ifLoggedIn>--%>
<%--			<sUser:renderProfileLink />--%>
<%--		</sec:ifLoggedIn> </span>--%>
<%--	<div class="login-box-options" style="display: none;">--%>
<%--		<sec:ifLoggedIn>--%>
<%--			<a id="logout" href="${createLink(controller:'logout')}"><g:message code="button.logout" /></a>--%>
<%--		</sec:ifLoggedIn>--%>
<%--	</div>--%>
<%--</div>--%>
