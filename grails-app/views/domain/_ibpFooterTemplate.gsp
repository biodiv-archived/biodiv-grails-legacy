<%@page import="species.utils.Utils"%>
<div id="ibp-footer" class="gradient-bg-reverse navbar">

	<div class="container outer-wrapper" style="width:940px">
		<div class="links_box_column">
			<ul>
				<li
					class=" nav-header bold${(params.controller == 'species')?' active':''}"><a
					href="${uGroup.createLink(controller:'species', action:'list')}"
					title="Species"><g:message code="link.all.species" /></a>
				</li>
				<li
					class="nav-header bold ${(request.getHeader('referer')?.contains('/map'))?' active':''}"><a
					href="${ '/map'}" title="Maps"><g:message code="link.all.maps" /></a></li>
				<li
					class=" nav-header bold${(params.controller == 'checklist')?' active':''}"><a
					href="${uGroup.createLink(controller:'checklist', action:'list')}" title="Checklists"><g:message code="link.all.checklists" /> </a></li>
				<!-- li
					class="nav-header bold ${(params.controller == 'userGroup' && params.action== 'list')?' active':''}"><a
					href="${ uGroup.createLink(controller:"userGroup", "action":"list")}"
					title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality.">All
						Groups<sup>Beta</sup> </a>
				</li-->
			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/theportal'><g:message code="link.the.portal" /></a></li>
				<li><a href="${ '/biodiversity_in_india'}"><g:message code="link.biodiversity.india" /></a>
				</li>
				<li><a href="${ '/about/whats-new'}"><g:message code="link.what.new" /></a></li>
				<li><a href="${ '/about/technology'}"><g:message code="link.technology" /></a></li>
				<li><a href="${ '/help/faqs'}"><g:message code="link.faq" /></a></li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/people'><g:message code="link.people" /></a></li>
				<li><a href="${ '/people/partners'}"><g:message code="link.partners" /></a></li>
				<li><a href="${ '/people/donors'}"><g:message code="link.donors" /></a></li>
				<li><a href="${ '/people/fraternity'}"><g:message code="link.fraternity" /></a></li>
				<li><a href="${ '/people/team'}"><g:message code="link.team" /></a></li>
			</ul>
		</div>

		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/policy'><g:message code="link.policy" /></a>
				</li>
				<li><a href="${ '/policy/data_sharing'}"><g:message code="link.data.sharing" /></a>
				</li>
				<li><a href="${ '/licenses'}"><g:message code="default.licenses.label" /></a>
				</li>
				<li><a href="${ '/terms'}"><g:message code="link.terms.conditions" /></a>
				</li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold" style="color:#5E5E5E; padding-left: 0px;"><g:message code="default.others.label" /></li>
				<li><a href="${ '/sitemap'}"><g:message code="link.sitemap" /></a>
				</li>
                <li><a href="${ '/biodiv/docs'}"><g:message code="link.apidocs" /></a>
				</li>

				<li><a href="${ '/feedback_form'}"><g:message code="link.feedbak" /></a>
				</li>
				<li><a href="${ '/contact'}"><g:message code="link.contact.us" /></a>
				</li>

			</ul>
		</div>



            </div>
    <div class="powered" style="text-align:center;">
    <p title="India Biodiversity Portal Mobile App"><a href="https://play.google.com/store/apps/details?id=com.mobisys.android.ibp&hl=en"><img src="${resource(dir:'images',file:'googleplayicon1.png', absolute:true)}"></a></p>
	<p> <a target="_blank" href="${grailsApplication.config.speciesPortal.app.facebookUrl}"><img src="${resource(dir:'images',file:'facebook.png', absolute:true)}"></a> | <a  target="_blank" href="${grailsApplication.config.speciesPortal.app.twitterUrl}"><img src="${resource(dir:'images',file:'twitter.png', absolute:true)}"></a>| <a  target="_blank" href="${grailsApplication.config.speciesPortal.app.googlePlusUrl}"><img src="${resource(dir:'images',file:'google_plus.png', absolute:true)}"></a><br />
<g:message code="text.supported.on" /><br />
<g:message code="text.powered.by" /> <a href="https://github.com/strandls/biodiv" target="_blank"><g:message code="link.informatics.platform" /></a>
<g:message code="text.technology.partner" /> <a href="http://www.strandls.com/strand-foundation/biodiversity" target="_blank"><g:message code="technology.partner.strandlifesciences" /></a>
</p>
	</div>
</div>
<r:script>
$(document).ready(function(){
	$(".youtube_container").each(function(){
		loadYoutube(this);
	});
	
	last_actions();

});

</r:script>

<g:set var="fbAppId" value="" />
<%
if(domain.equals(grailsApplication.config.wgp.domain)) {
fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
}
%>

