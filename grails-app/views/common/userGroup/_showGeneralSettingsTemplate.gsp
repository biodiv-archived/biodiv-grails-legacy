<form class="form-horizontal" action="${createLink(mapping:'userGroup', action:'settings', params:['webaddress':userGroupInstance.webaddress])}" name='groupSettingForm' method="POST">
	<g:hiddenField name="id" value="${userGroupInstance.id}" />
	
	<div class="prop">
		<span class="name">Home Page</span>
		<g:hiddenField name="homePage"/>
		<div class="value">
			<div class="btn-group pull-right">
				<button id="homePageSelector" class="btn dropdown-toggle" data-toggle="dropdown" href="#" rel="tooltip"
					data-original-title="Home page">${userGroupInstance.homePage ?: 'Select home page '}<span class="caret"></span>
				</button>
				<ul class="dropdown-menu" style="width: auto;">
					<li class="group_option"><a class=" home_page_label"
						value="${createLink(mapping:'userGroup', action:'about', params:['webaddress':userGroupInstance.webaddress])}"> ${createLink(mapping:'userGroup', action:'about', params:['webaddress':userGroupInstance.webaddress])} </a></li>
					<li class="group_option"><a class=" home_page_label"
						value="${createLink(mapping:'userGroup', action:'activity', params:['webaddress':userGroupInstance.webaddress])}"> ${createLink(mapping:'userGroup', action:'activity', params:['webaddress':userGroupInstance.webaddress])} </a></li>
					<li class="divider"></li>
					<g:each var="newsletterInstance" in="${userGroupInstance.getPages()}">
						<li class="group_option"><a class=" home_page_label"
							value="${createLink(controller:'newsletter', action:'show', id:newsletterInstance.id)}"> ${newsletterInstance.title + " "}</a></li>
					</g:each>
				</ul>
			</div>
		</div>
	</div>
	
	<div class="prop">
		<span class="name">Theme</span>
		<g:hiddenField name="theme"/>
		<div class="value">
			<div class="btn-group pull-right">
				<button id="themeSelector" class="btn dropdown-toggle" data-toggle="dropdown" href="#" rel="tooltip"
					data-original-title="Theme">${userGroupInstance.theme ?: 'Select theme '}<span class="caret"></span>
				</button>
				<ul class="dropdown-menu" style="width: auto;">
					<g:each var="theme" in="${userGroupInstance.getThemes()}">
						<li class="group_option"><a class=" theme_label"
							value="${theme}"> ${theme + " "}</a></li>
					</g:each>
				</ul>
			</div>
		</div>
	</div>
	
	
	<input type="submit" value="Update" class="btn btn-primary btn-small pull-right" style="clear:both; border-radius:4px" />
</form>
