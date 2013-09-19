var itemLoadCallback = function(carousel, state) {
	carousel.last = carousel.last?carousel.last:3;
	var params = {
		"limit" : carousel.last - carousel.first,
		"offset" :carousel.first,
		"filterProperty": carousel.options.filterProperty,
		"filterPropertyValue": carousel.options.filterPropertyValue,
		"contextGroupWebaddress":carousel.options.contextGroupWebaddress
	}
	
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
	$(".jcarousel-item").css('width', window.params.carousel.maxWidth);
	var items = data["observations"];
	for (i = 0; i < items.length; i++) {
		var actualIndex = first + i;
		if (!carousel.has(actualIndex)) {
			var item = carousel.add(actualIndex, getItemHTML(carousel, items[i]));
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
	$(".jcarousel-item").css('width', window.params.carousel.maxWidth);
	$(".jcarousel-item  .thumbnail .ellipsis.multiline").trunk8({
		lines:3,		
	});
	
}

function resizeImage(item) {
    var ele = item.find('img');
    var maxHeight=window.params.carousel.maxHeight;
    var maxWidth=window.params.carousel.maxWidth;
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

var getItemHTML = function(carousel, item) {
	var paramsString = "";
	if(carousel.options.filterProperty === "speciesName"){
		paramsString = "?" + encodeURIComponent("species=" + carousel.options.filterPropertyValue);	
	}
	var imageTag = '<img class=img-polaroid src="' + item.imageLink + paramsString  + '" title="' + item.imageTitle  +'" alt="" />';
	var notes = item.notes?item.notes:''
	return '<div class=thumbnail><div class="'+item.type.replace(' ','_')+'_th snippet tablet'+'"><div class=figure><a href='+ item.url + paramsString + '>' + imageTag + '</a></div><div class="'+'ellipsis multiline caption'+'">'+notes+'</div></div></div>';
};

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
