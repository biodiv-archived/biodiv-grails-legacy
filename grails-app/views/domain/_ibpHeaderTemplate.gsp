<%@page import="species.utils.Utils"%>
<div id="ibp-header" class="gradient-bg">
	<div class=" gradient-bg navbar navbar-static-top"
		style="margin-bottom: 0px;">
		<div class="navbar-inner">
			<div class="container" style="width:100%">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> </a> <a
					class="brand" href="${createLink('absolute':true) }"> <!-- img class="logo" alt="India Biodiversity Portal"
						src="/sites/all/themes/ibp/images/map-logo.gif"--> India
					Biodiversity Portal</a>
				<div class="nav-collapse">
					<ul class="nav">

					</ul>


					<ul class="nav pull-right">
						<li>
								<search:searchBox />
						</li>
						<g:if test="${params.controller != 'openId' && params.controller != 'login' &&  params.controller != 'register'}">
						<li>
							<uGroup:showSidebar />
						</li>
						</g:if>
						<li>
							<sUser:userLoginBox />
						</li>

					</ul>
				</div>
			</div>

		</div>
	</div>
	<domain:showHeader model="['userGroupInstance':userGroupInstance]"/>
</div>
