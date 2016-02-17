var carouselLinks =[],gallery,defaultThumb ;

//For Audio 
var audio;
var playlist;
var tracks;
var current;

//audioInit();
function audioInit(){
    current = 0;
    audio = $('audio');
    playlist = $('#playlist');
    tracks = playlist.find('li a');
    len = tracks.length - 1;
    audio[0].volume = .10;
    //audio[0].play();
    playlist.find('a').click(function(e){
        e.preventDefault();
        link = $(this);                
        $('.audioAttr').hide();
        $('.audioAttr_'+$(this).attr('rel')).show();
        current = link.parent().index();
        run(link, audio[0]);    
    });
    /*    audio[0].addEventListener('ended',function(e){
          current++;
          if(current == len){
          current = 0;
          link = playlist.find('a')[0];
          }else{
          link = playlist.find('a')[current];    
          }
          run($(link),audio[0]);
          }); */
}
String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

function run(link, player){
    player.src = link.attr('href');
    par = link.parent();
    par.addClass('active').siblings().removeClass('active');
    audio[0].load();
    audio[0].play();
}

function youtube_parser(url){
    var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
    var match = url.match(regExp);
    return (match&&match[7].length==11)? match[7] : false;
}

function updateGallery1(resources,domainObj){
    if(resources.length == 0){
        console.log('Error: Resources is Empty');
        $('.galleryWrapper, #gallerySpinner').hide();
        return false;
    }        
    initializeGallery(resources,domainObj);        

    rate($('.star_gallery_rating'));   
    $('.mover img').css('opacity','initial');


}
function initializeGallery(resources,domainObj){
    console.log(resources.length);
    var isAudio = [];
    var isImageOrVideo = [];
    $.each(resources, function (index, photo) {
        if(photo.type == 'Image' || photo.type == 'Video'){
            isImageOrVideo.push(photo);
        }else if(photo.type == 'Audio'){
            isAudio.push(photo);
        }

    });
    var gallCount =0;
    $.each(isImageOrVideo, function (index, photo) {
        gallCount +=1;
        photo.url = (photo.url)?photo.url:defaultThumb.replace('_th1','_gall');
        photo.icon = (photo.icon)?photo.icon:defaultThumb;
        if(photo.type == 'Image' || photo.type == 'Video'){
            // Adding Thumbnail
            $('.jc_ul').append('<li><img class="thumb img-polaroid thumb_'+index+'" rel="'+index+'" src="'+photo.icon+'" /></li>');

        }    
        // For Slider    
        //TODO some More fix here
        photo.url = (photo.url.indexOf('/biodiv') != -1)?photo.url.replace('.jpg','_gall.jpg'): photo.url;

        if(photo.type == 'Image'){
            carouselLinks.push({
                href: photo.url,
                title: gallCount+'/'+isImageOrVideo.length
            });
        }else if(photo.type == 'Video'){
            carouselLinks.push({
                youtube: youtube_parser(photo.url),
                type: 'text/html',
                title: gallCount+'/'+isImageOrVideo.length
            });
        }

        update_imageAttribute(photo,$('.image_info'),index);
    });

    if(isAudio.length >= 1){
        if(domainObj == 'observation') {
            $('.galleryWrapper').after('<div class="audio_container"></div>');
        }else{
            $('#resourceTabs').after('<div class="audio_container" style="height:110px;"></div>');        
        }

        $('.audio_container').html('<audio class="audio_cls" controls style="padding: 8px 0px 0px 0px;width: 100%;"><source src="'+isAudio[0]['url'].replace('biodiv/','biodiv/observations/')+'" type="audio/mpeg"></audio>');
        $.each(isAudio, function (index, resource) {
            $('.audio_container').append(update_imageAttribute(resource,$('.audio_container'),index));
        });
        $('.audio_container div').first().show();
    }

    if(isAudio.length >= 2) {
        var audio_playlist = '<ul id="playlist" style="padding: 5px 0px 2px 0px;margin: 0px;">';
        $.each(isAudio, function (index, audio) {
            audio_playlist += '<li class="active" style="display: inline;">';
            audio_playlist += '<a href="'+audio.url.replace('biodiv/','biodiv/observations/')+'" class="btn btn-small btn-success" rel="'+index+'"  >Audio '+index+'</a>';
            audio_playlist += '</li>';
        });
        audio_playlist += '</ul>';
        if(domainObj == 'observation') {
            $('.audio_container').css('height','150px');
        }else{
            $('.audio_container').css('height','140px')
        }
        $('.audio_container').prepend(audio_playlist);
        audioInit();        
    }


    $('.jc').jcarousel();
    console.log(carouselLinks);
    // Initialize the Gallery as image carousel:
    gallery = blueimp.Gallery(carouselLinks, {
        container: '#blueimp-image-carousel',
            carousel: false, 
            titleElement: 'h6',
            onopen: function () {
                // Callback function executed when the Gallery is initialized.
                $('#gallerySpinner').hide();
                $('.gallery_wrapper').show();

                // $('.image_info div').first().show();
                // alert("fddddd");
            },           
            onslideend: function (index, slide) {                
                // Callback function executed after the slide change transition.
                $('.thumb').css('opacity','0.6');
                $('.thumb_'+index).css('opacity','initial');
                if($('.imageAttr_'+index,'.videoAttr_'+index).hasClass('open')){
                    $('.image_info').css('height','auto');
                }
                $('.imageAttr, .videoAttr').hide();
                $('.imageAttr_'+index+', .videoAttr_'+index).show();
                $('.thumb').removeClass('active');
                $('.thumb_'+index).addClass('active');
                //$('.image_info').html(index);                        
            },          
    });
}

