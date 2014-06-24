<%@ page import="species.utils.Utils"%>
<html>
<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' /></title>
<r:require modules="userGroups_list"/>
</head>

<body>

	<div class="span12">
		<div class="page-header">
			<search:searchResultsHeading />
		</div>
		<uGroup:rightSidebar/>
		<div class="searchResults" style="margin-left:0px;">
			<uGroup:showUserGroupsListWrapper
				model="['totalUserGroupInstanceList':totalUserGroupInstanceList, 'userGroupInstanceList':UserGroupInstanceList, 'userGroupInstanceTotal':userGroupInstanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters]" />
		</div>
	</div>


	<r:script>
	$(document).ready(function() {

		$('.sort_filter_label').click(function() {
			$('.sort_filter_label.active').removeClass('active');
			$(this).addClass('active');
			$('#selected_sort').html($(this).html());
			$("#search").click();
			return false;
		});

		$("#removeQueryFilter").on('click', function(){
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
