
<div class="observations_list observation" style="clear: both;">
	<div class="btn-group button-bar" data-toggle="buttons-radio"
		style="float: right;">
		<button class="list_view_bttn btn list_style_button active">
			<i class="icon-align-justify"></i>
		</button>
		<button class="grid_view_bttn btn grid_style_button">
			<i class="icon-th-large"></i>
		</button>
	</div>
	<div class="mainContent">
		<ul class="grid_view thumbnails">
			<g:each in="${observationInstanceList}" status="i"
				var="observationInstance">

				<g:if test="${i%3 == 0}">
					<li class="thumbnail" style="clear: both;">
				</g:if>
				<g:else>
					<li class="thumbnail" style="margin: 0;">
				</g:else>
				<obv:showSnippetTablet
					model="['observationInstance':observationInstance]"></obv:showSnippetTablet>
				</li>

			</g:each>
		</ul>
		<ul class="list_view thumbnails" style="display: none;">
			<g:each in="${observationInstanceList}" status="i"
				var="observationInstance">
				<li class="thumbnail" style="clear: both;"><obv:showSnippet
						model="['observationInstance':observationInstance]"></obv:showSnippet>
				</li>
			</g:each>
		</ul>
	</div>

	<g:if test="${observationInstanceTotal > queryParams.max}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;">Loading ... </span> <span
					class="buttonTitle">Load more</span>
			</div>
		</div>
	</g:if>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<g:paginate total="${observationInstanceTotal}"
			max="${queryParams.max}" action="${activeFilters.action}"
			params="${activeFilters}" />
	</div>
	<script>
                		
       $('.list_view_bttn').click(function(){
			$('.grid_view').hide();
			$('.list_view').show();
			$(this).addClass('active');
                        //alert($(this).attr('class'));
			$('.grid_view_bttn').removeClass('active');
			$.cookie("observation_listing", "list");
		});
		
		$('.grid_view_bttn').click(function(){
			$('.grid_view').show();
			$('.list_view').hide();
                        //alert($(this).attr('class'));
			$(this).addClass('active');
			$('.list_view_bttn').removeClass('active');
			$.cookie("observation_listing", "grid");
		});
	
                function eatCookies(){
                    if ($.cookie("observation_listing") == "list") {
                            $('.list_view').show();
                            $('.grid_view').hide();
                            $('.grid_view_bttn').removeClass('active');
                            $('.list_view_bttn').addClass('active');
                    }else{
                            $('.grid_view').show();
                            $('.list_view').hide();
                            $('.grid_view_bttn').addClass('active');
                            $('.list_view_bttn').removeClass('active');	
                    }
                }

                eatCookies();

                $.autopager({
                 
                    autoLoad: false,
    		    // a selector that matches a element of next page link
    		    link: 'div.paginateButtons a.nextLink',

    		    // a selector that matches page contents
    		    content: '.mainContent',
                    
                    insertAfter: '.mainContent',
    			
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
                        if($('.grid_view_bttn.active')[0]) {
                      	  $('.grid_view').show();
                      	  $('.list_view').hide();
                        } else {
                      	  $('.grid_view').hide();
                      	  $('.list_view').show();
                        }
		    }
		});
	
                $('.loadMore').click(function() {
                    $.autopager('load');
                    return false;
                });

        </script>

</div>

