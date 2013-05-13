<%@page import="species.utils.Utils"%>
<div id="ibp-header" class="gradient-bg">
	<div id="ibp-header-bar" class="navbar navbar-static-top" style="margin-bottom: 0px;">
		<div class="navbar-inner"
			style="box-shadow: none; background-color: #dddbbb; background-image: none; padding: 0px 5px;  filter: progid :DXImageTransform.Microsoft.gradient (startColorstr = '#dddbbb', endColorstr = '#dddbbb' );">
			<div class="container" style="width: 100%">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> </a> <a
					class="brand"
					href="${createLink(url:Utils.getIBPServerDomain()) }">
					India Biodiversity Portal</a>
				
				<div class="nav-collapse">
				
				
					<ul class="nav pull-right">
						
						
						<li><uGroup:showSidebar /></li>
<%--						<li><sUser:userLoginBox--%>
<%--								model="['userGroup':userGroupInstance]" /></li>--%>

					</ul>
				</div>
			</div>

		</div>
	</div>
	<domain:showHeader model="['userGroupInstance':userGroupInstance]" />
         <div class="">
<%
String supportEmail = "";
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	supportEmail = grailsApplication.config.speciesPortal.wgp.supportEmail;
} else if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>


        <div id="contributeMenu" class="collapse">
                        <div class="container">
                            <ul style="list-style:none;">
                                <li>
                                Do you have an interesting picture of a species ... you can share it by uploading it here
                                <a class="btn btn-success"
                                    href="${uGroup.createLink(
                                    controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" class="btn btn-info"> <i class="icon-plus"></i>Add an Observation</a>
                                </li>
                                <li>
                                or have any document related to biodiversity like any project report or presentations or posters share them here
                                <a class="btn btn-success"
                                    href="${uGroup.createLink(
                                    controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                    class="btn btn-info" title="Add Document">
                                    <i class="icon-plus"></i> Add Document
                                </a>
                                </li>
                                <li>Every small bit of information helps in planning biodiversity conservation. So please contribute and if you have any suggestions or feedback please don't hesistate in share it with us at <span class="mailme">${supportEmail}</span>
                                </li>
                            </ul>
                        </div>

                    </div>

	<g:if test="${flash.error}">
		<div class="alertMsg alert alert-error" style="clear: both;margin-bottom:0px">
			${flash.error}
		</div>
	</g:if>

	<div class="alertMsg ${(flash.message)?'alert':'' }"
		style="clear: both; margin: 0px">
		${flash.message}
	</div>

	<auth:ajaxLogin />
	<div id="fb-root"></div>
    </div>
	<g:set var="fbAppId" value="" />
	<%
if(domain.equals(grailsApplication.config.wgp.domain)) {
	fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
	fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
}
%>

	<g:javascript>

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
								$.ajax({
								  url: "${uGroup.createLink(controller:'login', action:'authSuccess')}",
								  method:"GET",
								  data:{'uid':response.authResponse.userID, ${targetUrl?'"spring-security-redirect":"'+targetUrl+'"':''}},
								  success: function(data, statusText, xhr) {
								  	ajaxLoginSuccessHandler(data, statusText, xhr);
								  },  error: function(xhr, ajaxOptions, thrownError) {
              						$('#loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
              					  }
								});
							} else{
								var redirectTarget = ${targetUrl?'"&spring-security-redirect='+targetUrl+'"':'""'};
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
<%--			authParams["openid.return_to"] = 'http://indiabiodiversity.localhost.org/biodiv/j_spring_openid_security_check' --%>
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
		
	</g:javascript>
	<g:javascript>
	
	  var _gaq = _gaq || [];
	
	    if (document.domain == "${grailsApplication.config.ibp.domain}"){
	        _gaq.push(['_setAccount', 'UA-3185202-1']);
	    } else {
	        _gaq.push(['_setAccount', 'UA-23009417-1']);
	    }
	
	  _gaq.push(['_trackPageview']);
	
	  (function() {
	    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
	    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
	    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
	  })();
	
	</g:javascript>
</div>
