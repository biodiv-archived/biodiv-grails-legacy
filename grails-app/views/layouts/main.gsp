<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://ogp.me/ns/fb#">
<head>
<title>Species Portal</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<!-- link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'plugins',file:'jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/jquery-ui-1.8.15.custom.css', absolute:true)}" /-->
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'jquery-ui.css', absolute:true)}" />

<!-- r:require module="jquery-ui" /-->

<!-- r:layoutResources /-->
<!-- sNav:resources override="true" /-->

<script src="${resource(dir:'plugins',file:'jquery-1.7/js/jquery/jquery-1.7.min.js', absolute:true)}" type="text/javascript" ></script>
<script src="${resource(dir:'plugins',file:'jquery-ui-1.8.15/jquery-ui/js/jquery-ui-1.8.15.custom.min.js', absolute:true)}" type="text/javascript" ></script>

<link rel="stylesheet" type="text/css" media="screen"
	href="${resource(dir:'js/jquery/jquery.jqGrid-4.1.2/css',file:'ui.jqgrid.css', absolute:true)}" />

<!-- link rel="stylesheet" type="text/css"
	href="${resource(dir:'css',file:'auth.css', absolute:true)}" /-->
	<link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'spring-security-ui.css',plugin:'spring-security-ui')}"/>
	<link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'jquery.jgrowl.css',plugin:'spring-security-ui')}"/>
	<link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'jquery.safari-checkbox.css',plugin:'spring-security-ui')}"/>
	<link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'auth.css',plugin:'spring-security-ui')}"/>

	
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'reset.css', absolute:true)}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'text.css', absolute:true)}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'960.css', absolute:true)}" />

<link rel="stylesheet"
	href="${resource(dir:'css',file:'main.css', absolute:true)}" />
<link rel="stylesheet" type="text/css"
	href="${resource(dir:'css',file:'navigation.css', absolute:true)}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'jquery.rating.css', absolute:true)}" />

<!-- script type="text/javascript"
	src="${resource(dir:'plugins',file:'jquery-ui-1.8.15/jquery-ui/js/jquery-ui-1.8.15.custom.min.js', absolute:true)}"></script-->
<g:javascript src="jquery/jquery.form.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="jquery/jquery.rating.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="readmore/readmore.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
	
<g:javascript src='jquery/jquery.jgrowl.js' plugin='spring-security-ui'/>
<g:javascript src='jquery/jquery.checkbox.js' plugin='spring-security-ui'/>
<g:javascript src='spring-security-ui.js' plugin='spring-security-ui'/>

<g:javascript>
jQuery(document).ready(function($) {
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
								ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
								currentCategory = item.category;
							}
							self._renderItem( ul, item );
						});
					}
				});
});
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
    font-family: Verdana,Helvetica,Sans-Serif;
    color: #5E5E5E;
    box-shadow: 0 6px 6px -6px #5E5E5E;       
    border-bottom:1px solid #E5E5E5;
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
    top: 0;
    z-index: 501;
}
#top_nav_bar ul {
    list-style: none outside none;
    margin-top:14px;
    margin-bottom:14px;
    font-size: 1.1em;
    padding-left: 40px;
}
#top_nav_bar li {
    cursor: pointer;
    display: inline;
    padding: 10px 10px 3px;
}
#top_nav_bar li#maps_nav_link:hover{
background-color: #fafafa;
border-bottom:3px solid #f0575a;
}

#top_nav_bar li#checklists_nav_link:hover{
background-color: #fafafa;
border-bottom:3px solid #7764a2;
}

#top_nav_bar li#collaborate_nav_link:hover{
background-color: #fafafa;
border-bottom:3px solid #145b9b;
}

#top_nav_bar li#species_nav_link:hover{
background-color: #fafafa;
border-bottom:3px solid #00a4be;
}

#top_nav_bar li#themes_nav_link:hover{
background-color: #fafafa;
border-bottom:3px solid #45a989;
}

#top_nav_bar li#about_nav_link:hover{
background-color: #fafafa;
border-bottom:3px solid #003846;
}

