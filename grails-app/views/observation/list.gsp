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
    .commonName{
        width: 79% !important;
    }    

    .view_bootstrap_gallery{
          margin-top: -20px;
          position: absolute;
          color: white;
          font-weight: bold;
          padding: 0px 33px;
          text-decoration: none;
    }
    .view_bootstrap_gallery:hover, .view_bootstrap_gallery:visited{
        color: white;
        text-decoration: none;
    }
    .addmargin{
        margin:10px 0px !important;
        border: 3px solid #a6dfc8 !important;
    }
    .showObvDetails{
        padding: 10px 0px;
    }
    .snippettablet{
        padding: 5px;
    }
    .recoName, #recoComment{
        width:419px !important;
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
            baseUrl = "${grailsApplication.config.speciesPortal.observations.serverURL}"+photo;
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


<script type="text/javascript">
function loadSpeciesnameReco(){
    $('.showObvDetails').each(function(){
        var observationId = $(this).attr('rel');
        $(".recoSummary_"+observationId).html('<li style="text-align: center;"><img src="/biodiv/images/spinner.gif" /></li>')
        preLoadRecos(3, 0, false,observationId);
    });
}
function addListLayout(){
    $('.thumbnails>li').css({'width':'100%'}).addClass('addmargin');
    $('.snippet.tablet').addClass('snippettablet');
    $('.prop').css('clear','inherit');
    $('.showObvDetails, .view_bootstrap_gallery').show();
    $('.species_title_wrapper').hide();
    loadSpeciesnameReco();
    initializeLanguage();

}

function addGridLayout(){
    $('.thumbnails>li').css({'width':'inherit'}).removeClass('addmargin');
    $('.snippet.tablet').removeClass('snippettablet');
    $('.prop').css('clear','both');
    $('.species_title_wrapper').show();
    $('.showObvDetails, .view_bootstrap_gallery').hide();
}
$(document).ready(function(){
    $(document).on('click','#obvList',function(){
            checkView = true;
            $(this).addClass('active');
            $('#obvGrid').removeClass('active');
            addListLayout();
    });

    $(document).on('click','#obvGrid',function(){
            checkView = false;
            $(this).addClass('active');
            $('#obvList').removeClass('active');
            addGridLayout();
    });

    $(document).on('click','.clickSuggest',function(){  
        $(this).next().toggle('slow');
    });

       $('.addRecommendation').bind('submit', function(event) {
            var that = $(this);
            $(this).ajaxSubmit({ 
                url:"${uGroup.createLink(controller:'observation', action:'addRecommendationVote')}",
                dataType: 'json', 
                type: 'GET',
                beforeSubmit: function(formData, jqForm, options) {
                    console.log(formData);
                    updateCommonNameLanguage();
                    return true;
                }, 
                success: function(data, statusText, xhr, form) {
                    if(data.status == 'success' || data.success == true) {
                        console.log(data);
                        if(data.canMakeSpeciesCall === 'false'){
                            $('#selectedGroupList').modal('show');
                        } else{
                            preLoadRecos(3, 0, false,data.instance.observation);
                            setFollowButton();
                            showUpdateStatus(data.msg, data.success?'success':'error');
                        }
                    } else {
                        showUpdateStatus(data.msg, data.success?'success':'error');
                    }
                    $(".addRecommendation_"+data.instance.observation)[0].reset();
                    $("#canName").val("");
                    return false;
                },
                error:function (xhr, ajaxOptions, thrownError){
                    //successHandler is used when ajax login succedes
                    var successHandler = this.success, errorHandler = showUpdateStatus;
                    handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
                } 
            });
            event.preventDefault();
        });

});
</script>
</body>
</html>
