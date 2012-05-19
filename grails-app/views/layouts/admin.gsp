<%@ page import="org.codehaus.groovy.grails.plugins.PluginManagerHolder"%>
<%@ page
	import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>
<%@ page import="grails.plugins.springsecurity.SecurityConfigType"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>

<head>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title><g:layoutTitle
		default='Species Portal Management Console' /></title>

<link rel="shortcut icon"
	href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

<s2ui:resources module='spring-security-ui' />
<%--

The 'resources' tag in SecurityUiTagLib renders these tags if you're not using the resources plugin:

   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'reset.css',plugin:'spring-security-ui')}"/>
   <g:javascript library='jquery' plugin='jquery' />
   <jqui:resources />
   <link rel="stylesheet" media="screen" href="${resource(dir:'css/smoothness',file:'jquery-ui-1.8.2.custom.css',plugin:'spring-security-ui')}"/>
   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'jquery.jgrowl.css',plugin:'spring-security-ui')}"/>
   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'jquery.safari-checkbox.css',plugin:'spring-security-ui')}"/>
   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'date_input.css',plugin:'spring-security-ui')}"/>
   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'jquery.jdMenu.css',plugin:'spring-security-ui')}"/>
   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'jquery.jdMenu.slate.css',plugin:'spring-security-ui')}"/>
   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'table.css',plugin:'spring-security-ui')}"/>
   <link rel="stylesheet" media="screen" href="${resource(dir:'css',file:'spring-security-ui.css',plugin:'spring-security-ui')}"/>

or these if you are:

   <r:require module="spring-security-ui"/>
   <r:layoutResources/>

If you need to customize the resources, replace the <s2ui:resources> tag with
the explicit tags above and edit those, not the taglib code.
--%>

<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'reset.css')}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'text.css')}" />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'960.css')}" />

<link rel="stylesheet"
	href="${resource(dir:'css',file:'main.css')}" />
<link rel="stylesheet" type="text/css"
	href="${resource(dir:'css',file:'navigation.css')}" />

<%-- tab icons --%>
<style>
.icon_role {
	background-image: url('${fam.icon(name: ' lock ')}');
}

.icon_users {
	background-image: url('${fam.icon(name: ' group ')}');
}

.icon_user {
	background-image: url('${fam.icon(name: ' user ')}');
}

.icon_error {
	background-image: url('${fam.icon(name: ' exclamation ')}');
}

.icon_info {
	background-image: url('${fam.icon(name: ' information ')}');
}

.icon,.ui-tabs .ui-tabs-nav li a.icon {
	background-repeat: no-repeat;
	padding-left: 24px;
	background-position: 4px center;
}
</style>
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
	top: 0;
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

#top_nav_bar li#maps_nav_link:hover {
	background-color: #fafafa;
	border-bottom: 3px solid #f0575a;
}

#top_nav_bar li#checklists_nav_link:hover {
	background-color: #fafafa;
	border-bottom: 3px solid #7764a2;
}

#top_nav_bar li#collaborate_nav_link:hover {
	background-color: #fafafa;
	border-bottom: 3px solid #145b9b;
}

#top_nav_bar li#species_nav_link:hover {
	background-color: #fafafa;
	border-bottom: 3px solid #00a4be;
}

#top_nav_bar li#themes_nav_link:hover {
	background-color: #fafafa;
	border-bottom: 3px solid #45a989;
}

#top_nav_bar li#about_nav_link:hover {
	background-color: #fafafa;
	border-bottom: 3px solid #003846;
}
</style>


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
								ul.append( "<li class='ui-autocomplete-category'>" +item.category + "</li>" );
								currentCategory = item.category;
							}
							self._renderItem( ul, item );
						});
					}
				});
});
</g:javascript>


<g:layoutHead />

</head>

