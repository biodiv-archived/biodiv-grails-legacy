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
	if(state == 'init'){
		carousel.size(data["count"]);
	}
};

/**
 * Item html creation helper.
 */
var getItemHTML = function(item){
	var imageTag = '<img src="' + item.imageLink + '" title="' + item.imageTitle  +'" width="75" height="75" alt="" />';
	return '<a href=' + item.obvId + '>' + imageTag + '</a>';
};

var reloadCarousel = function(carousel, fitlerProperty, filterPropertyValue){
	carousel.options.filterProperty = fitlerProperty;
	carousel.options.filterPropertyValue = filterPropertyValue;
	carousel.reset();
}
 
