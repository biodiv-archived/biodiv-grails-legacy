/**
 * 
 */
$(document).ready(function() {
	$('.list_view_bttn').live('click', function() {
		$('.grid_view').hide();
		$('.list_view').show();
		$(this).addClass('active');
		// alert($(this).attr('class'));
		$('.grid_view_bttn').removeClass('active');
		$.cookie("observation_listing", "list");
		adjustHeight();
		return false;
	});

	$('.grid_view_bttn').live('click', function() {
		$('.grid_view').show();
		$('.list_view').hide();
		// alert($(this).attr('class'));
		$(this).addClass('active');
		$('.list_view_bttn').removeClass('active');
		$.cookie("observation_listing", "grid");
		return false;
	});

	eatCookies();

	$('.loadMore').live('click', function() {
		$.autopager({

			autoLoad : false,
			// a selector that matches a element of next page link
			link : 'div.paginateButtons a.nextLink',

			// a selector that matches page contents
			content : '.mainContent',

			appendTo : '.mainContentList',
			// insertBefore : '.loadMore',

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
				eatCookies();
			}
		});
		$.autopager('load');
		return false;
	});
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
