
<!-- label class="control-label" for="username"><g:message
								code='user.username.label' default='Username' />:</label-->

<div style="clear: both"></div>

<g:if test='${searched}'>
	<div class="row">
		<!-- main_content -->
		<div class="list span12">
			<div class="observations thumbwrap">
				<%
def queryParams = [username: username, enabled: enabled, accountExpired: accountExpired, accountLocked: accountLocked, passwordExpired: passwordExpired]
%>
				<div class="controls info-message">

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
					<div>
						<g:if test="${totalCount == 0}">
							<search:noSearchResults />
						</g:if>
						<g:else>
						<span class="name" style="color: #b1b1b1;"><i
							class="icon-user"></i> ${totalCount} </span> User<g:if test="${totalCount!=1}">s</g:if> 
						
						<g:if test="${params.query}">
                                    for search key <span class="highlight">
							<g:link controller="sUser" action="search" params="[query: params.query]">
								${params.query.encodeAsHTML()} <a
								id="removeQueryFilter" href="#">[X]</a></g:link>
							</span>
						</g:if>
						</g:else>
					</div>

				</div>

				<sUser:showUserList
					model="['userInstanceList':results, 'userInstanceTotal':totalCount, 'queryParams':queryParams]" />

			</div>
		</div>
	</div>
</g:if>
</div>