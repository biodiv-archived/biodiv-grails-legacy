<html>
<head>
<g:set var="title" value="Email Verification Sent"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="auth" />
</head>
<style>
form {
   padding-top: 10px;
}
</style>
<body>
<div class="alert alert-success">
<i><g:message code="register.resend.verifications.email" /> </i>
</div>

</body>
