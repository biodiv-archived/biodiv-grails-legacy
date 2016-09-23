<%@page import="species.utils.Utils"%>
<div id="ibp-footer" class="gradient-bg-reverse navbar">
	<!--table border="0" width="940" align="center">
      <tbody><tr>
      <td align="center">
      
      &nbsp;&nbsp;
      <a href="http://www.cirad.fr" target="_blank"><asset:image src="/all/partnerlogo/Cirad_sans_texte.jpg" absolute="true" class="partner_lg"/> </a>
      &nbsp;&nbsp;
      <a href="http://www.ifpindia.org" target="_blank"><asset:image src="/all/partnerlogo/IFP-anglais_logo.png" absolute="true" class="partner_lg"/></a>
      &nbsp;&nbsp;
      <a href="http://www.fofifa.mg" target="_blank"><asset:image src="/all/partnerlogo/FOFIFA_logo.png" absolute="true" class="partner_lg"/></a>
      &nbsp;&nbsp;
      <a href="http://www.msiri.mu/" target="_blank"><asset:image src="/all/partnerlogo/MCIA-LOGO1.png" absolute="true" class="partner_lg"/></a>
      &nbsp;&nbsp;
      <a href="http://www.msiri.mu/" target="_blank"><asset:image src="/all/partnerlogo/MSIRI-2_logo.png" absolute="true" class="partner_lg"/></a>
      &nbsp;&nbsp;
      <a href="http://www.cndrs-comores.org" target="_blank"><asset:image src="/all/partnerlogo/CNDRS2_logo.png" absolute="true" class="partner_lg"/></a>
      </td>
      </tr>
      <tr>
      <td align="center">
      &nbsp;&nbsp;
      <a href="http://www.europa.eu/index_fr.htm" target="_blank"><asset:image src="/all/partnerlogo/UE-TC_logo.png" absolute="true" class="partner_lg"/></a>
      &nbsp;&nbsp;
      <a href="http://www.acp-st.eu/" target="_blank"><asset:image src="/all/partnerlogo/acp-st_logo.png" absolute="true" class="partner_lg"/></a>
      &nbsp;&nbsp;
      <a href="http://www.acp.int/" target="_blank"><asset:image src="/all/partnerlogo/ACP.jpg" absolute="true" class="partner_lg"/></a>
      </td>
      </tr>
      </tbody></table-->
	<!--div class="container outer-wrapper" style="width:940px">
		<div class="links_box_column">
			<ul>
				<li
					class=" nav-header bold${(params.controller == 'species')?' active':''}"><a
					href="${uGroup.createLink(controller:'species', action:'list')}"
					title="Species">All Species</a>
				</li>
				<li
					class="nav-header bold ${(request.getHeader('referer')?.contains('/map'))?' active':''}"><a
					href="${ '/map'}" title="Maps">All Maps</a></li>
				<li
					class=" nav-header bold${(params.controller == 'checklist')?' active':''}"><a
					href="${uGroup.createLink(controller:'checklist', action:'list')}" title="Checklists">All Checklists</a></li>
			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/theportal'>The Portal</a></li>
				<li><a href="${ '/about/whats-new'}">What's new?</a></li>
				<li><a href="${ '/about/technology'}">Technology</a></li>
				<li><a href="${ '/help/faqs'}">FAQ</a></li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/people'>People</a></li>
				<li><a href="${ '/people/partners'}">Partners</a></li>
				<li><a href="${ '/people/donors'}">Donors</a></li>
				<li><a href="${ '/people/fraternity'}">Fraternity</a></li>
				<li><a href="${ '/people/team'}">Team</a></li>
			</ul>
		</div>

		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/policy'>Policy</a>
				</li>
				<li><a href="${ '/policy/data_sharing'}">Data Sharing</a>
				</li>
				<li><a href="${ '/licenses'}">Licenses</a>
				</li>
				<li><a href="${ '/terms'}">Terms & Conditions</a>
				</li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold" style="color:#5E5E5E; padding-left: 0px;">Others</li>
				<li><a href="${ '/sitemap'}">Sitemap</a>
				</li>
				<li><a href="${ '/feedback_form'}">Feedback</a>
				</li>
				<li><a href="${ '/contact'}">Contact Us</a>
				</li>

			</ul>
		</div>



            </div-->
<!--div class="powered" style="text-align:center;">
	<p> 
${g.message(code:'text.supported.on')}<br />
 ${g.message(code:'text.powered.by')}<a href="https://github.com/strandls/biodiv" target="_blank"> ${g.message(code:'link.informatics.platform')}</a> ${g.message(code:'text.technology.partner')} <a href="http://www.strandls.com/strand-foundation/biodiversity" target="_blank" > ${g.message(code:'technology.partner.strandlifesciences')} </p>
	</div-->
</div>
<asset:script>
$(document).ready(function(){
	$(".youtube_container").each(function(){
		loadYoutube(this);
	});
	
	last_actions();

});

</asset:script>

<g:set var="fbAppId" value="" />
<%
if(domain.equals(grailsApplication.config.wgp.domain)) {
fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
}
%>

