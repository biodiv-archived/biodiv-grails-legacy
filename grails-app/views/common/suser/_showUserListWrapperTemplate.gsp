
<!-- label class="control-label" for="username"><g:message
								code='user.username.label' default='Username' />:</label-->
<div class="controls">
	<!-- div class="input-append">
								<g:textField id="username" class="span3" name='username'
									size='50' maxlength='255' value='${username}'
									class="input-medium search-query" />
								<button id="userSearch" class="btn btn-primary" type="button">
									<g:message code='spring.security.ui.search' default='Search' />
								</button>
							</div-->


	<div class="btn-group" data-toggle="buttons-radio"
		style="float: right;">
		<button class="list_view_bttn btn list_style_button active">
			<i class="icon-align-justify"></i>
		</button>
		<button class="grid_view_bttn btn grid_style_button">
			<i class="icon-th-large"></i>
		</button>

		<div class="btn-group" style="float: left; z-index: 10">
			<button id="selected_sort" class="btn dropdown-toggle"
				data-toggle="dropdown" href="#" rel="tooltip"
				data-original-title="Sort by">
				<g:if test="${params.sort == 'lastLoginDate'}">
                                                Last Login
                                            </g:if>
				<g:elseif test="${params.sort == 'name'}">
                                                Name
                                            </g:elseif>
				<g:else>
                                                Activity
                                            </g:else>
				<span class="caret"></span>
			</button>
			<input id="userSearchSort" type="hidden" name="sort"
				value="${params.sort}" />

			<ul id="sort" class="dropdown-menu" style="width: auto;">
				<li class="group_option"><a class="sort_filter_label"
					value="activity">Activity </a></li>
				<li class="group_option"><a
					class=" sort_filter_label ${params.sort == 'lastLoginDate'?'active':'' }"
					value="lastLoginDate"> Last Login </a></li>
				<li class="group_option"><a
					class=" sort_filter_label  ${params.sort == 'name'?'active':'' }"
					value="name"> Name </a></li>
			</ul>


		</div>

	</div>
</div>

<div style="clear: both"></div>

<g:if test='${searched}'>
	<div class="row">
		<!-- main_content -->
		<div class="list span12">
			<div class="observations thumbwrap">
				<%
def queryParams = [username: username, enabled: enabled, accountExpired: accountExpired, accountLocked: accountLocked, passwordExpired: passwordExpired]
%>

				<sUser:showUserList
					model="['userInstanceList':results, 'userInstanceTotal':totalCount, 'queryParams':queryParams]" />

			</div>
		</div>
	</div>
</g:if>
</div>