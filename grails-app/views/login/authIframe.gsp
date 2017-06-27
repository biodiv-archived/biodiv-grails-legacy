<%@page import="species.utils.Utils"%>
<html>
<head>
<title></title>
<asset:stylesheet href="application.css"/>
<style>
.modal.fade.in {
        top: -13%;
}
</style>
</head>
<body>
    <auth:ajaxLogin model="['isSubGroup':false]"/>

<asset:javascript src="jquery.js"/>
<asset:javascript src="jquery.plugins/jquery.form.js"/>
<asset:javascript src="jquery.plugins/popuplib.js"/>
<asset:javascript src="jquery.plugins/jquery.cookie.js"/>
<script src="https://apis.google.com/js/auth.js" type="text/javascript">
<asset:javascript src="biodiv/util.js"/>
<asset:javascript src="biodiv/login.js"/>


<script type="text/javascript">
window.params = {
    'login' : {
            googleOAuthSuccessUrl : "/oauth/google/success",
            ibpServerCookieDomain : "${Utils.getIBPServerCookieDomain()}",
            authSuccessUrl : "${uGroup.createLink(controller:'login', action:'authSuccess')}",
            checkauthUrl : "${uGroup.createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
            channelUrl : "${Utils.getDomainServerUrl(request)}/channel.html",
            springOpenIdSecurityUrl : "${Utils.getDomainServerUrlWithContext(request)}/j_spring_openid_security_check", 
            fbAppId : "${grailsApplication.config.speciesPortal.ibp.facebook.appId}"
        }
}

$(document).ready(function() {
        console.log('authiframe')
        <g:if test="${isLoggedIn}">
            window.top.postMessage( JSON.stringify({signin: 'success', 'cookies':getCookies()}), '*' );
        </g:if>
        <g:else>
            window.addEventListener("message", receiveMessage, false);
            window.isAjax = false;
            function receiveMessage(event) {
                //if (event.origin !== "http://example.org:8080")
                //    return;
                console.log(event);
                var data = JSON.parse(event.data);
                if(data.fbJustConnect_login == 'true') {
                    $('.isParentGroup .fbJustConnect').click();
                    window.isAjax = data.isAjax;
                } else if(data.googleConnect_login == 'true') {
                    $('.isParentGroup .googleConnect').click();
                    window.isAjax = data.isAjax;
                } else if(data.yahooConnect_login == 'true') {
                    $('.isParentGroup .yahooConnect').click();
                    window.isAjax = data.isAjax;
                } else if(data.regular_login == 'true') {
                    $('input[name="j_username"]').val(data.j_username);
                    $('input[name="j_password"]').val(data.j_password);
                    window.isAjax = data.isAjax;
                    $('#ajaxLogin form').submit();
                }
            }

            var ajaxLoginFormHandler = function(event) {
                $(this).ajaxSubmit({
                    type : 'POST',
                    dataType : 'json',
                    success : function(data, statusText, xhr){
                        console.log('success');
                        console.log(arguments);
                        if(data.success) {
                            window.top.postMessage( JSON.stringify({signin: 'success', 'cookies':getCookies(), 'isAjax':window.isAjax, 'data':data, 'statusText':statusText}), '*' );
                        } else {
                            window.top.postMessage( JSON.stringify({signin: 'error', 'error':data.error, 'isAjax':window.isAjax}), '*' );
                        }
                    }, error : function(xhr, ajaxOptions, thrownError) {
                        console.log('error');
                        console.log(arguments);
                    }                                        
                });
                event.preventDefault();
                return false;
            }
            
            $('#ajaxLogin form').bind('submit', ajaxLoginFormHandler);
            $('#ajaxLogin').show();
    </g:else>
});
</script>
</body>
</html>
