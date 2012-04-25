<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' />
</title>
<g:javascript src="jquery/jquery.autopager-1.0.0.js"></g:javascript>
<g:set var="entityName"
	value="${message(code: 'sUser.label', default: 'Users')}" />

<style type="text/css">
.snippet.tablet .figure img {
	height: auto;
}

.figure .thumbnail {
	height: 120px;
	margin: 0 auto;
	text-align: center;
	*font-size: 120px;
	line-height: 120px;
}
</style>

</head>

<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header">
					<h1>
						<g:message code="default.list.label" args="[entityName]" />
					</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>


				<div class="filters">
					<g:form action='userSearch' name='userSearchForm' method="GET"
						id="userSearchForm" class="well form-horizontal ">
						<label class="control-label" for="username"><g:message
								code='user.username.label' default='Username' />:</label>
						<div class="controls">
							<div class="input-append">
								<g:textField id="username" class="span3" name='username'
									size='50' maxlength='255' value='${username}'
									class="input-medium search-query" />
								<button id="userSearch" class="btn btn-primary" type="button">
									<g:message code='spring.security.ui.search' default='Search' />
								</button>
							</div>


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
									<input id="userSearchSort" type="hidden" name="sort" value="${params.sort}"/>

									<ul id="sort" class="dropdown-menu" style="width: auto;">
										<li class="group_option"><a class="sort_filter_label"
											value="activity">Activity </a>
										</li>
										<li class="group_option"><a
											class=" sort_filter_label ${params.sort == 'lastLoginDate'?'active':'' }"
											value="lastLoginDate"> Last Login </a>
										</li>
										<li class="group_option"><a
											class=" sort_filter_label  ${params.sort == 'name'?'active':'' }"
											value="name"> Name </a>
										</li>
									</ul>


								</div>

							</div>
						</div>

					</g:form>

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
		</div>
	</div>

	<script>
	$(document).ready(function() {
		$("#username").focus().autocomplete({
			minLength: 3,
			cache: false,
			source: "${createLink(action: 'ajaxUserSearch')}"
		});

		$('.sort_filter_label').click(function() {
			$('.sort_filter_label.active').removeClass('active');
			$(this).addClass('active');
			$('#selected_sort').html($(this).html());
			$("#userSearch").click();
			return false;
		});

		function getSelectedSortBy() {
			var sortBy = '';
			$('.sort_filter_label').each(function() {
				if ($(this).hasClass('active')) {
					sortBy += $(this).attr('value') + ',';
				}
			});

			sortBy = sortBy.replace(/\s*\,\s*$/, '');
			return sortBy;
		}

		$("#userSearch").click(
				function() {
					var sortParam = getSelectedSortBy();
					if (sortParam) {
						$("#userSearchSort").val(sortParam);
					}
					$("#userSearchForm").submit();
				});

	});
	</script>

</body>
</html>
