var carouselLinks =[],gallery ;

function youtube_parser(url){
    var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
    var match = url.match(regExp);
    return (match&&match[7].length==11)? match[7] : false;
}

function updateGallery1(resources){
    if(resources.length == 0){
        console.log('Error: Resources is Empty');
        return false;
    }        
    initializeGallery(resources);        
    //For Slider Content
    update_imageAttribute(resources,$('.image_info'));
    rate($('.star_gallery_rating'));      
    $('.imageAttr:first').show();
    $('.mover img').css('opacity','initial');
}
function initializeGallery(resources){
    console.log(resources.length);
    $.each(resources, function (index, photo) {
        // Adding Thumbnail
       $('.mover').append('<img class="thumb thumb_'+index+'" rel="'+index+'" src="'+photo.icon+'" />')     
        
        // For Slider 
        index = index+1;
        if(photo.type == 'Image'){
            carouselLinks.push({
                 href: photo.url.replace('.jpg','_gall.jpg'),
                 title: index+'/'+resources.length
            });
        }else if(photo.type == 'Video'){
            carouselLinks.push({
                 youtube: youtube_parser(photo.url),
                 type: 'text/html',
                 title: index+'/'+resources.length
            });
        }
   });
    console.log(carouselLinks);
    // Initialize the Gallery as image carousel:
    gallery = blueimp.Gallery(carouselLinks, {
        container: '#blueimp-image-carousel',
            carousel: true, 
            onopen: function () {
                // Callback function executed when the Gallery is initialized.
                $('#gallerySpinner').hide();
                $('.gallery_wrapper').show();
            },           
            onslideend: function (index, slide) {
                // Callback function executed after the slide change transition.
                $('.thumb').css('opacity','0.6');
                $('.thumb_'+index).css('opacity','initial');
                if($('.imageAttr_'+index).hasClass('open')){
                    $('.image_info').css('height','auto');
                }
                $('.imageAttr').hide();
                $('.imageAttr_'+index).show();
                $('.thumb').removeClass('active');
                $('.thumb_'+index).addClass('active');
                //$('.image_info').html(index);             
            },          
    });
    carouselInit();
}

function update_imageAttribute(resourceList,ele){
    var output = '';
    $.each(resourceList, function (index, resource) {
        output += '<div class="row-fluid imageAttr imageAttr_'+index+'" style="display:none;">';
        output += ' <div class="span6">';

        if(resource.description){
            output += '<div class="span5 ellipsis multiline" style="margin-left:0px">'+resource.description+'</div>';
            output += '<div style="clear:both;"></div>'
        }


        output += '<div class="conts_wrap">'
        if(resource.contributors.length > 0){
            output += '<h5>Contributors</h5>';
            console.log(resource.contributors);        
            $.each(resource.contributors, function (index, contributor) {     
                output += '<ol>';
                output += '<li>'+contributor.name+'</li>';
                output += '</ol>';
            }); 
        } 

    if(resource.attributors.length > 0){
        output += '<h5>Attributors</h5>';
        console.log(resource.attributors);        
        $.each(resource.attributors, function (index, attributor) {     
            output += '<ol>';
            output += '<li>'+attributor.name+'</li>';
            output += '</ol>';
        }); 
    } 


    if(resource.url){
        output += '<a href="'+resource.url+'" target="_blank"><b>View image source</b> </a>';
    }

    if(resource.annotations){
        // Todo for Annotation
    }
    output += '</div>';

    output += '</div>';
    output += '<div class="span6">';
    output += '<div class="license span12">';
    output += '<a class="span7" href="'+resource.licenses[0]['url']+'" target="_blank">';
    output += '<img class="icon" style="height:auto;margin-right:2px;" src="../../../assets/all/license/'+resource.licenses[0]['name'].replace(' ','_').toLowerCase()+'.png" alt="'+resource.licenses[0]['name']+'">';
    output += '</a>';
    output += '<div class="rating_form span4">';
    output += '<form class="ratingForm" method="get" title="Rate it">';
    output += '<span class="star_gallery_rating pull-right" title="Rate" data-score="'+resource.rating+'" data-input-name="rating" data-id="'+resource.id+'" data-type="resource" data-action="like" >';
    output += '</span>';
    output += '<div class="noOfRatings">'; 
    var ratings ='';
    if(resource.rating != 1){
        ratings = 'ratings';
    }else{
        ratings = 'rating';
    }                     
    output += '(3 '+ratings+')';
            output += '</div>';
            output += '</form>';
            output += '</div>'; 
            output += '<i class="slideUp pull-right open icon-chevron-down" rel="58"></i>';                                       
            output += '</div>';
            output += '</div>';
            output += '</div>';
            });    
    ele.html(output);    

}

function carouselInit(){
    var proc = false;
    var k = 2;
    var mover = $('.mover');
    var run = $('.container1').outerWidth();

    $('.gallery_wrapper #right').on('click',function(){    
        if(proc){return false;}
        proc = true;


        var pos = parseInt(mover.css('left'),10)-run;
        var curr = mover.outerWidth()+pos;

        if(curr>run/k){ 
            mover.animate({left:'-='+run/k+'px'},function(){proc=false;});
            $('.inactive').removeClass('inactive');
        }else{
            mover.animate({left:'-='+curr+'px'},function(){proc=false;});
            $(this).addClass('inactive');
        }

    });
    $('.gallery_wrapper #left').on('click',function(){
        if(proc){return false;}
        proc = true;

        var pos = parseInt(mover.css('left'),10);
        var curr = mover.outerWidth()+pos;

        if(pos < -run/k){
            mover.animate({left:'+='+run/k+'px'},function(){proc=false;});
            $('.inactive').removeClass('inactive');
        }else if(pos <= 0){
            mover.animate({left:'+='+(-pos)+'px'},function(){proc=false;});
            $(this).addClass('inactive');
        }

    });

}

$(document).on('click','.mover img',function(){

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
        $('.image_info').css('height','38px');
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



function galleryAjax(url,domainObj){
    $.ajax({
        url: url,
        data: {
            format: 'json'
        }
    }).done(function (result) {
        console.log(result);
        console.log(domainObj)
        var resources;
    if(domainObj == 'observation'){
        console.log('observation==========================');
        console.log(result.instance.resource);
        updateGallery1(result.instance.resource);
    }else{
        console.log('species==========================');
        updateGallery1(result);
    }
    });
}
