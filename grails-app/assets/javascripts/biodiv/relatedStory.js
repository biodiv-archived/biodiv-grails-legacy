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
function relatedStory_latestObvs(relatedInstanceList, filterProperty, id, userGroupWebaddress, filterPropertyValue){
    var carouselOptions = {
        itemLoadCallback : itemLoadCallbackBiodivApi,
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

var itemLoadCallbackBiodivApi = function(carousel, state) {
    carousel.last = carousel.last?carousel.last:3;
    var limit = carousel.last - carousel.first + 1;
    if(limit <=0) limit = carousel.options.scroll;
    var params = {
        "limit" : limit,
        "offset" :carousel.first,
        "filterProperty": carousel.options.filterProperty,
        "filterPropertyValue": carousel.options.filterPropertyValue,
        "contextGroupWebaddress":carousel.options.contextGroupWebaddress
    }
	if (state == 'prev'){
		return;
	}
	params.offset = carousel.first - 1;
	if(carousel.last == carousel.options.size){
		params.limit = carousel.last;
	}
	var jqxhr = $.get(carousel.options.url, params, function(data) {
        var model = prepareModel(data);
		itemAddCallback(carousel, carousel.first, carousel.last, model, state);
	});
	$(".jcarousel-item  .thumbnail .ellipsis.multiline").trunk8({
		lines:3	
	});

	$(".jcarousel-item  .thumbnail .ellipsis").trunk8({lines:1});
}

function getUrl(thumbnail,speciesGroup,videos){

    let IBP_URL = window.params.IBPDomainUrl;
    let group=speciesGroup.toLowerCase();
    let groupIcon=null;
    groupIcon=IBP_URL+'/biodiv/group_icons/speciesGroups/'+group+'_th1.png';
    let res = thumbnail?thumbnail.split("."):null;

    if(res){
        if(res[1]=="mp3" || res[1]=="wav"){
            return IBP_URL+'/biodiv/assets/all/audioicon.png';
        }
        else if(res[0]=="v"){
            let url = videos[0];
            let videoid = url.match(/(?:https?:\/{2})?(?:w{3}\.)?youtu(?:be)?\.(?:com|be)(?:\/watch\?v=|\/)([^\s&]+)/);
            if(videoid != null) {
                let imageUrl="https://img.youtube.com/vi/"+videoid[1]+"/0.jpg";
                return imageUrl
            }
        }
        else{
            return IBP_URL+'/biodiv/observations/'+res[0]+"_th1.jpg"
        }
    }
    else {
        return groupIcon
    }
}


function prepareModel(elasticResult) {
    var model = {};
    model.observations = new Array();
    jQuery.each(elasticResult.documents, function(i, val) {
        var t = getUrl(val.thumbnail, val.speciesgroupname,val.urlresource);
        var obv = {'habitat':val.habitatname, 'id':parseInt(val.id),'imageLink':t, 'lat':val.latitude, 'lng':val.longitude,'notes':val.notes,'observedOn':val.fromDate, 'sGroup':val.speciesgroupname, 'summary':'', 'title':val.name,'type':'observation', 'url':'/observation/show/'+val.id};
        model.observations.push(obv);
    });
    return model
}
