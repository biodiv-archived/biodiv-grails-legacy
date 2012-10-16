<!DOCTYPE html>
<%@page import="species.groups.UserGroup"%>
<%@page import="species.utils.Utils"%>
<%@page
	import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>
<html lang="en" xmlns:fb="http://ogp.me/ns/fb#"
	xmlns:og="og: http://ogp.me/ns#">
<head>
<title>${Utils.getDomainName(request)}</title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<r:layoutResources/>
<ckeditor:resources/>
<g:set var="fbAppId" value="" />
<%
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
} else if(domain.equals(grailsApplication.config.ibp.domain)) {
	fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
}

%>
<g:if test="${domain.equals(grailsApplication.config.ibp.domain) }">
	<link rel="shortcut icon" href="/sites/default/files/ibp_favicon_2.png" type="image/x-icon" />
</g:if>
<g:else>
	<link rel="shortcut icon" href="/sites/all/themes/wg/images/favicon.png" type="image/x-icon" />
</g:else>
<g:javascript>
    window.appContext = '${request.contextPath}';
    window.appIBPDomain = '${grailsApplication.config.ibp.domain}'
    window.appWGPDomain = '${grailsApplication.config.wgp.domain}'
</g:javascript>

<g:layoutHead />

<!-- script src="http://cdn.wibiya.com/Toolbars/dir_1100/Toolbar_1100354/Loader_1100354.js" type="text/javascript"></script><noscript><a href="http://www.wibiya.com/">Web Toolbar by Wibiya</a></noscript-->


</head>
<body>
	<div id="loading" class="loading" style="display: none;">
		<span>Loading ...</span>
	</div>
	
	<auth:ajaxLogin />
	<div id="fb-root"></div>
	<%
		def userGroupInstance;
		if(params.userGroup) {
			userGroupInstance = UserGroup.get(params.long('userGroup'));
		} else if(params.webaddress) {
			userGroupInstance = UserGroup.findByWebaddress(params.webaddress);
		}
	%>
	
	<div id="species_main_wrapper" style="clear:both;">
		<domain:showIBPHeader model="['userGroupInstance':userGroupInstance]"/>

		<div class="container outer-wrapper">
				<div>
				<div style="padding:10px 0px">
					<g:layoutBody />
				</div>
				</div>
		</div>

		
		<domain:showIBPFooter />

	</div>
	
	
	<r:script>

		$(document).ready(function(){
			
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
		
			$(".ui-icon-edit").click(function() {
				var ele =$(this).siblings("div.toolbarIconContent").find("textArea.fieldEditor");
				if(ele) { 
					ele.ckeditor(function(){}, {customConfig:"${resource(dir:'js',file:'ckEditorConfig.js')}"});
					CKEDITOR.replace( ele.attr('id') );
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
							$.cookie("fb_login", "true", { path: '/', domain:".${Utils.getDomain(request)}"});
							if($(clickedObject).hasClass('ajaxForm')) {
								$('#loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
								$.ajax({
								  url: "${createLink(controller:'login', action:'authSuccess')}",
								  method:"GET",
								  data:{'uid':response.authResponse.userID, ${params['spring-security-redirect']?'"spring-security-redirect":"'+params['spring-security-redirect']+'"':''}},
								  success: function(data, statusText, xhr) {
								  	ajaxLoginSuccessHandler(data, statusText, xhr);
								  }
								});
							} else{
								var redirectTarget = ${params['spring-security-redirect']?'"&spring-security-redirect='+params['spring-security-redirect']+'"':'""'};
								window.location = "${createLink(controller:'login', action:'authSuccess')}"+"?uid="+response.authResponse.userID+redirectTarget
							}
						} else {
							alert("Failed to connect to Facebook");
						}
					}, scope);
				});
			});
			
			var t = "${createLink(controller:params.controller?:'search', action:params.action?:'') }";
			//t = t.replace(/\//g, "\\\\/");
			//console.log(t);
			$("#searchResultsTabs a[href='"+t+"']").parent().addClass("active");
			
			$('#searchResultsTabs a').click(function (e) {
				$( "#searchbox" ).attr('action', $(this).attr('href')).submit();
				e.preventDefault();
			})
			
			$(".ellipsis").trunk8();
			 
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
<%--			authParams["openid.return_to"] = 'http://indiabiodiversity.localhost.org/biodiv/j_spring_openid_security_check' --%>
			 $.ajax({
              url:  "${Utils.getDomainServerUrl(request)}/j_spring_openid_security_check" ,
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
			'realm' : "${Utils.getDomainServerUrl(request)}",
			'opEndpoint' : 'https://www.google.com/accounts/o8/ud',
			'returnToUrl' :	"${createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
			'onCloseHandler' : closeHandler,
			'shouldEncodeUrls' : true,
			'extensions' : extensions
		});
		
		var yahooOpener = popupManager.createPopupOpener({
			'realm' : "${Utils.getDomainServerUrl(request)}",
			'opEndpoint' : 'https://open.login.yahooapis.com/openid/op/auth',
			'returnToUrl' :	"${createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
			'onCloseHandler' : closeHandler,
			'shouldEncodeUrls' : true,
			'extensions' : extensions
		});
		
	</r:script>
	<r:script>
	
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
	
	</r:script>
		
	<r:layoutResources/>
</body>
</html>
