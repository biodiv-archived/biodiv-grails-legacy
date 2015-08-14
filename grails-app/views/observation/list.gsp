<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'showusergroupsig.title.observations')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_list" />
<style>
    
    .map_wrapper {
        margin-bottom: 0px;
    }

    /*.ellipsis {
        white-space:inherit;
    }*/

    li.group_option{
        height:30px;
    }
    li.group_option span{
        padding: 0px;
        float: left;
    }
    .groups_super_div{
        margin-top: -15px;
        margin-right: 10px;
    }
    .groups_div > .dropdown-toggle{
          height: 25px;
    }
    .group_options, .group_option{
          min-width: 110px;
    }
    .save_group_btn{
        float: right;
        margin-right: 11px;
          margin-top: -9px;
    }
    .group_icon_show_wrap{
        border: 1px solid #ccc;
        float: right;
        height: 33px;
        margin-right: 4px;
    }
    .edit_group_btn{
        top: -10px;
        position: relative;
        margin-right: 12px;
    }
    .propagateGrpHab{
        display:none;
        float: right;
        margin-top: -5px;
    }

</style>
<style type="text/css">
.commonName{
    width: 79% !important;
}
.group_icon_show_wrap {
  border: 1px solid #ccc;
  float: right;
  height: 33px;
  margin-right: 4px;
}
.edit_group_btn {
  top: -10px;
  position: relative;
  margin-right: 12px;
}

    li.group_option{
        height:30px;
    }
    li.group_option span{
        padding: 0px;
        float: left;
    }
    .groups_super_div{
        margin-top: -15px;
        margin-right: 10px;
    }
    .groups_div > .dropdown-toggle{
          height: 25px;
    }
    .group_options, .group_option{
          min-width: 110px;
    }
    .save_group_btn{
        float: right;
        margin-right: 11px;
          margin-top: -9px;
    }
    .group_icon_show_wrap{
        border: 1px solid #ccc;
        float: right;
        height: 33px;
        margin-right: 4px;
    }
    .edit_group_btn{
        top: -10px;
        position: relative;
        margin-right: 12px;
    }
    .propagateGrpHab{
        display:none !important;
        float: right;
        margin-top: -5px;
    }
    .view_bootstrap_gallery{
          margin-top: -20px;
          position: absolute;
          color: white;
          font-weight: bold;
          padding: 0px 33px;
          text-decoration: none;
    }
    .view_bootstrap_gallery:hover{
        color: white;
        text-decoration: none;
    }
</style>
</head>
<body>


	<div class="span12">
           <obv:showSubmenuTemplate/>

            <div class="page-header clearfix">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px;">

                        <h1><g:message code="default.observation.label" /></h1>

                    </div>
                </div>
                <div style="clear:both;"></div>
            </div>



            <uGroup:rightSidebar/>
            <obv:featured 
            model="['controller':params.controller, 'action':'related', 'filterProperty': 'featureBy', 'filterPropertyValue':true , 'id':'featureBy', 'userGroupInstance':userGroupInstance, 'userLanguage' : userLanguage]" />

<%--            <h4><g:message code="heading.browse.observations" /></h4>--%>
            <obv:showObservationsListWrapper />
	</div>


    <div id="links" class="links12" style="display:none;"></div>
<div id="blueimp-gallery" class="blueimp-gallery">
    <div class="slides"></div>
    <h3 class="title"></h3>
    <a class="prev">‹</a>
    <a class="next">›</a>
    <a class="close">×</a>
    <a class="play-pause"></a>
    <ol class="indicator"></ol>
</div>
 



 <script type="text/javascript">

 function appendGallery(ovbId,images){
        $("#links").removeClass();
        $("#links").addClass('links'+ovbId);
        var carouselLinks = [],
        linksContainer = $('.links'+ovbId),
        baseUrl,
        thumbUrl;
        $.each(images, function (index, photo) {
            console.log("photo ="+photo);
            baseUrl = "http://indiabiodiversity.org/biodiv/observations/"+photo;
            //thumbUrl = "http://indiabiodiversity.org/biodiv/observations/"+folderpath+"/"+photo+"_th1.jpg";
            //console.log(thumbUrl);
            $('<a/>')
                .append($('<img>').prop('src', baseUrl))
                .prop('href', baseUrl)                
                .attr('data-gallery', '')
                .appendTo(linksContainer);
            console.log(carouselLinks);
            carouselLinks.push({
                href: baseUrl              
            });
        }); 

        $('.links'+ovbId+' a:first').trigger('click');
        

    }

$(document).ready(function(){   

    $('.view_bootstrap_gallery').click(function(){
            // Load demo images from flickr:    
    var ovbId       = $(this).attr('rel');
    var images  = $(this).attr('data-img').split(",");
    $('#links').empty();
   //console.log(images);
   // return false;
    appendGallery(ovbId,images);           

    });
});

 </script>

	<script type="text/javascript">
		$(document).ready(function() {
            window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'observation', action:'getRecommendationVotes', userGroupWebaddress:params.webaddress) }";
			window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}";
                        initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
                });
	</script>
</body>
</html>
