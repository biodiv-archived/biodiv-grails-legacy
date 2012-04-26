<!DOCTYPE html>
<%@page
	import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>
<html lang="en" xmlns:fb="http://ogp.me/ns/fb#"
	xmlns:og="og: http://ogp.me/ns#">
<head>
<title>Species Portal</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />


<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'bootstrap/css',file:'bootstrap.min.css')}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'jquery-ui.css')}" />

<!-- r:require module="jquery-ui" /-->

<!-- r:layoutResources /-->
<!-- sNav:resources override="true" /-->

<script
	src="${resource(dir:'plugins',file:'jquery-1.7/js/jquery/jquery-1.7.min.js')}"
	type="text/javascript"></script>
<script
	src="${resource(dir:'plugins',file:'jquery-ui-1.8.15/jquery-ui/js/jquery-ui-1.8.15.custom.min.js')}"
	type="text/javascript"></script>

<g:if test="${params.controller  != 'species'}">
	<g:javascript src="../bootstrap/js/bootstrap.min.js" contextPath=""></g:javascript>
</g:if>

<g:javascript src="species/main.js" />

<link rel="stylesheet" type="text/css" media="screen"
	href="${resource(dir:'js/jquery/jquery.jqGrid-4.1.2/css',file:'ui.jqgrid.css')}" />

<link rel="stylesheet" type="text/css"
	href="${resource(dir:'css',file:'auth.css')}" />
<link rel="stylesheet" media="screen"
	href="${resource(dir:'css',file:'spring-security-ui.css',plugin:'spring-security-ui')}" />
<link rel="stylesheet" media="screen"
	href="${resource(dir:'css',file:'jquery.safari-checkbox.css',plugin:'spring-security-ui')}" />

<!-- link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'reset.css')}" /-->
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'text.css')}" />


<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'960.css')}" />
<g:if test="${params.controller == 'species'}">
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
</g:if>

<link rel="stylesheet" type="text/css"
	href="${resource(dir:'css',file:'navigation.css')}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'jquery.rating.css')}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'wgp.css')}" />

<g:javascript src="jquery/jquery.form.js"></g:javascript>
<g:javascript src="jquery/jquery.rating.js"></g:javascript>
<g:javascript src="readmore/readmore.js" />
<g:javascript src="jquery/jquery.cookie.js"></g:javascript>

<g:javascript src='jquery/jquery.checkbox.js'
	plugin='spring-security-ui' />
<g:javascript src='spring-security-ui.js' plugin='spring-security-ui' />
<ckeditor:resources/>

<g:javascript>
jQuery(document).ready(function($) {
        if (document.domain == "${grailsApplication.config.wgp.domain}"){
            $('#ibp-header').hide();
            $('#wgp-header').show();
            $('#ibp-footer').hide();
            $('#wgp-footer').show();
        }

        if (document.domain == "${grailsApplication.config.ibp.domain}"){
            $('#wgp-header').hide();
            $('#ibp-header').show();
            $('#wgp-footer').hide();
            $('#ibp-footer').show();
        }


	$("#menu .navigation li").hover(
  		function () {
    		$(".subnavigation", this).show();
  		}, 
  		function () {
    		$(".subnavigation", this).hide();
  		}
	);
	$.widget( "custom.catcomplete", $.ui.autocomplete, {
					_renderMenu: function( ul, items ) {
						var self = this,
							currentCategory = "";
						$.each( items, function( index, item ) {
							if ( item.category != currentCategory ) {
								ul.append( "<li class='ui-autocomplete-category'>" +
		item.category + "</li>" );
								currentCategory = item.category;
							}
							self._renderItem( ul, item );
						});
					}
				});
});
var reloadLoginInfo = function() {
			$.ajax({
				url : "${createLink(controller:'SUser', action:'login')}",
				success : function(data) {
					$('.header:visible .header_userInfo').html(data);
				}
			});
		}
</g:javascript>

<g:layoutHead />
<ga:trackPageview />

<!-- script src="http://cdn.wibiya.com/Toolbars/dir_1100/Toolbar_1100354/Loader_1100354.js" type="text/javascript"></script><noscript><a href="http://www.wibiya.com/">Web Toolbar by Wibiya</a></noscript-->

<style>
#header {
	background-color: #F7F7F7;
	height: 80px;
	width: 100%;
	z-index: 2000;
	font-family: Verdana, Helvetica, Sans-Serif;
	color: #5E5E5E;
	box-shadow: 0 6px 6px -6px #5E5E5E;
	border-bottom: 1px solid #E5E5E5;
}

#wg_logo {
	border: 0 none;
	height: 80px;
	width: auto;
}

