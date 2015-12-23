function relatedStory(relatedInstanceList, filterProperty, id, userGroupWebaddress, filterPropertyValue){
    var carouselOptions = {
        itemLoadCallback : itemLoadCallback,
        initCallback : initCallback,
        setupCallback : setupCallback,
        url: $("#carousel_"+id).data("url"),
        filterProperty:filterProperty,
        filterPropertyValue:filterPropertyValue,
        carouselDivId:"#carousel_" + id,
        carouselMsgDivId:"#relatedObservationMsg_" + id,
        carouselAddObvDivId:"#relatedObservationAddButton_" + id,
        itemFallbackDimension : window.params.carousel.maxWidth,
        contextFreeUrl:$("#carousel_"+id).data("contextFreeUrl"),
        contextGroupWebaddress:userGroupWebaddress 
    }
    if(filterProperty == 'featureBy'){
        carouselOptions['vertical'] = true;
        carouselOptions['scroll'] = 1;
        carouselOptions['getItemHTML'] = getSnippetHTML;
    } else{
        carouselOptions['getItemHTML'] = getSnippetTabletHTML;
        carouselOptions['horizontal'] = true;
    }

    if (relatedInstanceList || filterProperty != 'featureBy'){
        $('#carousel_'+id).jcarousel(carouselOptions);
    }
}
