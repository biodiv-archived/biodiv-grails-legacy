
<!-- label class="control-label" for="username"><g:message
								code='user.username.label' default='Username' />:</label-->

<g:if test='${searched}'>
	<%
def queryParams = [username: username, enabled: enabled, accountExpired: accountExpired, accountLocked: accountLocked, passwordExpired: passwordExpired]
%>


	<div class="">
		<div class="list">
			<div class="observation thumbwrap">
				<div class="observation">
					<div>
						<obv:showObservationFilterMessage
							model="['instanceTotal':instanceTotal, resultType:'user']" />
					</div>
					<div style="clear: both;"></div>
					<div class="btn-group" style="z-index: 10">
						<button id="selected_sort" class="btn dropdown-toggle"
							data-toggle="dropdown" href="#" rel="tooltip"
							data-original-title="Sort by">
							<g:if test="${params.sort == 'lastLoginDate'}">
                                                Last Login
                                            </g:if>
							<g:elseif test="${params.sort == 'name'}">
                                                Name
                                            </g:elseif>
                                            <g:elseif test="${params.sort == 'score'}">
                                                Relevancy
                                            </g:elseif>
				            <g:else>
                                                Activity
                                            </g:else>
							<span class="caret"></span>
						</button>
						<input id="userSearchSort" type="hidden" name="sort"
							value="${params.sort}" />

						<ul id="sortFilter" class="dropdown-menu" style="width: auto;">
							<li class="group_option"><a class="sort_filter_label"
								value="activity">Activity </a></li>
							<li class="group_option"><a
								class=" sort_filter_label ${params.sort == 'lastLoginDate'?'active':'' }"
								value="lastLoginDate"> Last Login </a></li>
							<li class="group_option"><a
								class=" sort_filter_label  ${params.sort == 'name'?'active':'' }"
								value="name"> Name </a></li>
							<g:if test="${isSearch}">
								<li class="group_option"><a class=" sort_filter_label"
									value="score"> Relevancy </a></li>
							</g:if>
						
						</ul>
					</div>
				</div>
				<sUser:showUserList
					model="['userInstanceList':userInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'userGroupInstance':userGroupInstance]" />
			</div>
		</div>
	</div>
</g:if>
