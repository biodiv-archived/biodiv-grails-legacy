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
<style>
.login-box {
	font-weight: normal;
<%--	height: 80px;--%>
	margin: 0;
	top: 0;
	max-width: 200px;
	float: right;
}

<sec:ifLoggedIn>.login-box:hover {
	background-color: #ffffff;
	box-shadow: 0 6px 8px -6px #5e5e5e;
}
</sec:ifLoggedIn>

.login-box-options {
	float: right;
	right: 0;
	padding-right: 10px;
	padding-bottom: 10px;
}

.login-box img {
	max-height: 32px;
	min-height: 16px;
	max-width: 32px;
}

.loginLink {
	margin: 0;
	position: relative;
	float: right;
}

.register {
	float: right;
	margin-right: 10px;
}

.user-icon {
	height: 32px;
	line-height: 32px;
	margin: 0 auto;
	text-align: center;
	width: 32px;
}

.figure {
	font-size: 80%;
	font-style: italic;
	position: relative !important;
	text-align: center;
}

.user_signature {
	height: 30px;
	padding-left: 5px;
	padding-top: 2px;
	text-align: left;
	max-width: 175px;
}

.story-footer .footer-item {
	float: left;
	margin-right: 10px;
}

.prop {
	margin-bottom: 5px;
	margin-top: 5px;
}
</style>
<ul class="nav">
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
					<li><a id="logout" href="${createLink(controller:'logout')}">Logout</a></li>
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