<body>
	<div id="header">
		<!-- Logo -->
		<div id="logo">
			<a href="/"> <img id="wg_logo" alt="western ghats"
				src="/sites/all/themes/wg/images/map-logo.gif"> </a>
		</div>
		<!-- Logo ends -->

		<div id="top_nav_bar">
			<ul>
				<li onclick="location.href='/map'" title="Maps" id="maps_nav_link">Maps</li>
				<li onclick="location.href='/browsechecklists'" title="Checklists"
					id="checklists_nav_link">Checklists</li>
				<li onclick="location.href='/collaborate-wg'" title="Collaborate"
					id="collaborate_nav_link">Collaborate</li>
				<li onclick="location.href='/biodiv/species/list'" title="Species"
					id="species_nav_link">Species</li>
				<li onclick="location.href='/themepages/list'" title="Themes"
					id="themes_nav_link">Themes</li>
				<li onclick="location.href='/about/western-ghats'" title="About"
					id="about_nav_link">About</li>
			</ul>
		</div>

	</div>


	<div>
		<span id='loginLink'
			style='position: relative; margin-right: 30px; float: right'>
			<sec:ifLoggedIn>
         	Logged in as <sec:username /> (<g:link controller='logout'>Logout</g:link>)
      		</sec:ifLoggedIn> <sec:ifNotLoggedIn>
				<!--a href='#' onclick='show_login_dialog();  return false'>Login</a-->
				<g:link controller='login'>Login</g:link>
			</sec:ifNotLoggedIn> </span>
		<g:render template='/common/ajaxLogin' />
		<br />
	</div>



	<div id="species_main_wrapper">

		<div class="container_12">
			<div id="menu" class="grid_12 ui-corner-all">
				<div class="demo" style="float: right; margin-right: .3em;"
					title="These are demo pages">These are demo pages</div>
				<br />
				<sNav:render group="dashboard" subitems="true" />
				<div style="float: right;">
					<search:searchBox />
				</div>

			</div>
			<br />
			<div class="container_16">


				<div>

					<ul class="jd_menu jd_menu_slate">
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.users" /> </a>
							<ul>
								<li><g:link controller="SUser" action='search'>
										<g:message code="spring.security.ui.search" />
									</g:link></li>
								<li><g:link controller="SUser" action='create'>
										<g:message code="spring.security.ui.create" />
									</g:link></li>
							</ul>
						</li>
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.roles" /> </a>
							<ul>
								<li><g:link controller="role" action='search'>
										<g:message code="spring.security.ui.search" />
									</g:link></li>
								<li><g:link controller="role" action='create'>
										<g:message code="spring.security.ui.create" />
									</g:link></li>
							</ul>
						</li>
						<g:if
							test='${SpringSecurityUtils.securityConfig.securityConfigType == SecurityConfigType.Requestmap}'>
							<li><a class="accessible"><g:message
										code="spring.security.ui.menu.requestmaps" /> </a>
								<ul>
									<li><g:link controller="requestmap" action='search'>
											<g:message code="spring.security.ui.search" />
										</g:link></li>
									<li><g:link controller="requestmap" action='create'>
											<g:message code="spring.security.ui.create" />
										</g:link></li>
								</ul>
							</li>
						</g:if>
						<g:if
							test='${SpringSecurityUtils.securityConfig.rememberMe.persistent}'>
							<li><a class="accessible"><g:message
										code="spring.security.ui.menu.persistentLogins" /> </a>
								<ul>
									<li><g:link controller="persistentLogin" action='search'>
											<g:message code="spring.security.ui.search" />
										</g:link></li>
								</ul>
							</li>
						</g:if>
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.registrationCode" /> </a>
							<ul>
								<li><g:link controller="registrationCode" action='search'>
										<g:message code="spring.security.ui.search" />
									</g:link></li>
							</ul>
						</li>
						<g:if
							test="${PluginManagerHolder.pluginManager.hasGrailsPlugin('springSecurityAcl')}">
							<li><a class="accessible"><g:message
										code="spring.security.ui.menu.acl" /> </a>
								<ul>
									<li><g:message code="spring.security.ui.menu.aclClass" />
										&raquo;
										<ul>
											<li><g:link controller="aclClass" action='search'>
													<g:message code="spring.security.ui.search" />
												</g:link></li>
											<li><g:link controller="aclClass" action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link></li>
										</ul>
									</li>
									<li><g:message code="spring.security.ui.menu.aclSid" />
										&raquo;
										<ul>
											<li><g:link controller="aclSid" action='search'>
													<g:message code="spring.security.ui.search" />
												</g:link></li>
											<li><g:link controller="aclSid" action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link></li>
										</ul>
									</li>
									<li><g:message
											code="spring.security.ui.menu.aclObjectIdentity" /> &raquo;
										<ul>
											<li><g:link controller="aclObjectIdentity"
													action='search'>
													<g:message code="spring.security.ui.search" />
												</g:link></li>
											<li><g:link controller="aclObjectIdentity"
													action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link></li>
										</ul>
									</li>
									<li><g:message code="spring.security.ui.menu.aclEntry" />
										&raquo;
										<ul>
											<li><g:link controller="aclEntry" action='search'>
													<g:message code="spring.security.ui.search" />
												</g:link></li>
											<li><g:link controller="aclEntry" action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link></li>
										</ul>
									</li>
								</ul>
							</li>
						</g:if>
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.appinfo" /> </a>
							<ul>
								<li><g:link action='config' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.config' />
									</g:link></li>
								<li><g:link action='mappings' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.mappings' />
									</g:link></li>
								<li><g:link action='currentAuth' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.auth' />
									</g:link></li>
								<li><g:link action='usercache' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.usercache' />
									</g:link></li>
								<li><g:link action='filterChain' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.filters' />
									</g:link></li>
								<li><g:link action='logoutHandler'
										controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.logout' />
									</g:link></li>
								<li><g:link action='voters' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.voters' />
									</g:link></li>
								<li><g:link action='providers' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.providers' />
									</g:link></li>
							</ul>
						</li>
					</ul>



					<div id="s2ui_main">
						<div id="s2ui_content">
							<s2ui:layoutResources module='spring-security-ui' />
							<g:layoutBody />
							<%--
<g:javascript src='jquery/jquery.jgrowl.js' plugin='spring-security-ui'/>
<g:javascript src='jquery/jquery.checkbox.js' plugin='spring-security-ui'/>
<g:javascript src='jquery/jquery.date_input.js' plugin='spring-security-ui'/>
<g:javascript src='jquery/jquery.positionBy.js' plugin='spring-security-ui'/>
<g:javascript src='jquery/jquery.bgiframe.js' plugin='spring-security-ui'/>
<g:javascript src='jquery/jquery.jdMenu.js' plugin='spring-security-ui'/>
<g:javascript src='jquery/jquery.dataTables.min.js' plugin='spring-security-ui'/>
<g:javascript src='spring-security-ui.js' plugin='spring-security-ui'/>
--%>
						</div>
					</div>

				</div>
			</div>
			<g:layoutBody />
		</div>
</body>
</html>
