<g:javascript>
    window.appContext = '${request.contextPath}';
    window.appIBPDomain = '${grailsApplication.config.ibp.domain}'
    window.appWGPDomain = '${grailsApplication.config.wgp.domain}'
</g:javascript>

<div class="container group-theme" style="width:100%;">
	<div>
		<g:if test="${userGroupInstance  && userGroupInstance.id }">
			<uGroup:showHeader model="[ 'userGroupInstance':userGroupInstance]" />
		</g:if>
		<g:else>
			<a href="${createLink(url:grailsApplication.config.grails.serverURL+"/..") }" class="span3 logo" style="margin-left: 0px;"> <img
				class="logo" src="/sites/all/themes/ibp/images/map-logo.gif"
				title="India Biodiversity Portal" alt="India Biodiversity Portal">
			</a>
			<h1>India Biodiversity Portal</h1>
		</g:else>
	</div>
</div>
<div class="navbar navbar-static-top btn"
	style="margin-bottom: 0px; position: relative; width: 100%;padding: 0px; ">
	<div class="navbar-inner"
		style="box-shadow: none; background-color: transparent; background-image: none;">
		<div class="container outer-wrapper"
			style="background-color: transparent; padding-bottom: 0px;">

			<g:if test="${userGroupInstance && userGroupInstance.id}">

				<ul class="nav pull-left">

				</ul>
				<ul class="nav pull-left">

					<li
						class="${((params.controller == 'userGroup' && params.action == 'species')||(params.controller == 'species'))?' active':''}"><a
						href="${uGroup.createLink('mapping':'userGroup', 'action':'species', 'userGroup':userGroupInstance)}"
						title="Species">Species</a>
					</li>
					<li class="${((params.controller == 'userGroup' && params.action == 'observation') ||(params.controller == 'observation'))?' active':''}"><a
						href="${uGroup.createLink('mapping':'userGroup', 'action':'observation', 'userGroup':userGroupInstance)}"
						title="Observations">Observations</a>
					</li>
					<li
						class="${(request.getHeader('referer')?.contains('/map') && params.action == 'header')?' active':''}"><a
						href="${userGroupInstance.domainName}/map" title="Maps">Maps</a></li>
					<li
						class="${((params.controller == 'userGroup' && params.action == 'checklist') ||(params.controller == 'checklist'))?' active':''}"><a
						href="${uGroup.createLink('mapping':'userGroup', 'action':'checklist', 'userGroup':userGroupInstance)}"
						title="Checklists">Checklists</a>
					</li>



				</ul>
				<ul class="nav pull-right">
					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->

					<li
						class="${(params.controller == 'userGroup' && params.action == 'activity')?' active':''}"><a
						href="${uGroup.createLink(mapping:"userGroup", 'action':"activity", 'userGroup':userGroupInstance)}"
						title="Activity">Activity</a>
					</li>
					
					<li class="${(params.controller == 'userGroup' && params.action == 'pages')?' active':''}"><a
								href="${uGroup.createLink(mapping:"userGroup", 'action':"pages", 'userGroup':userGroupInstance)}"
								title="Pages">Pages</a>
					</li>

					<li class="${(params.controller == 'userGroup' && params.action == 'about')?' active':''}"><a
						href="${uGroup.createLink(mapping:"userGroup", 'action':"about", 'userGroup':userGroupInstance)}"
						title="About Us">About Us</a>
					</li>
					<li class="dropdown"><a href="#" class="dropdown-toggle"
						data-toggle="dropdown"> More <b class="caret"></b> </a>
						<ul class="dropdown-menu" style="text-align: left;">
							<!-- li
								class="${(params.controller == 'userGroup' && params.action == 'user')?' active':''}"><a
								href="${uGroup.createLink(mapping:"userGroup", 'action':"user", 'userGroup':userGroupInstance)}"
								title="Members">Members</a>
							</li-->
							
							<li
								class="${(request.getHeader('referer')?.contains('/calendar') && params.action == 'header')?' active':''}"><a
								href="/calendar" title="Events">Events</a></li>
							<li
								class="${(request.getHeader('referer')?.contains('/biodiversity_news') && params.action == 'header')?' active':''}"><a
								href="/biodiversity_news" title="News">News</a></li>
							
						</ul></li>
				</ul>

			</g:if>
			<g:else>
				<ul class="nav pull-left">

				</ul>
				<ul class="nav">
					<li class=" ${(params.controller == 'species')?'active':''}"><a
						href="${uGroup.createLink("controller":"species")}" title="Species">
							Species</a></li>
					<li class="${(params.controller == 'observation')?'active':''}"><a
						href="${uGroup.createLink("controller":"observation")}"
						title="Observations">Observations</a></li>
					<li
						class="${(request.getHeader('referer')?.contains('/map') && params.action == 'header')?' active':''}"><a
						href="/map" title="Maps">Maps</a></li>
					<li
						class="${(params.controller == 'checklist')?'active':''}"><a
						href='${uGroup.createLink("controller":"checklist")}' title="Checklists">Checklists</a></li>
				</ul>

				<ul class="nav pull-right">
					<li class=" ${(params.controller == 'activityFeed')?'active':''}"><a
						href="${uGroup.createLink("controller":"activityFeed")}" title="Activity">Activity</a>
					</li>

					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
					<li class="${(params.action == 'pages')?' active':''}"><a
								href="${uGroup.createLink(mapping:"pages", controller:"userGroup", 'action':"pages")}"
								title="Pages">Pages</a>
					</li>
					
					<li
						class="${(request.getHeader('referer')?.contains('/about') && params.action == 'header')?' active':''}"><a
						href="/about" title="About Us">About Us </a></li>
					<li class="dropdown"><a href="#" class="dropdown-toggle"
						data-toggle="dropdown"> More <b class="caret"></b> </a>

						<ul class="dropdown-menu" style="text-align: left; color: #000">
							<!-- li
								class="${((params.controller == 'user' || params.controller == 'SUser') && params.action != 'header')?' active':''}"><a
								href="${uGroup.createLink(controller:'user', action:'list')}"
								title="Members">Members</a></li-->
							<li
								class="${(request.getHeader('referer')?.contains('/calendar') && params.action == 'header')?' active':''}"><a
								href="/calendar" title="Events">Events</a></li>
							<li
								class="${(request.getHeader('referer')?.contains('/biodiversity_news') && params.action == 'header')?' active':''}"><a
								href="/biodiversity_news" title="News">News</a></li>

						</ul>
					</li>
				</ul>

			</g:else>

		</div>
	</div>
</div>


<g:javascript>
$(document).ready(function(){
	//IMP:Header is loaded in drupal pages as well. Any code in this block is not run when loaded by ajax
	//So please don't put any code here. Put it in init_header function in membership.js
	 init_header();
});

</g:javascript>
