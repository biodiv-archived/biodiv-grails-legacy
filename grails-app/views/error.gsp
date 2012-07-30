<html>
<head>
<title>Error</title>
<g:javascript src="species/util.js" />
<meta name="layout" content="main" />
</head>

<body>
	<div class="container_12 container outer-wrapper">
		<div class="row">
			<div class="observation  span12 ui-state-error">
				<p class="message">
					Oops!!! There seems to be some problem. <br /> Please mail us the
					following message as bug report here <span class="mailme">team(at)thewesternghats(dot)in</span><br />
					<br />
				</p>
				<div class="errors">
					<strong>Error ${request.'javax.servlet.error.status_code'}:</strong>
					${request.'javax.servlet.error.message'.encodeAsHTML()}<br />
				</div>
			</div>
		</div>


	</div>
</body>
</html>