</style>
</head>
<body>

	<!-- div id="spinner" class="spinner" style="display:none;">
		<img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
			alt="${message(code:'spinner.alt',default:'Loading...')}" />
	</div-->
	
	<div id="loading" class="loading" style="display:none;">
		<span>Loading ...</span>
	</div>
        <div id="header">
            <!-- Logo -->
              <div id="logo">
                <a href="/">
                  <img id="wg_logo" alt="western ghats" src="/sites/all/themes/wg/images/map-logo.gif">
                </a>
              </div>
            <!-- Logo ends -->

            <div id="top_nav_bar">
            <ul>
            <li onclick="location.href='/map'" title="Maps" id="maps_nav_link">Maps</li>
            <li onclick="location.href='/browsechecklists'" title="Checklists" id="checklists_nav_link">Checklists</li>
            <li onclick="location.href='/collaborate-wg'" title="Collaborate" id="collaborate_nav_link">Collaborate</li>
            <li onclick="location.href='/biodiv/species/list'" title="Species" id="species_nav_link">Species</li>
            <li onclick="location.href='/themepages/list'" title="Themes" id="themes_nav_link">Themes</li>
            <li onclick="location.href='/about/western-ghats'" title="About" id="about_nav_link">About</li>
            </ul>
            </div>

        </div>


	<div>
		<span id='loginLink'
			style='position: relative; margin-right: 30px; float: right'>
			<sec:ifLoggedIn>
         		<sUser:renderProfileLink/> (<g:link controller='logout'>Logout</g:link>)
      		</sec:ifLoggedIn> <sec:ifNotLoggedIn>
				<g:link controller='login'>Login</g:link>
			</sec:ifNotLoggedIn> </span>
		<g:render template='/common/ajaxLogin' />
		<br />
	</div>



	<div id="species_main_wrapper">
	
		<div class="container_12">
			<div id="menu" class="grid_12 ui-corner-all">
				<div class="demo" style="float: right; margin-right: .3em;"
					title="These are demo pages">These are demo pages</div><br/>
				
				<sNav:render group="dashboard" subitems="true" />
				
				<div style="float: right;">
					<g:searchBox />
				</div>
				
			</div>
			<br />
		</div>
		<g:layoutBody />
	</div>

	<g:javascript>
		
		$(document).ready(function(){
	
			$('.rating').each(function(){
				$('.star', $(this)).rating({
							callback: function(value, link){
								//alert(value);
								//$(this.form).ajaxSubmit();
							}
						});
			});
		
			var offset = $('#loginLink').offset();
			if(offset) {
				$('#ajaxLogin').offset({left:offset.left-$('#ajaxLogin').width()+$('#loginLink').width(), top:offset.top});
			}
	   		var options = { 
	   		 	type:'POST', 
		        dataType: 'json',
		        beforeSubmit:  function (formData, jqForm, options) {
		        	return true; 
		        },  
		        success:  function (json, statusText, xhr, $form)  {
			       	 if (json.success) {
			            $('#ajaxLogin').hide();
			            $('#loginLink').html('Logged in as ' + json.username + ' (<%=link(controller: 'logout') { 'Logout' }%>)');
			         }
			         else if (json.error) {
			            $('#loginMessage').html("<span class='errorMessage'>" + json.error + '</error>'); } else { $('#loginMessage').html(responseText);
					} 
				}
			};
			
			// bind form using 'ajaxForm' var form =
			$('#ajaxLoginForm').ajaxForm(options);
			
			$('#spinner')
				.hide()  // hide it initially
    			.ajaxStart(function() { 
    				$("html").addClass('busy');
    				$(this).offset({left:($('body').width()/2), top:($('body').height()/2)});
        			$(this).show();
    			})
    			.ajaxStop(function() {
    				$("html").removeClass('busy');
        			$(this).hide();
    			});
    		
				$(".ui-icon-control").click(function() {
					var div = $(this).siblings("div.toolbarIconContent");
					if (div.is(":visible")) {
						div.hide(400);
					} else {
						div.slideDown("slow");	
						// div.css("float","right");
						if(div.offset().left < 0) {
							div.offset({left:div.parent().offset().left});					
						}
					}
				});
			
				$(".ui-icon-edit").click(function() {
					var ele =$(this).siblings("div.toolbarIconContent").find("textArea.fieldEditor");
					if(ele) { 
						ele.ckeditor(function(){}, {customConfig:"${resource(dir:'js',file:'ckEditorConfig.js', absolute:true)}"});
						CKEDITOR.replace( ele.attr('id') );
					}
				});
			
				$("a.ui-icon-close").click(function() {
					$(this).parent().hide("slow");
				})
				
		}); 
			<%if(!grailsApplication.config.checkin.drupal) {%>
			function show_login_dialog() {
				$('#ajaxLogin').show(); 
			} 
			<%} %>
			
			function cancelLogin() {
				$('#ajaxLogin').hide(); 
			}

			function authAjax() { 
				$('#loginMessage').val('Sending request ...');
				$('#loginMessage').show(); 
				$('#ajaxLoginForm').submit(); 
			}
			
			window.fbAsyncInit = function() {
		  FB.init({
		    appId  : '${SpringSecurityUtils.securityConfig.facebook.appId}',
		    channelUrl : "${grailsApplication.config.grails.serverURL}/channel.html",
		    status : true,
		    cookie : true,
		    xfbml  : true,
		    oauth  : true,
		    logging : true
		  });
		  
		FB.Event.subscribe('edge.create',
			function(response) {
				alert('You liked the URL: ' + response);
		});
		
		FB.Event.subscribe('auth.login',
				function(response) {
					console.log(response);
					if(response.status == 'connected') {
						window.location = "${createLink(controller:'login', action:'authSuccess')}"
					} else {
						alert("Error in authenticating via Facebook");
					}
				}
			);
		
		};
		
		
	 
		(function(d){var js, id = 'facebook-jssdk'; if (d.getElementById(id)) {return;}js = d.createElement('script'); js.id = id; js.async = true;js.src = "//connect.facebook.net/en_US/all.js";d.getElementsByTagName('head')[0].appendChild(js);}(document));
		
			if (typeof(console) == "undefined") { console = {}; } 
			if (typeof(console.log) == "undefined") { console.log = function() { return 0; } }
	</g:javascript>
	
	
	
</body>
</html>
