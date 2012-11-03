<%@page import="species.utils.Utils"%>
<div id="ibp-header" class="gradient-bg">
		<div class="navbar navbar-static-top"
			style="margin-bottom: 0px;">
			<div class="navbar-inner"
				style="box-shadow: none; background-color: #dddbbb; background-image: none;">
				<div class="container" style="width: 100%">
					<a class="btn btn-navbar" data-toggle="collapse"
						data-target=".nav-collapse"> <span class="icon-bar"></span> </a>
					<a class="brand"
									href="${createLink(url:grailsApplication.config.grails.serverURL+"/..") }">
										India Biodiversity Portal</a></li>
					<div class="nav-collapse">
						<ul class="nav">

						</ul>


						<ul class="nav pull-right">
<%--							<li><search:searchBox /></li>--%>
							<g:if test="${userGroupInstance  && userGroupInstance.id}">
								<li>
							</g:if>
							<g:if
								test="${params.controller != 'openId' && params.controller != 'login' &&  params.controller != 'register'}">
								<li><uGroup:showSidebar /></li>
							</g:if>
							<li><sUser:userLoginBox
									model="['userGroup':userGroupInstance]" /></li>

						</ul>
					</div>
				</div>

			</div>
		</div>	
		<domain:showHeader model="['userGroupInstance':userGroupInstance]" />
	</div>