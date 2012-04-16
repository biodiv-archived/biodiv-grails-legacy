var itemLoadCallback = function(carousel, state) {
	var params = {
		"limit" : carousel.last - carousel.first + 1,
		"offset" : carousel.first - 1,
		"filterProperty": carousel.options.filterProperty,
		"filterPropertyValue": carousel.options.filterPropertyValue
		
	}
	if (state == 'prev')
		return;

	var jqxhr = $.get(carousel.options.url, params, function(data) {
		itemAddCallback(carousel, carousel.first, carousel.last, data, state);
	});
	// jqxhr.error(function() { alert("error"); });
}

var itemAddCallback = function(carousel, first, last, data, state) {
	var items = data["observations"];
	for (i = 0; i < items.length; i++) {
		var actualIndex = first + i
		if (!carousel.has(actualIndex)) {
			carousel.add(actualIndex, getItemHTML(items[i]));
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
	
	
	$('.jcarousel-item a img').each(function() {
        var maxWidth = 75; // Max width for the image
        var maxHeight = 75;    // Max height for the image
        var ratio = 0;  // Used for aspect ratio
        var width = $(this).width();    // Current image width
        var height = $(this).height();  // Current image height
        // Check if the current width is larger than the max
        if(width > maxWidth){
            ratio = maxWidth / width;   // get ratio for scaling image
            $(this).css("width", maxWidth); // Set new width
            $(this).css("height", height * ratio);  // Scale height based on ratio
            $(this).css("margin-top", (maxHeight - height * ratio)/2);
            height = height * ratio;    // Reset height to match scaled image
            width = width * ratio;    // Reset width to match scaled image
        }

        // Check if current height is larger than max
        if(height > maxHeight){
            ratio = maxHeight / height; // get ratio for scaling image
            $(this).css("height", maxHeight);   // Set new height
            $(this).css("width", width * ratio);    // Scale width based on ratio
            $(this).css("margin-left", (maxWidth - width * ratio)/2);
            width = width * ratio;    // Reset width to match scaled image
        }
    });

};

/**
 * Item html creation helper.
 */
var getItemHTML = function(item) {
	var imageTag = '<img style="height:100%;" src="' + item.imageLink + '" title="' + item.imageTitle  +'" alt="" />';
	return '<a href=/biodiv/observation/show/' + item.obvId + '>' + imageTag + '</a>';
};

var reloadCarousel = function(carousel, fitlerProperty, filterPropertyValue){
	carousel.options.filterProperty = fitlerProperty;
	carousel.options.filterPropertyValue = filterPropertyValue;
	carousel.reset();
}