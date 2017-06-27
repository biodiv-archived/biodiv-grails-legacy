
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
            <iframe id="biodiv_iframe " name="biodiv_iframe" class="hide" src="" width="450px" height="280px"   allowTransparency="true" frameborder="0" scrolling="yes" style="width:100%;" ></iframe>
            <asset:script type="text/javascript">
                $(document).ready(function() {
                    $('.openid-loginbox form').on('submit', function(e) {
                        $('body').addClass('busy');
                        e.stopPropagation();
                        var isAjax = $("#ajaxLogin").is(':visible'); 
                        loadBiodivLoginIframe(function() {
                            var iframe = document.getElementsByName("biodiv_iframe")[0];
                            if(iframe) {
                                var iframeWindow = (iframe.contentWindow || iframe.contentDocument);
                                iframeWindow.postMessage(JSON.stringify({'regular_login':'true', 'j_username':$('.isSubGroup input[name="j_username"]').val(),'j_password':$('.isSubGroup input[name="j_password"]').val(), isAjax:isAjax}), '*');
                            }
                        });
                        return false;
                    });

                    $('.isSubGroup .fbJustConnect, .isSubGroup .googleConnect, .isSubGroup .yahooConnect' ).click(function(event) {
                        $('body').addClass('busy');
                        event.stopPropagation();
                        var isAjax = $("#ajaxLogin").is(':visible'); 
                        loadBiodivLoginIframe(function() {
                            var iframe = document.getElementsByName("biodiv_iframe")[0];
                            if(iframe) {
                                var iframeWindow = (iframe.contentWindow || iframe.contentDocument);
                                console.log(event);
                                var className = event.currentTarget.className.split(' ')[0]+'_login';
                                iframeWindow.postMessage(JSON.stringify({[className]:'true', 'isAjax':isAjax}), '*');
                            }
                        });
                        return false;
                    });
                   

                    // Create IE + others compatible event handler
                    var eventMethod = window.addEventListener ? 'addEventListener' : 'attachEvent';
                    var eventer = window[eventMethod];
                    var messageEvent = eventMethod == 'attachEvent' ? 'onmessage' : 'message';

                    // Listen to message from child window
                    eventer(messageEvent,function(e) {
                        console.log(event.origin);
                        if(event.origin != window.params.serverURL) 
                            return;

                        var childMsg = JSON.parse(e.data);
                        console.log('received message!:  ', JSON.parse(e.data));
                        if(childMsg.signin == 'success') {
                            console.log(childMsg.cookies);
                            $.each(childMsg.cookies, function(key,value) {
                                $.cookie(key, null, { path: '/' });
                                $.cookie(key, value, {path:'/'});
                            });
                            $('.loginMessage').html('').removeClass('alert alert-error').hide();
                            console.log(window.location);
                            if(childMsg.isAjax)
                                ajaxLoginSuccessHandler(childMsg.data, childMsg.statusText, childMsg.xhr);
                            else
                                window.location.href = window.location.origin;
                        } else if(childMsg.signin == 'error'){
                            $('.loginMessage').html(childMsg.error).addClass('alert alert-error').show();
                        }
                        $('body').removeClass('busy');

                    },false);

                });
            </asset:script>
            </g:if>

	</div>
</div>
