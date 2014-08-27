
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
                                               <g:message code="button.last.login" /> 
                                            </g:if>
							<g:elseif test="${params.sort == 'name'}">
                                                <g:message code="default.name.label" />
                                            </g:elseif>
                                            <g:elseif test="${params.sort == 'score'}">
                                               <g:message code="button.relevancy" /> 
                                            </g:elseif>
				            <g:else>
                                               <g:message code="button.activity" /> 
                                            </g:else>
							<span class="caret"></span>
						</button>
						<input id="userSearchSort" type="hidden" name="sort"
							value="${params.sort}" />

						<ul id="sortFilter" class="dropdown-menu" style="width: auto;">
							<li class="group_option"><a class="sort_filter_label"
								value="activity"><g:message code="button.activity" />  </a></li>
							<li class="group_option"><a
								class=" sort_filter_label ${params.sort == 'lastLoginDate'?'active':'' }"
								value="lastLoginDate"> <g:message code="button.last.login" />  </a></li>
							<li class="group_option"><a
								class=" sort_filter_label  ${params.sort == 'name'?'active':'' }"
								value="name"> <g:message code="default.name.label" /> </a></li>
							<g:if test="${isSearch}">
								<li class="group_option"><a class=" sort_filter_label"
									value="score"> <g:message code="button.relevancy" /> </a></li>
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