function update_imageAttribute(resource,ele,index){
    var output = '';
    var resourceType = resource.type.toLowerCase();  

    output += '<div class="row-fluid '+resourceType+'Attr '+resourceType+'Attr_'+index+'" style="display:none;">';
    output += '<div>';

    if(resource.description && resource.description != ''){
        output += '<div class="span12 ellipsis multiline" style="margin-left:0px">'+resource.description+'</div>';            
    }


    output += '<div class="conts_wrap">';
    output += '<div class="span12">';
    output += '<div class="span6">';
    if(resource.contributors && Object.keys(resource.contributors).length > 0){

            output += '<div class="conts_wrap_line">';
            output += '<h5>Contributors</h5>';
            console.log(resource.contributors);        
            $.each(resource.contributors, function (index, contributor) {     
                output += '<ol>';
                output += '<li>'+contributor.name+'</li>';
                output += '</ol>';
            }); 
            output += '</div>';            
        } 

    if(resource.attributors && Object.keys(resource.attributors).length > 0){
        output += '<div class="conts_wrap_line">';            
        output += '<h5>Attributors</h5>';
        console.log(resource.attributors);        
        $.each(resource.attributors, function (index, attributor) {     
            output += '<ol>';
            output += '<li>'+attributor.name+'</li>';
            output += '</ol>';
        });
        output += '</div>';             
    } 

    if(resource.url && resource.url !=''){
        output += '<div class="conts_wrap_line">';
        output += '<a href="'+resource.url+'" target="_blank"><b>View image source</b> </a>';
        output += '</div>';
    }
    output += '</div>';


    output += '<div class="span6">';
    output += '<div class="license">';
    output += '<a class="span7" href="'+resource.license['url']+'" target="_blank">';
    output += '<img class="icon" style="height:auto;margin-right:2px;" src="../../../assets/all/license/'+resource.license['name'].replace(' ','_').toLowerCase()+'.png" alt="'+resource.license['name']+'">';
    output += '</a>';
    output += '<div class="rating_form span4">';
    output += '<form class="ratingForm" method="get" title="Rate it">';
    output += '<span class="star_gallery_rating pull-right" title="Rate" data-score="'+resource.averageRating+'" data-input-name="rating" data-id="'+resource.id+'" data-type="resource" data-action="like" >';
    output += '</span>';
    output += '<div class="noOfRatings">'; 
    var ratings ='';
    if(resource.rating != 1){
        ratings = 'ratings';
    }else{
        ratings = 'rating';
    }                     
    output += '('+resource.totalRatings+' rating'+(resource.totalRatings>1?'s':'')+')';
    output += '</div>';
    output += '</form>';
    output += '</div>'; 
    output += '<i class="slideUp pull-right open icon-chevron-down" rel="58"></i>';                                       
    output += '</div>';
    output += '</div>';
    output += '</div>';

    if(resource.annotations && (Object.keys(resource.annotations).length > 0)){
        output += '<div class="span12" style="margin-left:0px;">';
        output += '<div class="conts_wrap_line">';            
        output += '<h5>Annotations</h5>';
        output += '<div class="annotationsWrapper">';
        output += '<table class="table" style="margin: 0px;table-layout:fixed;display:block;overflow-y:auto;">';
        output += '<tbody>';
        $.each(resource.annotations, function (key,annotation) {
            if(annotation.value){
                console.log(annotation.value);
                output += '<tr>';
                output += '<td style="word-wrap:break-word;">';
                if($.isArray(annotation.value) && annotation.value.url) {
                    output += '<a href="'+annotation.value.url+'">'+annotation.key.replace("_", " ").capitalize()+'</a>';
                }else{
                    output += key.replace("_", " ").capitalize();
                }
                output += '</td>';
                output += '<td class="ellipsis multiline linktext" style="word-wrap:break-word;">';
                if($.isArray(annotation.value)){
                    output += annotation.value.value;
                }else{
                    output += annotation.value;
                }
                output += '</td>';
                output += '</tr>';
            }
        });
        output += '</tbody>';
        output += '</table>';
        output += '</div>';
        output += '</div>';
        output += '</div>';
    }


    output += '</div>';
    output += '</div>';
    output += '</div>';            
    ele.append(output);    
    //$(e.imageTarget).css({'width':'auto', 'height':'400px', 'position':'', top:'', 'left':'', 'margin':'auto'});
    $(ele).find('.linktext').linkify();
}

$(document).ready (function() {
    $(document).on('click','.jc img',function(){

        var that = $(this);
        if(!that.hasClass('active')){
            var img_ch = that.attr('rel'); 
            $('.thumb_'+img_ch).addClass('active');
            gallery.slide(img_ch);
        }
    });

    $(document).on('click','.license .slideUp',function(){
        if($(this).hasClass('open')){
            $(this).removeClass('open').addClass('close').removeClass('icon-chevron-down').addClass('icon-chevron-up');
            $('.image_info').css('height','55px');
        }else{
            $(this).addClass('open').removeClass('close').removeClass('icon-chevron-up').addClass('icon-chevron-down');
            $('.image_info').css('height','auto');
        }
    });

    $(document).on('click','.slide .slide-content',function(){
        event.preventDefault(0);
        var a = document.createElement("a");
        a.target = "_blank";
        if($(this).attr('src')) {
            a.href = $(this).attr('src').replace('_gall.jpg','.jpg');
            a.click();  
        }
    });


});

function galleryAjax(url,domainObj){
    $.ajax({
        url: url,
        dataType:'json'
    }).done(function (result) {
        console.log(result);
        console.log(domainObj+'==========================');
        defaultThumb = result.defaultThumb;
        updateGallery1(result.resources,domainObj);
    });
}
