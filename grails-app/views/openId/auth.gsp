<head>
<meta name="layout" content="main" />
<r:require modules="auth"/>
<title>Login</title>
</head>

<body>

	<div class="container outer-wrapper">
		<div class="row">

			<div class="openid-loginbox super-section">

				<g:if test="${flash.error}">
					<div class="alert alert-error">
						${flash.error}
					</div>
				</g:if>
				
				<g:if test="${flash.message}">
					<div class="alert alert-error">
						${flash.message}
					</div>
				</g:if>
				<auth:loginForm/>
			</div>
		</div>
	</div>
</body>
