	<div class="prop">
		<div class="row control-group left-indent">

			<label class="control-label">Home Page </label>
			<g:hiddenField name="homePage" />
			<div class="controls">
				<div class="btn-group" style="margin-top: 10px;">
					<button id="homePageSelector" class="btn dropdown-toggle"
						data-toggle="dropdown" href="#" rel="tooltip"
						data-original-title="Home page">
                                                ${userGroupInstance?.fetchHomePageTitle() ?: 'Select home page '}<span
							class="caret"></span>
					</button>
					<ul class="dropdown-menu" style="width: auto;">
						<li class="group_option"><a class=" home_page_label"
							value="${uGroup.createLink(controller:'userGroup', action:'about', 'userGroup':userGroupInstance)}">
								${uGroup.createLink(controller:'userGroup', action:'about', 'userGroup':userGroupInstance)}
						</a>
						</li>
						<li class="group_option"><a class=" home_page_label"
							value="${uGroup.createLink(controller:'userGroup', action:'activity', 'userGroup':userGroupInstance)}">
								${uGroup.createLink(controller:'userGroup', action:'activity', 'userGroup':userGroupInstance)}
						</a>
						</li>
						<li class="divider"></li>
						<g:if test="${userGroupInstance.id != null}">
							<g:each var="newsletterInstance"
								in="${userGroupInstance.getPages()}">
								<li class="group_option"><a class=" home_page_label"
									value="${uGroup.createLink(controller:'newsletter', action:'show', id:newsletterInstance.id)}">
									${newsletterInstance.title + " "}
									</a>
								</li>
							</g:each>
						</g:if>
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
				<div class="btn-group" style="margin-top: 10px;">
					<button id="themeSelector" class="btn dropdown-toggle"
						data-toggle="dropdown" href="#" rel="tooltip"
						data-original-title="Theme">
						${userGroupInstance?.theme ?: 'Select theme '}<span class="caret"></span>
					</button>
					<ul class="dropdown-menu" style="width: auto;">
						<g:each var="theme" in="${userGroupInstance?.getThemes()}">
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

<r:script>
$(document).ready(function(){
	$('.home_page_label').each(function() {
		var caret = '<span class="caret"></span>'
		if($.trim(($(this).html())) == $.trim($("#homePageSelector").html().replace(caret, ''))){
			$(this).addClass('active');
		}
	});
	
	$('.theme_label').each(function() {
		var caret = '<span class="caret"></span>'
		if($.trim(($(this).html())) == $.trim($("#themeSelector").html().replace(caret, ''))){
			$(this).addClass('active');
		}
	});
	
});
</r:script>

