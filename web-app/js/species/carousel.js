var itemLoadCallback = function(carousel, state) {
        carousel.last = carousel.last?carousel.last:3;
	var params = {
		"limit" : carousel.last - carousel.first,
		"offset" :carousel.first,
		"filterProperty": carousel.options.filterProperty,
		"filterPropertyValue": carousel.options.filterPropertyValue,
		"contextGroupWebaddress":carousel.options.contextGroupWebaddress
	}
        if(params.limit == 0)
            params.limit = 3;

	if (state == 'prev'){
		return;
	}
	params.offset = carousel.first -1;
	if(carousel.last == carousel.options.size){
		params.limit = carousel.last;
	}
	var jqxhr = $.get(carousel.options.url, params, function(data) {
		itemAddCallback(carousel, carousel.first, carousel.last, data, state);
	});
	// jqxhr.error(function() { alert("error"); });
}

var itemAddCallback = function(carousel, first, last, data, state) {
	var items = data["observations"];
	for (i = 0; i < items.length; i++) {
		var actualIndex = first + i;
		if (!carousel.has(actualIndex)) {
			var item = carousel.add(actualIndex, carousel.options.getItemHTML(carousel, items[i]));
			resizeImage(item);
		}
	}
	if(state == 'init') {
		if(data["count"] == 0) {
			$(carousel.options.carouselDivId).hide();
			if(carousel.options.filterProperty == 'user'){
				$(carousel.options.carouselAddObvDivId).show();
			}else{ 
				$(carousel.options.carouselMsgDivId).show();
			}
		} else {
			carousel.size(data["count"]);
		}
	}	
	$(".jcarousel-item-horizontal").css('width', '75px');
	$(".jcarousel-item  .thumbnail .ellipsis.multiline").trunk8({
		lines:3,		
	});
        
                /*            
        $(".jcarousel-item").popover({ 
            title: function() {
                return $(this).find('img').attr('title')
            },
            content: function() {
                return $(this).find(".caption").html()
            },
            trigger:(is_touch_device ? "click" : "hover"),
            html:true,
            container:'body'
        });
        */
	
}

function resizeImage(item) {
    var ele = item.find('img');
    var maxWidth=item.hasClass('.jcarousel-item-horizontal') ? window.params.carousel.maxWidth : '100%';
    var maxHeight=item.hasClass('.jcarousel-item-horizontal') ? '75px':window.params.carousel.maxHeight;
    var width = ele.width();    // Current image width
    var height = ele.height();  // Current image height
    if(height > maxHeight){
        item.css('height', maxHeight);
    } 

    if(width == 0) {
        width = maxWidth;
    }

    if(width > maxWidth) {
        ele.css('position','absolute').css('left',(0-(Math.abs(maxWidth-width)/2)));
    }
    item.css('width', Math.min(maxWidth, width)).css('overflow', 'hidden');
}

var reloadCarousel = function(carousel, fitlerProperty, filterPropertyValue){
	carousel.options.filterProperty = fitlerProperty;
	carousel.options.filterPropertyValue = filterPropertyValue;
	var visibleOffset = carousel.last - carousel.first;
	carousel.reset();
	carousel.first = 1;
	carousel.last = carousel.first + visibleOffset;
	itemLoadCallback(carousel, 'init');
}

var itemAfterLoadCallback = function(carousel, state) {
	$(".jcarousel-item  .thumbnail .ellipsis.multiline").trunk8({
		lines:3,		
	});
}

var initCallback = function(carousel, status) {
    $(".jcarousel-prev-vertical").append("<i class='icon-chevron-up'></i>").hover(function(){
        $(this).children().addClass('icon-black');    
    }, function(){
        $(this).children().removeClass('icon-black');    
    });

    $(".jcarousel-next-vertical").append("<i class='icon-chevron-down'></i>").hover(function(){
        $(this).children().addClass('icon-black');    
    }, function(){
        $(this).children().removeClass('icon-black');    
    });

   
}

var setupCallback = function(carousel) {
}
var getSnippetHTML = function(carousel, item) {
	var paramsString = "";
	if(carousel.options.filterProperty === "speciesName"){
		paramsString = "?" + encodeURIComponent("species=" + carousel.options.filterPropertyValue);	
	}
	var imageTag = '<img class=img-polaroid src="' + item.imageLink + paramsString  + '" title="' + item.imageTitle  +'" alt="" />';

	var notes = item.notes?item.notes:''
        //TODO:split this into separate methods so that figure badge story parts can be build independently
	var eleHTML = '<div class=thumbnail>'+
                '<div class="'+item.type.replace(' ','_')+'_th snippet'+'">'+
                    '<span class="badge featured"> </span>'+
                    '<div class="figure pull-left observation_story_image">'+
                            '<a href='+ item.url + paramsString + '>' + imageTag + '</a>'+
                    '</div>'+
                    '<div class="'+'observation_story'+'">'+
                        '<div class="observation-icons">'

        eleHTML +=       (item.habitat)?
                            '<span style="float:right;" class="habitat_icon group_icon habitats_sprites active '+item.habitat.toLowerCase()+'_gall_th" title="'+item.habitat+'"></span>':''
        eleHTML +=       (item.sGroup)?
                            '<span style="float:right;" class="group_icon species_groups_sprites active '+item.sGroup.toLowerCase()+'_gall_th" title="'+item.sGroup+'"></span>':''

        eleHTML +=       '</div>'+
                    '<div class="featured_title ellipsis">'

        eleHTML +=      '<div class="heading">'+
                            '<a href='+ item.url + paramsString + '><span class="ellipsis">'+item.imageTitle + '</span></a>'+
                        '</div>'+
                        '<small style="font-weight:normal;"> featured on <time class="timeago" datetime="'+new Date(item.featuredOn)+'">'+$.datepicker.formatDate('M dd yy',new Date(item.featuredOn))+'</time> </small>'+
                    '</div>'+
                    '<div class="featured_notes linktext">'+item.notes+'</div>'+
                '<div>'+
            '</div></div>'
        return eleHTML;
};

var getSnippetTabletHTML = function(carousel, item) {
	var paramsString = "";
	if(carousel.options.filterProperty === "speciesName"){
		paramsString = "?" + encodeURIComponent("species=" + carousel.options.filterPropertyValue);	
	}
	var imageTag = '<img class=img-polaroid src="' + item.imageLink + paramsString  + '" title="' + item.imageTitle  +'" alt="" />';

	var notes = item.notes?item.notes:''
	return '<div class=thumbnail><div class="'+item.type.replace(' ','_')+'_th snippet tablet'+'"><div class=figure><a href='+ item.url + paramsString + '>' + imageTag + '</a></div><div class="'+'ellipsis multiline caption'+'">'+notes+'</div></div></div>';

}
