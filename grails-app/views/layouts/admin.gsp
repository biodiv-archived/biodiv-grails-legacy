<%@ page import="org.codehaus.groovy.grails.plugins.PluginManagerHolder"%>
<%@ page
	import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>
<%@ page import="grails.plugins.springsecurity.SecurityConfigType"%>


<html>

<head>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title><g:layoutTitle
		default='Species Portal Management Console' />
</title>

<link rel="shortcut icon"
	href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
<r:require modules="admin" />

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

<g:javascript>
    window.appContext = '${request.contextPath}';
    window.appIBPDomain = '${grailsApplication.config.ibp.domain}'
    window.appWGPDomain = '${grailsApplication.config.wgp.domain}'
</g:javascript>
<g:layoutHead />
<s2ui:resources module='spring-security-ui' />
<r:layoutResources />

</head>

<body>


	<div id="species_main_wrapper">
		<div id='s2ui_header_body'>

			<div id='s2ui_header_title'>Biodiv Management Console
			</div>

			<span id='s2ui_login_link_container'> <nobr>
					<div id='loginLinkContainer'>
						<sec:ifLoggedIn>
				Logged in as <sec:username /> (<g:link controller='logout'>Logout</g:link>)
				</sec:ifLoggedIn>
						<sec:ifNotLoggedIn>
							<a href='#' id='loginLink'>Login</a>
						</sec:ifNotLoggedIn>

						<sec:ifSwitched>
							<a href='${request.contextPath}/j_spring_security_exit_user'>
								Resume as <sec:switchedUserOriginalUsername /> </a>
						</sec:ifSwitched>
					</div>
				</nobr> </span>
		</div>

	</div>
	<div class="container_12 container">
		<div id="menu" class="grid_12 ui-corner-all"
			style="margin-bottom: 10px;">

			<sNav:render group="users_dashboard" subitems="false" />

		</div>

	</div>

	<div class="container outer-wrapper">

		<div class="observation_create row">
			<div class="span12">

				<div>

					<ul class="jd_menu jd_menu_slate">
						<li><a class="accessible">Data </a>
							<ul>
								<li><a href="${createLink(action:'loadData')}">Load
										sample data</a></li>
								<li><a href="${createLink(action:'loadNames')}">Load
										sample names</a></li>
								<br />
								<li><a href="${createLink(action:'updateGroups')}">Update
										groups for taxon concepts</a></li>
								<li><a href="${createLink(action:'updateExternalLinks')}">Update
										external links for taxon concepts</a></li>
								<li><a href="${createLink(action:'reloadNames')}">Sync
										names and recommendations</a></li>
								<br />
								<li><a href="${createLink(action:'reloadNamesIndex')}">Recreate
										names index</a>
								<li><a
									href="${createLink(action:'reloadSpeciesSearchIndex')}">Recreate
										species search index</a>
								<li><a
									href="${createLink(action:'reloadObservationsSearchIndex')}">Recreate
										observations search index</a> <br />
								<li><a href="${createLink(action:'recomputeInfoRichness')}">Recompute
										information richness</a></li>
							</ul></li>
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.users" /> </a>
							<ul>
								<li><g:link controller='SUser' action='search'>
										<g:message code="spring.security.ui.search" />
									</g:link>
								</li>
								<li><g:link controller='SUser' action='create'>
										<g:message code="spring.security.ui.create" />
									</g:link>
								</li>
							</ul></li>
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.roles" /> </a>
							<ul>
								<li><g:link controller="role" action='search'>
										<g:message code="spring.security.ui.search" />
									</g:link>
								</li>
								<li><g:link controller="role" action='create'>
										<g:message code="spring.security.ui.create" />
									</g:link>
								</li>
							</ul></li>
						<g:if
							test='${SpringSecurityUtils.securityConfig.securityConfigType == SecurityConfigType.Requestmap}'>
							<li><a class="accessible"><g:message
										code="spring.security.ui.menu.requestmaps" /> </a>
								<ul>
									<li><g:link controller="requestmap" action='search'>
											<g:message code="spring.security.ui.search" />
										</g:link>
									</li>
									<li><g:link controller="requestmap" action='create'>
											<g:message code="spring.security.ui.create" />
										</g:link>
									</li>
								</ul></li>
						</g:if>
						<g:if
							test='${SpringSecurityUtils.securityConfig.rememberMe.persistent}'>
							<li><a class="accessible"><g:message
										code="spring.security.ui.menu.persistentLogins" /> </a>
								<ul>
									<li><g:link controller="persistentLogin" action='search'>
											<g:message code="spring.security.ui.search" />
										</g:link>
									</li>
								</ul></li>
						</g:if>
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.registrationCode" /> </a>
							<ul>
								<li><g:link controller="registrationCode" action='search'>
										<g:message code="spring.security.ui.search" />
									</g:link>
								</li>
							</ul></li>
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
												</g:link>
											</li>
											<li><g:link controller="aclClass" action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link>
											</li>
										</ul></li>
									<li><g:message code="spring.security.ui.menu.aclSid" />
										&raquo;
										<ul>
											<li><g:link controller="aclSid" action='search'>
													<g:message code="spring.security.ui.search" />
												</g:link>
											</li>
											<li><g:link controller="aclSid" action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link>
											</li>
										</ul></li>
									<li><g:message
											code="spring.security.ui.menu.aclObjectIdentity" /> &raquo;
										<ul>
											<li><g:link controller="aclObjectIdentity"
													action='search'>
													<g:message code="spring.security.ui.search" />
												</g:link>
											</li>
											<li><g:link controller="aclObjectIdentity"
													action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link>
											</li>
										</ul></li>
									<li><g:message code="spring.security.ui.menu.aclEntry" />
										&raquo;
										<ul>
											<li><g:link controller="aclEntry" action='search'>
													<g:message code="spring.security.ui.search" />
												</g:link>
											</li>
											<li><g:link controller="aclEntry" action='create'>
													<g:message code="spring.security.ui.create" />
												</g:link>
											</li>
										</ul></li>
								</ul></li>
						</g:if>
						<li><a class="accessible"><g:message
									code="spring.security.ui.menu.appinfo" /> </a>
							<ul>
								<li><g:link action='config' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.config' />
									</g:link>
								</li>
								<li><g:link action='mappings' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.mappings' />
									</g:link>
								</li>
								<li><g:link action='currentAuth' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.auth' />
									</g:link>
								</li>
								<li><g:link action='usercache' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.usercache' />
									</g:link>
								</li>
								<li><g:link action='filterChain' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.filters' />
									</g:link>
								</li>
								<li><g:link action='logoutHandler'
										controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.logout' />
									</g:link>
								</li>
								<li><g:link action='voters' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.voters' />
									</g:link>
								</li>
								<li><g:link action='providers' controller='securityInfo'>
										<g:message code='spring.security.ui.menu.appinfo.providers' />
									</g:link>
								</li>
							</ul></li>
					</ul>

					<div id="s2ui_main">
						<div id="s2ui_content">
							<s2ui:layoutResources module='spring-security-ui' />
							<g:layoutBody />
						</div>
					</div>

				</div>


			</div>
		</div>
	</div>

	<domain:showWGPFooter />

	<domain:showIBPFooter />
</body>
</html>
