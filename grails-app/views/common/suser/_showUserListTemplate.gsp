<div class="users_list" style="clear: both;">
	<div class="mainContentList">
		<div class="mainContent">
			<ul class="grid_view thumbnails">
	
				<g:each in="${userInstanceList}" status="i" var="userInstance">
					<g:if test="${i%4 == 0}">
						<li class="thumbnail" style="clear: both;">
					</g:if>
					<g:else>
						<li class="thumbnail" style="margin: 0;">
					</g:else>
					<sUser:showUserSnippetTablet model="['userInstance':userInstance]"></sUser:showUserSnippetTablet>
					</li>
				</g:each>
			</ul>
	
	
			<ul class="list_view thumbnails" style="display: none;">
				<g:each in="${userInstanceList}" status="i" var="userInstance">
					<li class="thumbnail" style="clear: both;"><sUser:showUserSnippet
							model="['userInstance':userInstance]"></sUser:showUserSnippet></li>
				</g:each>
			</ul>
		</div>
	</div>

	<g:if test="${userInstanceTotal > params.max}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;">Loading ... </span> <span
					class="buttonTitle">Load more</span>
			</div>
		</div>
	</g:if>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<g:paginate total="${userInstanceTotal}" max="${params.max}" action="${params.action}"
			params="${params}" />
	</div>

	<r:script>
		$(document).ready(function() {
			$('.list_view_bttn').click(function() {
				$('.grid_view').hide();
				$('.list_view').show();
				$(this).addClass('active');
				//alert($(this).attr('class'));
				$('.grid_view_bttn').removeClass('active');
				$.cookie("observation_listing", "list");
				adjustHeight();
				return false;
			});

			$('.grid_view_bttn').click(function() {
				$('.grid_view').show();
				$('.list_view').hide();
				//alert($(this).attr('class'));
				$(this).addClass('active');
				$('.list_view_bttn').removeClass('active');
				$.cookie("observation_listing", "grid");
				return false;
			});

			function eatCookies() {
				if ($.cookie("observation_listing") == "list") {
					$('.list_view').show();
					$('.grid_view').hide();
					$('.grid_view_bttn').removeClass('active');
					$('.list_view_bttn').addClass('active');
					adjustHeight();
				} else {
					$('.grid_view').show();
					$('.list_view').hide();
					$('.grid_view_bttn').addClass('active');
					$('.list_view_bttn').removeClass('active');
				}
			}

			eatCookies();

			$.autopager({

				autoLoad : false,
				// a selector that matches a element of next page link
				link : 'div.paginateButtons a.nextLink',

				// a selector that matches page contents
				content : '.mainContent',

				appendTo: '.mainContentList',
				//insertBefore : '.loadMore',

				// a callback function to be triggered when loading start 
				start : function(current, next) {
					$(".loadMore .progress").show();
					$(".loadMore .buttonTitle").hide();
				},

				// a function to be executed when next page was loaded. 
				// "this" points to the element of loaded content.
				load : function(current, next) {
					$(".mainContent:last").hide().fadeIn(3000);
					if (next.url == undefined) {
						$(".loadMore").hide();
					} else {
						$(".loadMore .progress").hide();
						$(".loadMore .buttonTitle").show();
					}
					if ($('.grid_view_bttn.active')[0]) {
						$('.grid_view').show();
						$('.list_view').hide();
					} else {
						$('.grid_view').hide();
						$('.list_view').show();
					}
					adjustHeight();
				}
			});

			$('.loadMore').click(function() {
				$.autopager('load');
				return false;
			});
		});
	</r:script>
</div>

