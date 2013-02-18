var itemLoadCallback = function(carousel, state) {
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
	var items = data["observations"];
	for (i = 0; i < items.length; i++) {
		var actualIndex = first + i;
		if (!carousel.has(actualIndex)) {
			var item = carousel.add(actualIndex, getItemHTML(carousel.options.contextFreeUrl, carousel.options.contextGroupWebaddress, items[i]));
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
}

function resizeImage(item) {

	var ele = item.find('img');
	var maxHeight=75;
	var maxWidth=75;
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
   	
   	
	/*
	 var maxWidth = 75; // Max width for the image
        var maxHeight = 75;    // Max height for the image
        var ratio = 0;  // Used for aspect ratio
        var width = $(this).width();    // Current image width
        var height = $(this).height();  // Current image height
        

        // Check if current height is larger than max
        if(height > maxHeight){
            ratio = maxHeight / height; // get ratio for scaling image
            $(this).css("height", maxHeight);   // Set new height
            $(this).css("width", width * ratio);    // Scale width based on ratio
            width = width * ratio;    // Reset width to match scaled image
            height = maxHeight;
        }
        console.log(width+"  "+height);
        // Check if the current width is larger than the max
        if(width > maxWidth){
            ratio = maxWidth / width;   // get ratio for scaling image
            $(this).css("width", maxWidth); // Set new width
            $(this).css("height", maxHeight);  // Scale height based on ratio
            width = maxWidth;
            height = maxHeight    // Reset height to match scaled image
        }
        
        console.log(width+"  "+height);
        $(this).css("margin-left", (maxWidth - width)/2);
        $(this).css("margin-top", (maxHeight - height)/2);*/

}
/**
 * Item html creation helper.
 */
var getItemHTML = function(contextFreeUrl, contextGroup, item) {
	var imageTag = '<img src="' + item.imageLink + '" title="' + item.imageTitle  +'" alt="" />';
	/*
	if(contextGroup){
		if(item.groupContextLink){
			return '<a href='+item.groupContextLink +'/'+ item.obvId + '>' + imageTag + '</a>';
		}else{
			return '<a  target="_blank" href='+contextFreeUrl +'/'+ item.obvId + '>' + imageTag + '</a>';
		}
	}
	*/
	return '<a href='+contextFreeUrl +'/'+ item.obvId + '>' + imageTag + '</a>';
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

/*
var itemVisibleInCallback  = function(carousel, item, idx, state) {
	console.log(item);
	console.log(idx);
	console.log(state);
	carousel.buttons(true);
   // if (carousel.first == 1) { $("#editos .jcarousel-prev").css("visibility", "hidden"); } else { $("#editos .jcarousel-prev").css("visibility", "visible"); }
    //if (carousel.last == carousel.size()) { $("#editos .jcarousel-next").css("visibility", "hidden"); } else { $("#editos .jcarousel-next").css("visibility", "visible"); }
}
var buttonNextCallback  = function(carousel, ele, flag) {
	console.log(carousel);
	console.log(ele);
	console.log(flag);

}*/
