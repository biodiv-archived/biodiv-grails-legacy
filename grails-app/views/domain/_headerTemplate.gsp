<%@page import="species.utils.Utils"%>
<g:javascript>
    window.appContext = '';
    window.appIBPDomain = '${grailsApplication.config.ibp.domain}'
    window.appWGPDomain = '${grailsApplication.config.wgp.domain}'
</g:javascript>

<div class="container group-theme navbar" style="width:100%;margin-bottom:0px;">
	<div>
		<g:if test="${userGroupInstance  && userGroupInstance.id }">
			<uGroup:showHeader model="[ 'userGroupInstance':userGroupInstance]" />
		</g:if>
		<g:else>
			<a class="pull-left" href="${createLink(url:grailsApplication.config.grails.serverURL+"/..") }" style="margin-left: 0px;"> <img
                            class="logo" src="${Utils.getIBPServerDomain()+'/'+grailsApplication.config.speciesPortal.app.logo}"
                            title="${grailsApplication.config.speciesPortal.app.siteName}" alt="${grailsApplication.config.speciesPortal.app.siteName}">
			</a>
			<a href="${createLink(url:grailsApplication.config.grails.serverURL+"/..") }" class="brand">
                            <h1>${grailsApplication.config.speciesPortal.app.siteName}</h1>
			</a>
		</g:else>
	</div>
</div>
<div class="navbar navbar-static-top btn"
	style="margin-bottom: 0px; position: relative; width: 100%;padding: 0px; border-left-width:0; border-right-width: 0;">
	<div class="navbar-inner"
		style="box-shadow: none; background-color: transparent; background-image: none;height:40px;">
		<div class="container outer-wrapper"
			style="background-color: transparent; padding-bottom: 0px;text-align:center;">
			<g:if test="${userGroupInstance && userGroupInstance.id}">

				<ul class="nav pull-left">

					<li
						class="${((params.controller == 'userGroup' && params.action == 'species')||(params.controller == 'species'))?' active':''}"><a
						href="${uGroup.createLink('mapping':'userGroup', 'action':'species', 'userGroup':userGroupInstance)}"
						title="Species">Species</a>
					</li>
					<li class="${((params.controller == 'userGroup' && params.action == 'observation') ||(params.controller == 'observation'))?' active':''}"><a
						href="${uGroup.createLink('controller':'observation', 'action':'list', 'userGroup':userGroupInstance)}"
                                                title="Observations">Observations</a> 
                                            <!--a style="position:absolute;top:-18px;right:140px;box-shadow:none;background-color:transparent;" href="${uGroup.createLink('controller':'observation', 'action':'create', 'userGroup':userGroupInstance)}"><span class="badge badge-important" title="Add Observation"><i class="icon-add"></i></span></a--> 
					</li>
					<li
						class="${((params.controller == 'SUser' && params.action == 'header') ||(params.controller == 'map'))?' active':''}"><a
						href="${uGroup.createLink('mapping':'userGroup', 'action':'map', 'userGroup':userGroupInstance)}" title="Maps">Maps</a></li>
					
			 		<li
                    	class="${((params.controller == 'document' && params.action == 'browser') ||(params.controller == 'browser'))?' active':''}"><a
                        href="${uGroup.createLink('controller':'document', 'action':'browser', 'userGroup':userGroupInstance)}"
                        title="Documents">Documents</a>
                    </li>
					
