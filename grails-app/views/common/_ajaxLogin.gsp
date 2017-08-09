
<style>
#ajaxLogin .modal {
	z-index : 10000;
}
</style>
<div id="ajaxLogin" class="modal hide fade in ${isSubGroup?'isSubGroup':'isParentGroup'}" style="display: none;z-index:3000;" tabindex='-1' aria-hidden="true">
	<div class="modal-body">
		<div class="openid-loginbox super-section">
			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			
			<auth:loginForm model="['openIdPostUrl': openIdPostUrl,
					'daoPostUrl':daoPostUrl,
					'persistentRememberMe': persistentRememberMe,
					'rememberMeParameter': rememberMeParameter,
					'openidIdentifier': openidIdentifier, 'ajax':true, 'isSubGroup':isSubGroup]"/>
		</div>

        <g:if test="${userGroupInstance && userGroupInstance.domainName}">
            <!--iframe id="biodiv_iframe " name="biodiv_iframe" class="hide" src="" width="450px" height="280px"   allowTransparency="true" frameborder="0" scrolling="yes" style="width:100%;"></iframe-->
            <asset:script type="text/javascript">
                /*$(document).ready(function() {
                    // Create IE + others compatible event handler
                    var eventMethod = window.addEventListener ? 'addEventListener' : 'attachEvent';
                    var eventer = window[eventMethod];
                    var messageEvent = eventMethod == 'attachEvent' ? 'onmessage' : 'message';

                    // Listen to message from child window
                    eventer(messageEvent,function(e) {
                        if(event.origin != window.params.serverURL) 
                            return;

                        var childMsg = JSON.parse(e.data);
                        if(childMsg.signin == 'success') {
                            clearAllCookies();
                            $.each(childMsg.cookies, function(key,value) {
                                $.cookie(key, null, { path: '/' });
                                $.cookie(key, value, {path:'/'});
                            });
                            $('.loginMessage').html('').removeClass('alert alert-error').hide();
                            if(childMsg.isAjax)
                                ajaxLoginSuccessHandler(childMsg.data, childMsg.statusText, childMsg.xhr);
                            else
                                window.location.href = childMsg.referer ? childMsg.referer :  window.location.origin;
                        } else if(childMsg.signin == 'error'){
                            $('.loginMessage').html(childMsg.error).addClass('alert alert-error').show();
                        } else if(childMsg.signout == 'success'){
                            clearAllCookies();
                           $.each(childMsg.cookies, function(key,value) {
                                $.cookie(key, value, {path:'/'});
                            });
                            window.location.href = window.location.origin;
                        }
                        $('body').removeClass('busy');

                    },false);

                });*/
            </asset:script>
            </g:if>

	</div>
</div>
