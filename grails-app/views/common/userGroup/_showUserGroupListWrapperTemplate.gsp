<%@page import="species.auth.SUser"%>
<%@ page import="species.groups.UserGroup"%>
<div class="filters" style="position: relative;">
	<obv:showGroupFilter model="['hideAdvSearchBar':true]" />
</div>

<div>
	<!-- main_content -->
	<div class="list">

		<div class="observations thumbwrap">
			<div class="observation">
				<div>
				<%
                def user_group=g.message(code:'text.user.group')
                %>
                <obv:showObservationFilterMessage
						model="['observationInstanceList':userGroupInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'resultType':user_group]" />
				</div>
				<g:if test="${instanceTotal > 0}">
					<div class="btn-group pull-left" style="z-index: 10">
						<button id="selected_sort" class="btn dropdown-toggle"
							data-toggle="dropdown" href="#" rel="tooltip"
							data-original-title="${g.message(code:'showobservationlistwrapertemp.sort')}">


							<g:if test="${params.sort == 'foundedOn'}">
                                               <g:message code="button.latest" /> 
                                            </g:if>
							<g:elseif test="${params.sort == 'score'}">
                                                <g:message code="button.relevancy" />
                                            </g:elseif>
							<g:else>
                                               <g:message code="button.most.viewed" /> 
                                            </g:else>
							<span class="caret"></span>
						</button>
						<ul id="sortFilter" class="dropdown-menu" style="width: auto;">
							<li class="group_option"><a class=" sort_filter_label"
								value="foundedOn"> <g:message code="button.latest" />  </a></li>

							<g:if test="${isSearch}">
								<li class="group_option"><a class=" sort_filter_label"
									value="score"> <g:message code="button.relevancy" />  </a></li>
							</g:if>
							<g:else>
								<li class="group_option"><a class=" sort_filter_label"
									value="visitCount">  <g:message code="button.most.viewed" />  </a></li>
							</g:else>
						</ul>
					</div>


					<uGroup:showUserGroupsList />
				</g:if>
			</div>


		</div>
	</div>

	<!-- main_content end -->
</div>


<!--container end-->
<script text="text/javascript">
$(document).ready(function() {
	window.params.tagsLink = "${uGroup.createLink(controller:'userGroup', action: 'tags')}";

});

$( "#search" ).click(function() {                		
	updateGallery(undefined, ${queryParams?.max}, 0);
	return false;
});
</script>
