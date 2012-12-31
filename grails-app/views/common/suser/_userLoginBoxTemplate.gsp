<%@page import="species.utils.Utils"%>
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

<ul class="nav header_userInfo">
	<sec:ifNotLoggedIn>
		<li><a href="${uGroup.createLink(controller:'login', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }">Login</a>
		</li>
		
		<li><g:link controller='register'>Register</g:link>
		</li>
	</sec:ifNotLoggedIn>
	<sec:ifLoggedIn>
		<li class="dropdown">
			<a href="#" class="dropdown-toggle" data-toggle="dropdown" style="float:right;">
				<b class="caret" style="border-top-color: black;border-bottom-color: black;"></b>
			</a>	
				<ul class="dropdown-menu">
					<li><a id="logout" href="${uGroup.createLink(controller:'logout', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }">Logout</a></li>
				</ul>
			<div class="login-box"><sUser:renderProfileLink /></div>
		</li>
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
