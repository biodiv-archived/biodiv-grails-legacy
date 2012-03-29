
<div id="list_view_bttn" class="list_style_button active"></div>
<div id="grid_view_bttn" class="grid_style_button"></div>
<div class="observations_list" class="observation grid_11">
	<div class="mainContent">
		<div class="grid_view">
			<g:each in="${userInstanceList}" status="i" var="userInstance">
				<sUser:showUserSnippetTablet model="['userInstance':userInstance]"></sUser:showUserSnippetTablet>
			</g:each>
		</div>
		<div class="list_view" style="display: none;">
			<g:each in="${userInstanceList}" status="i" var="userInstance">
				<sUser:showUserSnippet model="['userInstance':userInstance]"></sUser:showUserSnippet>
			</g:each>
		</div>
	</div>
	<g:if test="${userInstanceTotal > queryParams?.max}">
		<div class="button loadMore">
			<span class="progress" style="display: none;"> <img
				src="${resource(dir:'images',file:'spinner.gif', absolute:true)}" />Loading
				... </span> <span class="buttonTitle">Load more</span>
		</div>
	</g:if>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<g:paginate total="${userInstanceTotal}" max="2"
			params="${activeFilters}" />
	</div>
	<script>
                		
                $('#list_view_bttn').click(function(){
			$('.grid_view').hide();
			$('.list_view').show();
			$(this).addClass('active');
			$('#grid_view_bttn').removeClass('active');
			$.cookie("observation_listing", "list");
		});
		
		$('#grid_view_bttn').click(function(){
			$('.grid_view').show();
			$('.list_view').hide();
			$(this).addClass('active');
			$('#list_view_bttn').removeClass('active');
			$.cookie("observation_listing", "grid");
		});
	
                function eatCookies(){
                    if ($.cookie("observation_listing") == "list") {
                            $('.list_view').show();
                            $('.grid_view').hide();
                            $('#grid_view_bttn').removeClass('active');
                            $('#list_view_bttn').addClass('active');
                    }else{
                            $('.grid_view').show();
                            $('.list_view').hide();
                            $('#grid_view_bttn').addClass('active');
                            $('#list_view_bttn').removeClass('active');	
                    }
                }

                eatCookies();

                $.autopager({
                 
                    autoLoad: false,
    		    // a selector that matches a element of next page link
    		    link: 'div.paginateButtons a.nextLink',

    		    // a selector that matches page contents
    		    content: '.mainContent',
                    
                    insertBefore: '.loadMore',
    		
    		    // a callback function to be triggered when loading start 
		    start: function(current, next) {
                        $(".loadMore .progress").show();
                        $(".loadMore .buttonTitle").hide();
		    },
		
		    // a function to be executed when next page was loaded. 
		    // "this" points to the element of loaded content.
		    load: function(current, next) {
		    			$(".mainContent:last").hide().fadeIn(3000);
                        if (next.url == undefined){
                            $(".loadMore").hide();
                        }else{
                            $(".loadMore .progress").hide();
                            $(".loadMore .buttonTitle").show();
                        }
		    }
		});
	
                $('.loadMore').click(function() {
                    $.autopager('load');
                    return false;
                });

        </script>

</div>