<asset:script>

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
                                                        window.location = "${uGroup.createLink(controller:'login', action:'authSuccess')}"+"?uid="+response.authResponse.userID+redirectTarget
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
        js.src = "//connect.facebook.net/en_US/all.js";//#xfbml=1&appId=${fbAppId}

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
        
</asset:script>

<asset:script>
//Twitter
!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');

//google plus
(function() {
var po = document.createElement('script'); 
po.type = 'text/javascript'; po.async = true;
po.src = 'http://apis.google.com/js/plusone.js?onload=renderGooglePlus';
var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
})();
</asset:script>
<asset:script>

 (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-48037338-1', 'wikwio.org');
  ga('send', 'pageview');
</asset:script>

<div id="ibp-foooter" class="gradient-bg-down navbar">

    <div class="container outer-wrapper" style="width:940px;margin-left:22%;">
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
        <!--div class="links_box_column">
            <ul>
                <li class="nav-header bold"  style="padding-left: 0px;"><a href='/page/48'><g:message code="link.the.portal" /></a></li>               
                <li><a href="#"><g:message code="link.biodiversity.india" /></a>
                </li>
                <li><a href="${ '/about/49'}"><g:message code="link.what.new" /></a></li>
                <li><a href="#"><g:message code="link.technology" /></a></li>
                <li><a href="${ '/page/43'}"><g:message code="link.faq" /></a></li>

            </ul>
        </div-->
        <div class="links_box_column">
            <ul>
                <li class="nav-header bold"  style="padding-left: 0px;"><g:message code="link.people" /></li>
                <li><a href="${'/wikwio/partners'}"><g:message code="link.partners" /></a></li>
                <li><a href="${'/wikwio/donors'}"><g:message code="link.donors" /></a></li>
                <!--li><a href="#"><g:message code="link.fraternity" /></a></li-->
                <li><a href="${'/wikwio/team'}"><g:message code="link.team" /></a></li>
            </ul>
        </div>

        <div class="links_box_column">
            <ul>
                <li class="nav-header bold"  style="padding-left: 0px;"><g:message code="link.policy" />
                </li>
                <li><a href="${ '/wikwio/datasharing'}"><g:message code="link.data.sharing" /></a>
                </li>
                <li><a href="${ '/wikwio/licenses'}"><g:message code="default.licenses.label" /></a>
                </li>
                <li><a href="${ '/wikwio/terms'}"><g:message code="link.terms.conditions" /></a>
                </li>

            </ul>
        </div>
        <div class="links_box_column">
            <ul>
                <li class="nav-header bold" style="color:#111; padding-left: 0px;"><g:message code="default.others.label" /></li>
                <li><a href="${ '/wikwio/citation'}"><g:message code="link.blog" /></a></li>
                <!--li><a href="#"><g:message code="link.sitemap" /></a>
                </li-->
                <!--li><a href="#"><g:message code="link.apidocs" /></a>
                </li-->

                <li><a href="${ '/wikwio/feedback'}"><g:message code="link.feedbak" /></a>
                </li>
                <li><a href="${ '/wikwio/contact'}"><g:message code="link.contact.us" /></a>
                </li>

            </ul>
        </div>



            </div>
    <div class="powered" style="text-align:center;">
        <p >
            <a href="https://play.google.com/store/apps/details?id=com.ifp.wikwio">
                <asset:image src="/all/googleplayicon1.png" class="mobile_app_icon" title="WIKWIO Portal Mobile App" absolute="true"/>
            </a>
            <a href="https://itunes.apple.com/us/app/wikwio-citizen-science/id1028388728?mt=8">
                <asset:image src="/all/apple_store1.png" class="mobile_app_icon" title="WIKWIO Portal Mobile App" absolute="true"/>
            </a> |
            <a href="https://play.google.com/store/apps/details?id=com.wikwio&hl=en">
                <asset:image src="/all/googleplayicon1.png" class="mobile_app_icon" title="WIKWIO IDAO Mobile App"absolute="true"/>
            </a>
            <a href="https://itunes.apple.com/us/app/wikwio-idao/id997358932?ls=1&mt=8">
                <asset:image src="/all/apple_store1.png" class="mobile_app_icon" title="WIKWIO IDAO Mobile App" absolute="true"/>
            </a>
        </p>
        <p> <!--a target="_blank" href="${grailsApplication.config.speciesPortal.app.facebookUrl}">
            <asset:image src="/all/facebook.png" absolute="true"/>
        </a> | <a  target="_blank" href="${grailsApplication.config.speciesPortal.app.twitterUrl}">
            <asset:image src="/all/twitter.png" absolute="true"/>
        </a>| <a  target="_blank" href="${grailsApplication.config.speciesPortal.app.googlePlusUrl}">
            <asset:image src="/all/google_plus.png" absolute="true"/>
        </a--><br />
<g:message code="text.supported.on" /><br />
<g:message code="text.powered.by" /> <a href="https://github.com/strandls/biodiv" target="_blank"><g:message code="link.informatics.platform" /></a>
<g:message code="text.technology.partner" /> <a href="http://www.strandlifefoundation.org/" target="_blank"><g:message code="technology.partner.strandlifesciences" /></a>
</p>
    </div>
</div>