#top_nav_bar {
	font-size: 1em;
	font-weight: bold;
	left: 300px;
	position: absolute;
	top: 30px;
	z-index: 501;
}

#top_nav_bar ul {
	list-style: none outside none;
	margin-top: 14px;
	margin-bottom: 14px;
	font-size: 1.1em;
	padding-left: 40px;
}

#top_nav_bar li {
	cursor: pointer;
	display: inline;
	padding: 10px 10px 3px;
}

#top_nav_bar li:hover {
	background-color: #e8f7f1;
	border-bottom: 3px solid #003846;
}
</style>

<script type="text/javascript">

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

</script>

</head>
<body>

	<div id="loading" class="loading" style="display: none;">
		<span>Loading ...</span>
	</div>

	<domain:showWGPHeader />
	<domain:showIBPHeader />

	<auth:ajaxLogin />

	<div id="species_main_wrapper">

		<div class="container_12 container">
			<div id="menu" class="grid_12 ui-corner-all"
				style="margin-bottom: 10px;">

				<g:if
					test="${params.controller == 'species' || params.controller == 'search'}">
					<sNav:render group="species_dashboard" subitems="false" />
				</g:if>
				<g:if test="${params.controller == 'observation'}">
					<sNav:render group="observation_dashboard" subitems="false" />
				</g:if>

				<div style="float: right;">
					<g:searchBox />
				</div>

			</div>

		</div>
		<g:layoutBody />

		<domain:showWGPFooter />

		<domain:showIBPFooter />

	</div>

	<g:javascript>

		var domainAppId;
		if (document.domain == "${grailsApplication.config.wgp.domain}"){
			domainAppId = '${grailsApplication.config.speciesPortal.wgp.facebook.appId}'
		} else if(document.domain == "${grailsApplication.config.ibp.domain}") {
			domainAppId = '${grailsApplication.config.speciesPortal.ibp.facebook.appId}'
		}
		// Callback to execute whenever ajax login is successful.
		// Todo some thing meaningful with the response data
		var ajaxLoginSuccessCallbackFunction, ajaxLoginErrorCallbackFunction;
		
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
			
			// make sure facebook is initialized before calling the facebook JS api
			window.fbEnsure = function(callback) {
 					if (window.facebookInitialized) { callback(); return; }
 					
 					if(!window.FB) {
 						//alert("Facebook script all.js could not be loaded for some reason. Either its not available or is blocked.")
 					} else {
 						var options = {
						appId  : domainAppId,
					    channelUrl : "Utils.getDomainServerUrl(request)/channel.html",
					    status : true,
					    cookie : true,
					    xfbml: true,
					    oauth  : true,
					    logging : true
					  };
				  
				  FB.init(options); 
				  
				  window.facebookInitialized = true;
				  callback();
				  }
			};
			
			/**
			 * Just connect (for logged in users) to facebook and reassociate the user
			 * (and possibly get the facebook profile picture if no avatar yet set).
			 * Triggers two events on the '.fbJustConnect' element:
			 * - "connected" will be triggered if the reassociation was successful
			 * - "failed" will be triggered when for whatever reason the coupling was unsuccessful
			 */
			var fbConnect = function() {
									
				var scope = { scope: "" };
				scope.scope = "email,user_about_me,user_location,user_activities,user_hometown,manage_notifications,user_website,publish_stream";
				
				fbEnsure(function() {
					FB.login(function(response) {
						if (response.status == 'connected') {
							window.location = "${createLink(controller:'login', action:'authSuccess')}"+"?uid="+response.authResponse.userID
						} else {
							alert("Failed to connect to Facebook");
						}
					}, scope);
				});
			};
			
			$('.fbJustConnect').click(function() {
				fbConnect();
			});
			 
		}); 
			
</g:javascript>
	<script>
		(function(d, s, id) {
			var js, fjs = d.getElementsByTagName(s)[0];
			if (d.getElementById(id))
				return;
			js = d.createElement(s);
			js.id = id;
			js.src = "//connect.facebook.net/en_US/all.js#xfbml=1&appId="
					+ domainAppId;
			fjs.parentNode.insertBefore(js, fjs);
		}(document, 'script', 'facebook-jssdk'));
	</script>


	<g:javascript src="species/popuplib.js"></g:javascript>

	<script type="text/javascript">
		function closeHandler() {
			console.log("close")
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
			'realm' : 'http://thewesternghats.localhost.in',
			'opEndpoint' : 'https://www.google.com/accounts/o8/ud',
			'returnToUrl' : 'http://thewesternghats.localhost.in/biodiv',
			'onCloseHandler' : closeHandler,
			'shouldEncodeUrls' : true,
			'extensions' : extensions
		});
	</script>


</body>
</html>