<%--					<li--%>
<%--						class="${((params.controller == 'userGroup' && params.action == 'checklist') ||(params.controller == 'checklist'))?' active':''}"><a--%>
<%--						href="${uGroup.createLink('mapping':'userGroup', 'action':'checklist', 'userGroup':userGroupInstance)}"--%>
<%--						title="Checklists">Checklists</a>--%>
<%--					</li>--%>
<%----%>
<%--					<li--%>
<%--						class="${((params.controller == 'userGroup' && params.action == 'chart') ||(params.controller == 'chart'))?' active':''}"><a--%>
<%--						href="${uGroup.createLink('mapping':'userGroup', 'action':'chart', 'userGroup':userGroupInstance)}"--%>
<%--						title="Dashboard">Dashboard</a>--%>
<%--					</li>--%>



				</ul>
                                <ul class="nav contributeButton"  style="float:none;display:inline-block">
                                    <li>
                                    <a href="#contributeMenu" data-toggle="collapse" style="margin-top:0px;color:white;">
                                        <i class="icon-list"></i>
                                        Contribute
                                        <i class="caret"></i>
                                    </a>
                                    </li>
                                </ul>
				<ul class="nav pull-right">
					<!--li class="menu-449 first"><a href="/" title="">Home</a></li-->

					<li
						class="${(params.controller == 'userGroup' && params.action == 'activity')?' active':''}"><a
						href="${uGroup.createLink(mapping:'userGroup', 'action':'activity', 'userGroup':userGroupInstance)}"
						title="Activity">Activity</a>
					</li>
					
					<li class="${(params.controller == 'userGroup' && params.action == 'pages')?' active':''}"><a
								href="${uGroup.createLink(mapping:'userGroup', 'action':'pages', 'userGroup':userGroupInstance)}"
								title="Pages">Pages</a>
					</li>

					<li class="${(params.controller == 'userGroup' && params.action == 'about')?' active':''}"><a
						href="${uGroup.createLink(mapping:'userGroup', 'action':'about', 'userGroup':userGroupInstance)}"
						title="About Us">About Us</a>
					</li>
					

					<li class="dropdown"><a href="#" class="dropdown-toggle"
						data-toggle="dropdown"> More <b class="caret"></b> </a>
						<ul class="dropdown-menu" style="text-align: left;">
                                                    <li
                                                    class="${((params.controller == 'user' && params.action == 'list') ||(params.controller == 'user'))?' active':''}"><a
                                                        href="${uGroup.createLink('controller':'user', 'action':'list', 'userGroup':userGroupInstance)}"
                                                        title="Members">Members</a>
                                                    </li>

                                                   

                                                    <li
                                                    class="${((params.controller == 'userGroup' && params.action == 'chart') ||(params.controller == 'chart'))?' active':''}"><a
                                                        href="${uGroup.createLink('mapping':'userGroup', 'action':'chart', 'userGroup':userGroupInstance)}"
                                                        title="Dashboard">Dashboard</a>
                                                    </li>
						
						</ul></li>
				</ul>

			</g:if>
			<g:else>
				<ul class="nav pull-left">

				</ul>
				<ul class="nav">
					<li class=" ${(params.controller == 'species')?'active':''}"><a
						href="${uGroup.createLink('controller':'species')}" title="Species">
							Species</a></li>
					<li class="${(params.controller == 'observation')?'active':''}"><a
						href="${uGroup.createLink('controller':'observation')}"
                                                title="Observations">Observations</a>
                                            <!--a  style="position:absolute;top:-18px;right:140px;box-shadow:none;background-color:transparent;" href="${uGroup.createLink('controller':'observation', 'action':'create', 'userGroup':userGroupInstance)}"><span class="badge badge-important" title="Add Observation"><i class="icon-plus"></i></span></a--> 
</li>
					<li
						class="${(request.getHeader('referer')?.contains('/map') && params.action == 'header')?' active':''}"><a
						href="/map" title="Maps">Maps</a></li>
						
					<li
                    	class="${((params.controller == 'document' && params.action == 'browser') ||(params.controller == 'browser'))?' active':''}"><a
                    	href="${uGroup.createLink('controller':'document', 'action':'browser', 'userGroup':userGroupInstance)}"
                        title="Documents">Documents</a></li>	
<%--					<li--%>
<%--						class="${(params.controller == 'checklist')?'active':''}"><a--%>
<%--						href='${uGroup.createLink("controller":"checklist")}' title="Checklists">Checklists</a></li>--%>
<%--					<li--%>
<%--						class="${(params.controller == 'chart')?'active':''}"><a--%>
<%--						href='${uGroup.createLink("controller":"chart")}' title="Dashboard">Dashboard</a></li>--%>
				</ul>
                                <ul class="nav contributeButton" style="float:none;display:inline-block">
                                    <li>
                                    <a href="#contributeMenu" data-toggle="collapse" style="margin-top:0px;color:white">
                                        <i class="icon-list"></i>
                                        Contribute
                                        <i class="caret"></i>
                                    </a>
                                    </li>
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
						href="/theportal" title="About Us">About Us </a></li>
					<li class="dropdown"><a href="#" class="dropdown-toggle"
						data-toggle="dropdown"> More <b class="caret"></b> </a>

						<ul class="dropdown-menu" style="text-align: left; color: #000">
							<li
								class="${((params.controller == 'user' || params.controller == 'SUser') && params.action != 'header')?' active':''}"><a
								href="${uGroup.createLink(controller:'user', action:'list')}"
								title="Members">Members</a></li>
							<li
								class="${(request.getHeader('referer')?.contains('/calendar') && params.action == 'header')?' active':''}"><a
								href="/calendar" title="Events">Events</a></li>
							<li
								class="${(request.getHeader('referer')?.contains('/biodiversity_news') && params.action == 'header')?' active':''}"><a
								href="/biodiversity_news" title="News">News</a></li>

                            

							
							<li
								class="${(params.controller == 'chart')?' active':''}"><a
								href="${uGroup.createLink(controller:'chart')}"
								title="Dashboard">Dashboard</a> </li>
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
