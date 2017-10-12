<head>
<meta name="layout" content="main" />
<title><g:message code="button.login" /></title>
</head>

<body>
	<div class="openid-loginbox super-section">

        <div>
    	<auth:loginForm model="['isSubGroup':true]"/>
        </div>
	</div>

<asset:script>
$(document).ready(function() {
    isLoggedIn();
});
</asset:script>
</body>