<r:script>

        $(document).ready(function() {
                
                $(".ui-icon-control").click(function() {
                        var div = $(this).siblings("div.toolbarIconContent");
                        if (div.is(":visible")) {
                                div.hide(400);
                        } else {
                                div.slideDown("slow");	
                                if(div.offset().left < 0) {
                                        div.offset({left:div.parent().offset().left});					
                                }
                        }
                });
        
                $("a.ui-icon-close").click(function() {
                        $(this).parent().hide("slow");
                });
                
                
                $('.fbJustConnect').click(function() {
                        var clickedObject = this;
                        var scope = { scope: "" };
                        scope.scope = "email,user_about_me,user_location,user_activities,user_hometown,manage_notifications,user_website,publish_stream";
                        
                        window.fbEnsure(function() {
                                FB.login(function(response) {
                                        if (response.status == 'connected') {
                                                $.cookie("fb_login", "true", { path: '/', domain:".${Utils.getIBPServerCookieDomain()}"});
                                                if($(clickedObject).hasClass('ajaxForm')) {
                                                        $('#loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
                                                        var p = new Array();
                                                        p['uid'] = response.authResponse.userID;
                                                        <g:if test="${targetUrl}">
                                                            p['spring-security-redirect'] = '${targetUrl}'
                                                        </g:if>
                                                        $.ajax({
                                                            url: "${uGroup.createLink(controller:'login', action:'authSuccess')}",
                                                            method:"GET",
                                                            data:p,
                                                            success: function(data, statusText, xhr) {
                                                                ajaxLoginSuccessHandler(data, statusText, xhr);
                                                            },  error: function(xhr, ajaxOptions, thrownError) {
                                                                $('#loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
                                                            }
                                                        });
                                                } else{
                                                        var redirectTarget = "${targetUrl?'spring-security-redirect='+targetUrl:''}";
                                                        window.location = "${uGroup.createLink(controller:'login', action:'authSuccess')}"+"?uid="+response.authResponse.userID+'&'+redirectTarget
                                                }
                                        } else {
                                                alert("Failed to connect to Facebook");
                                        }
                                }, scope);
                        });
                });

        }); 

//////////////////////// FB RELATED CALLS ///////////////////////

window.fbInitCalls = Array();
window.fbAsyncInit = function() {	
        
        if (!window.facebookInitialized) { 
        FB.init({
            appId  : "${fbAppId}",
                    channelUrl : "${Utils.getDomainServerUrl(request)}/channel.html",
                    status : true,
                    cookie : true,
                    xfbml: true,
                    oauth  : true,
                    version: 'v2.2',
                    logging : true
        });
        window.facebookInitialized = true;
        }
$.each(window.fbInitCalls, function(index, fn) {
    fn();
});
window.fbInitCalls = [];
};

// make sure facebook is initialized before calling the facebook JS api
window.fbEnsure = function(callback) {
        if (window.facebookInitialized) { callback(); return; }
                
        if(!window.FB) {
                //alert("Facebook script all.js could not be loaded for some reason. Either its not available or is blocked.")
                window.fbInitCalls.push(callback);
        } else {
                window.fbAsyncInit();
                callback();
        }
};

(function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id))
                return;
        js = d.createElement(s);
        js.id = id;
        js.src = "//connect.facebook.net/en_US/sdk.js";//#xfbml=1&appId=${fbAppId}

        fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));
////////////////////////FB RELATED CALLS END HERE ///////////////////////


        
        function closeHandler() {
                $('#loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
                var authParams = window.mynewparams;
                console.log(authParams);
                    $.ajax({
        url:  "${Utils.getDomainServerUrlWithContext(request)}/j_spring_openid_security_check" ,
        method: "POST",
                    data: authParams,	
        success: function(data, statusText, xhr) {
        ajaxLoginSuccessHandler(data, statusText, xhr);
        },
        error: function(xhr, ajaxOptions, thrownError) {
        $('#loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
        }
    });
        };

        function setAttribute(node, name, value) {
                var attr = document.createAttribute(name);
                attr.nodeValue = value;
                node.setAttributeNode(attr);
        }

        var extensions = {
                'openid.ns.ax' : 'http://openid.net/srv/ax/1.0',
                'openid.ax.mode' : 'fetch_request',
                'openid.ax.type.email' : 'http://axschema.org/contact/email',
                'openid.ax.type.first' : 'http://axschema.org/namePerson/first',
                'openid.ax.type.last' : 'http://axschema.org/namePerson/last',
                'openid.ax.type.country' : 'http://axschema.org/contact/country/home',
                'openid.ax.type.lang' : 'http://axschema.org/pref/language',
                'openid.ax.type.web' : 'http://axschema.org/contact/web/default',
                'openid.ax.required' : 'email,first,last,country,lang,web',
                'openid.ns.oauth' : 'http://specs.openid.net/extensions/oauth/1.0',
                'openid.oauth.consumer' : 'www.puffypoodles.com',
                'openid.oauth.scope' : 'http://www.google.com/m8/feeds/',
                'openid.ui.icon' : 'true'
        };

        var googleOpener = popupManager.createPopupOpener({
                'realm' : 'http://*.'+"${Utils.getIBPServerCookieDomain()}",
                'opEndpoint' : 'https://www.google.com/accounts/o8/ud',
                'returnToUrl' :	"${uGroup.createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
                'onCloseHandler' : closeHandler,
                'shouldEncodeUrls' : true,
                'extensions' : extensions
        });
        
        var yahooOpener = popupManager.createPopupOpener({
                'realm' : 'http://*.'+"${Utils.getIBPServerCookieDomain()}",
                'opEndpoint' : 'https://open.login.yahooapis.com/openid/op/auth',
                'returnToUrl' :	"${uGroup.createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
                'onCloseHandler' : closeHandler,
                'shouldEncodeUrls' : true,
                'extensions' : extensions
        });
        
</r:script>

<r:script>
//Twitter
!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');

//google plus
(function() {
var po = document.createElement('script'); 
po.type = 'text/javascript'; po.async = true;
po.src = 'http://apis.google.com/js/plusone.js?onload=renderGooglePlus';
var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
})();
</r:script>
<r:script>

    var _gaq = _gaq || [];

    if (document.domain == "${grailsApplication.config.ibp.domain}"){
        _gaq.push(['_setAccount', 'UA-3185202-1']);
    } else {
        _gaq.push(['_setAccount', 'UA-23009417-1']);
    }

    _gaq.push(['_setDomainName', 'indiabiodiversity.org']);
    _gaq.push(['_trackPageview']);

    (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();

</r:script>


