<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' /></title>
<g:javascript src="jquery/jquery.autopager-1.0.0.js"></g:javascript>
<g:javascript src="jquery/jquery.url.js" />
<g:javascript
	src="jquery/jquery-history-1.7.1/scripts/bundled/html4+html5/jquery.history.js" />

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

				<sUser:showUserListWrapper model="['results':results, 'totalCount':totalCount]"/>
			</div>
		</div>
</div>
	
<g:javascript>
	$(document).ready(function() {
		

		$('.sort_filter_label').click(function() {
			$('.sort_filter_label.active').removeClass('active');
			$(this).addClass('active');
			$('#selected_sort').html($(this).html());
			//$("#search").click();
			updateGallery(undefined, ${params.max}, 0);
			return false;
		});

		$("#removeQueryFilter").live('click', function(){
           	$( "#searchTextField" ).val('');
          	//$("#search").click();
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
	        return sortBy
        }
        
        function getFilterParameters(url, limit, offset, removeUser) {
             var params = url.param();
             var sortBy = getSelectedSortBy();
             if(sortBy) {
                     params['sort'] = sortBy;
             }
             return params;
        }
        
        function updateGallery(target, limit, offset) {
             if(target === undefined) {
                     target = window.location.pathname + window.location.search;
             }
             
             var a = $('<a href="'+target+'"></a>');
             var url = a.url();
             var href = url.attr('path');
             var params = getFilterParameters(url, limit, offset);
             //alert(" tag in params " + params['tag'] );
             
             var recursiveDecoded = decodeURIComponent($.param(params));
             
             var doc_url = href+'?'+recursiveDecoded;
             var History = window.History;
             
             History.pushState({state:1}, "Species Portal", '?'+decodeURIComponent($.param(params))); 
             //alert("doc_url " + doc_url);
             window.location = doc_url;
        }
	});
	
	
</g:javascript>
	
</body>
</html>
