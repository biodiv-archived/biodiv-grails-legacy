<form class="form-horizontal"
	action="${createLink(mapping:'userGroup', action:'settings', params:['webaddress':userGroupInstance.webaddress])}"
	name='groupSettingForm' method="POST">
	<g:hiddenField name="id" value="${userGroupInstance.id}" />

	<div class="prop">
		<div class="row control-group left-indent">

			<label class="control-label">Home Page </label>
			<g:hiddenField name="homePage" />
			<div class="controls">
				<div class="btn-group">
					<button id="homePageSelector" class="btn dropdown-toggle"
						data-toggle="dropdown" href="#" rel="tooltip"
						data-original-title="Home page">
						${userGroupInstance.homePage ?: 'Select home page '}<span
							class="caret"></span>
					</button>
					<ul class="dropdown-menu" style="width: auto;">
						<li class="group_option"><a class=" home_page_label"
							value="${createLink(mapping:'userGroup', action:'about', params:['webaddress':userGroupInstance.webaddress])}">
								${createLink(mapping:'userGroup', action:'about', params:['webaddress':userGroupInstance.webaddress])}
						</a>
						</li>
						<li class="group_option"><a class=" home_page_label"
							value="${createLink(mapping:'userGroup', action:'activity', params:['webaddress':userGroupInstance.webaddress])}">
								${createLink(mapping:'userGroup', action:'activity', params:['webaddress':userGroupInstance.webaddress])}
						</a>
						</li>
						<li class="divider"></li>
						<g:each var="newsletterInstance"
							in="${userGroupInstance.getPages()}">
							<li class="group_option"><a class=" home_page_label"
								value="${createLink(controller:'newsletter', action:'show', id:newsletterInstance.id)}">
									${newsletterInstance.title + " "}
							</a>
							</li>
						</g:each>
					</ul>
				</div>
			</div>
		</div>
	</div>

	<div class="prop">
		<div class="row control-group left-indent">

			<label class="control-label">Theme</label>
			<g:hiddenField name="theme" />
			<div class="controls">
				<div class="btn-group">
					<button id="themeSelector" class="btn dropdown-toggle"
						data-toggle="dropdown" href="#" rel="tooltip"
						data-original-title="Theme">
						${userGroupInstance.theme ?: 'Select theme '}<span class="caret"></span>
					</button>
					<ul class="dropdown-menu" style="width: auto;">
						<g:each var="theme" in="${userGroupInstance.getThemes()}">
							<li class="group_option"><a class=" theme_label"
								value="${theme}"> ${theme + " "}
							</a>
							</li>
						</g:each>
					</ul>
				</div>
			</div>
		</div>
	</div>

	<div class="" style="margin-top: 20px; margin-bottom: 40px;">
		<input type="submit" value="Update"
			class="btn btn-primary" 
			style="clear: both; float:right; border-radius: 5px" />
	</div>
</form>
