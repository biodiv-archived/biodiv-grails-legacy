<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' /></title>
<r:require modules="susers_list"/>
<g:set var="entityName"
	value="${message(code: 'searchlabel', default: 'Search Results')}" />

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
					<search:searchResultsHeading/>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<div class="searchResults">
					<sUser:showUserListWrapper/>
				</div>
				</div>
			</div>
		</div>

		<r:script>
	$(document).ready(function() {
		/*$("#username").focus().autocomplete({
			minLength: 3,
			cache: false,
			source: "${createLink(action: 'ajaxUserSearch')}"
		});*/

		$('.sort_filter_label').click(function() {
			$('.sort_filter_label.active').removeClass('active');
			$(this).addClass('active');
			$('#selected_sort').html($(this).html());
			$("#search").click();
			return false;
		});

		$("#removeQueryFilter").live('click', function(){
           	$( "#searchTextField" ).val('');
          	$("#search").click();
           	return false;
        });
	});
	
	$( "#search" ).unbind('click');
	
	$( "#search" ).click(function() {
		var sortBy = '';
		$('.sort_filter_label').each(function() {
			if ($(this).hasClass('active')) {
				sortBy += $(this).attr('value') + ',';
			}
		});

		sortBy = sortBy.replace(/\s*\,\s*$/, '');
		
		var sortParam = sortBy;
		if (sortParam) {
			$("#searchBoxSort").val(sortParam);
		}
		$("#searchBox").submit();
	});
	</r:script>
</body>
</html>
