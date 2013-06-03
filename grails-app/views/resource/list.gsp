<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />

<g:set var="entityName"
	value="${message(code: 'resource.label', default: 'Resources')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<r:require modules="observations_list,prettyPhoto" />
</head>
<body>

	<div class="span12">
		<uGroup:rightSidebar/>
<div class="">
	<!-- main_content -->
	<div class="list" style="margin-left:0px;clear:both">
		<div class="observations thumbwrap">
			<div class="observation">
				<div class="btn-group pull-left" style="z-index: 10">
					<button id="selected_sort" class="btn dropdown-toggle"
						data-toggle="dropdown" href="#" rel="tooltip"
						data-original-title="Sort by">

						<g:if test="${params.sort == 'visitCount'}">
                                               Most Viewed
                                            </g:if>
						<g:elseif test="${params.sort == 'createdOn'}">
                                                Latest
                                            </g:elseif>
						<g:elseif test="${params.sort == 'score'}">
                                                Relevancy
                                            </g:elseif>
						<g:else>
                                                Last Updated
                                            </g:else>
						<span class="caret"></span>
					</button>
					<ul id="sortFilter" class="dropdown-menu" style="width: auto;">
						<li class="group_option"><a class=" sort_filter_label"
							value="createdOn"> Latest </a></li>
						<li class="group_option"><a class=" sort_filter_label"
							value="lastRevised"> Last Updated </a></li>
						<g:if test="${isSearch}">
							<li class="group_option"><a class=" sort_filter_label"
								value="score"> Relevancy </a></li>
						</g:if>
						<g:else>
							<li class="group_option"><a class=" sort_filter_label"
								value="visitCount"> Most Viewed </a></li>
						</g:else>
					</ul>


				</div>
				
			    </div>
			</div>

                        <rc:showResourceList  model="['resourceInstanceList':resourceInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters, 'userGroup':userGroup]"  />
	
		</div>
	</div>

	<!-- main_content end -->
</div>
<g:javascript>
$(document).ready(function() {
	window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}"
	
});
</g:javascript>
<r:script>
$(document).ready(function(){
    function changepicturecallback() {
        var activeimage = $("#fullResImage");

        var source = activeimage.attr('src');
        if(activeimage) {
            var rating = $("a[href='"+activeimage.attr('src')+"']'").parent().next().children(".rating").children(".ratingForm");
            activeimage.parent().next().next().children(".rating").html(rating.clone());
        }
    }

    var imgRating = rate($(".rating"), function(avgRate, noOfRatings){
        imgRating.select(avgRate);
        $(".rating").find(".noOfRatings").html('('+noOfRatings+' ratings)');
    });
;
    $("a[rel^='prettyPhoto']").prettyPhoto({
        animation_speed: 'fast', /* fast/slow/normal */
        slideshow: 5000, /* false OR interval time in ms */
        autoplay_slideshow: false, /* true/false */
        opacity: 0.80, /* Value between 0 and 1 */
        show_title: true, /* true/false */
        allow_resize: true, /* Resize the photos bigger than viewport. true/false */
        default_width: 500,
        default_height: 344,
        counter_separator_label: '/', /* The separator for the gallery counter 1 "of" 2 */
        theme: 'pp_default', /* light_rounded / dark_rounded / light_square / dark_square / facebook */
        horizontal_padding: 20, /* The padding on each side of the picture */
        hideflash: false, /* Hides all the flash object on a page, set to TRUE if flash appears over prettyPhoto */
        wmode: 'opaque', /* Set the flash wmode attribute */
        autoplay: false, /* Automatically start videos: True/False */
        modal: false, /* If set to true, only the close button will close the window */
        deeplinking: false, /* Allow prettyPhoto to update the url to enable deeplinking. */
        overlay_gallery: true, /* If set to true, a gallery will overlay the fullscreen image on mouse over */
        keyboard_shortcuts: true, /* Set to false if you open forms inside prettyPhoto */
        changepicturecallback: changepicturecallback, /* Called everytime an item is shown/changed */
        callback: function(){}, /* Called when prettyPhoto is closed */
        ie6_fallback: true,
        markup: '<div class="pp_pic_holder"> \
            <div class="ppt">&nbsp;</div> \
            <div class="pp_top"> \
                <div class="pp_left"></div> \
                <div class="pp_middle"></div> \
                <div class="pp_right"></div> \
            </div> \
            <div class="pp_content_container"> \
                <div class="pp_left"> \
                    <div class="pp_right"> \
                        <div class="pp_content"> \
                            <div class="pp_loaderIcon"></div> \
                            <div class="pp_fade"> \
                                <a href="#" class="pp_expand" title="Expand the image">Expand</a> \
                                <div class="pp_hoverContainer"> \
                                    <a class="pp_next" href="#">next</a> \
                                    <a class="pp_previous" href="#">previous</a> \
                                </div> \
                                <div id="pp_full_res"></div> \
                                <div class="pp_details"> \
                                    <div class="pp_nav"> \
                                        <a href="#" class="pp_arrow_previous">Previous</a> \
                                        <p class="currentTextHolder">0/0</p> \
                                        <a href="#" class="pp_arrow_next">Next</a> \
                                    </div> \
                                    <p class="pp_description"></p> \
                                    <div class="rating pull-right" style="padding-top:7px;"></div> \
                                    <a class="pp_close" href="#">Close</a> \
                                </div> \
                            </div> \
                        </div> \
                    </div> \
                </div> \
            </div> \
            <div class="pp_bottom"> \
                <div class="pp_left"></div> \
                <div class="pp_middle"></div> \
                <div class="pp_right"></div> \
            </div> \
        </div> \
        <div class="pp_overlay"></div>',
        image_markup: '<img id="fullResImage" src="{path}" />',
        inline_markup: '<div class="pp_inline">{content}</div>',
        custom_markup: '',
        social_tools: ''
    });
});
</r:script>

	</div>
</body>
</html>
