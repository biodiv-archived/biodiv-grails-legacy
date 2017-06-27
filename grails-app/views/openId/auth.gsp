<head>
<meta name="layout" content="main" />
<title><g:message code="button.login" /></title>
</head>

<body>
	<div class="openid-loginbox super-section">

        <div>
    	<auth:loginForm model="['isSubGroup':(userGroupInstance && userGroupInstance.domainName)]"/>
        </div>
	</div>
</body>
