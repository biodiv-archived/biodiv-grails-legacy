<%@page import="species.utils.Utils"%>
<div id="ibp-footer" class="gradient-bg-reverse navbar">
<g:set var="userGroupService" bean="userGroupService"/>
<%
userGroupInstance = userGroupInstance && userGroupInstance.id ? userGroupInstance : null;
	def newsletters = userGroupService.getNewsLetters(userGroupInstance, -1, 0, "displayOrder", "asc", null, ['showInFooter':true]);
    def pages = [:];
    def pagesWithNoParent = [:];
    newsletters.each {
        if(it.parentId) {
            if(!pages[it.parentId]) pages[it.parentId] = [];
            pages[it.parentId] << it;
        } else {
            pagesWithNoParent[(int)it.id] = it;
        }
    }
%>
<div id="ibp-footer" class="gradient-bg-reverse navbar">
    <div class="container outer-wrapper" style="width:940px">
        <!--div class="links_box_column">
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
                    class=" nav-header bold${(params.controller == 'checklist')?' active':''}" style="width:200px;"><a
                    href="${uGroup.createLink(controller:'checklist', action:'list')}" title="Checklists">All Checklists</a></li>
            
            </ul>
        </div-->
        <g:each var="page" in="${pages}">
        	<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href="${userGroupInstance?uGroup.createLink('mapping':'userGroup', 'action':'page', 'id':pagesWithNoParent[page.key]?.id, 'userGroup':userGroupInstance):'/page/'+pagesWithNoParent[page.key]?.id }">${pagesWithNoParent[page.key]?.title}</a></li>
                <%pagesWithNoParent.remove(page.key)%>
                <g:each var="sub_page" in="${page.value}">
				    <li><a href="${userGroupInstance?uGroup.createLink('mapping':'userGroup', 'action':'page', 'id':sub_page.id, 'userGroup':userGroupInstance):'/page/'+sub_page.id }">${sub_page.title}</a></li>
                </g:each>
	        </ul>	
            </div>
        </g:each>
        <!--div class="links_box_column">
            <ul>
                <li class="nav-header bold"  style="padding-left: 0px;"><a href='/bbp/aboutus'>The Portal</a></li>
                <li style="width:200px;"><a href="${ '/bbp/theportal'}" >Biodiversity Of Bhutan</a>
                </li>
                <li><a href="${ '/bbp/technology'}">Technology</a></li>
                <li><a href="http://biodiversity.bt/page/170801">FAQ</a></li>

            </ul>
        </div>
        <div class="links_box_column">
            <ul>
                <li class="nav-header bold"  style="padding-left: 0px;"><a href='/bbp/people'>People</a></li>
                <li><a href="${ '/bbp/partners'}">Partners</a></li>
                <li><a href="${ '/bbp/donors'}">Donors</a></li>
                <li><a href="${ '/bbp/team'}">Team</a></li>
            </ul>
        </div>

        <div class="links_box_column">
            <ul>
                <li class="nav-header bold"  style="padding-left: 0px;"><a href='/bbp/policy'>Policy</a>
                </li>
                <li><a href="${ '/bbp/datasharing'}">Data Sharing</a>
                </li>
                <li><a href="${ '/bbp/license'}">Licenses</a>
                </li>

            </ul>
        </div>
        <div class="links_box_column">
            <ul>
                <li class="nav-header bold" style="padding-left: 0px;">Others</li>
                <li><a href="${ '/bbp/feedback'}">Feedback</a>
                </li>
                <li><a href="${ '/bbp/contactus'}">Contact Us</a>
                </li>

            </ul>
        </div-->
        
        <div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/page/415184'><g:message code="link.policy" /></a>
				</li>
				<li><a href="/page/415185"><g:message code="link.data.sharing" /></a>
				</li>
				<li><a href="/page/415189"><g:message code="default.licenses.label" /></a>
				</li>
			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold" style="color:#5E5E5E; padding-left: 0px;"><g:message code="default.others.label" /></li>
        <g:if test="${pagesWithNoParent}">
                <g:each var="page" in="${pagesWithNoParent}">
				    <li><a href="">${page.value.title}</a></li>
                </g:each>
        </g:if>

                
				
                <li><a href="${ '/biodiv/docs'}"><g:message code="link.apidocs" /></a>
				</li>

				<li><a href="${ '/page/415197'}"><g:message code="link.feedbak" /></a>
				</li>
				<li><a href="${ '/page/415210'}"><g:message code="link.contact.us" /></a>
				</li>

			</ul>
		</div>
   </div>
   <div class="powered" style="text-align:center;">
        Powered by the open source <a href="https://github.com/strandls/biodiv" target="_blank">Biodiversity Informatics Platform.</a></p>
    </div>
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
                'returnToUrl' : "${uGroup.createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
                'onCloseHandler' : closeHandler,
                'shouldEncodeUrls' : true,
                'extensions' : extensions
        });
        
        var yahooOpener = popupManager.createPopupOpener({
                'realm' : 'http://*.'+"${Utils.getIBPServerCookieDomain()}",
                'opEndpoint' : 'https://open.login.yahooapis.com/openid/op/auth',
                'returnToUrl' : "${uGroup.createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
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
po.src = 'https://apis.google.com/js/plusone.js?onload=renderGooglePlus';
var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
})();
</asset:script>
<asset:script>

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

</asset:script>
