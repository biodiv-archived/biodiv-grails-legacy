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
    .view_bootstrap_gallery:hover, .view_bootstrap_gallery:visited, .view_bootstrap_gallery:focus{
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
var checkView = false;
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

    $(document).on('click','.view_bootstrap_gallery',function(){
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

<g:if test="${!activeFilters.isChecklistOnly}">
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

function checkUrl(viewText,changeText){
    var ls = window.location.search;
    ls = ls.slice(1);
    if((!params['view'] || params['view'] == viewText) && !ls){
        var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?view='+changeText;
        window.history.pushState({path:newurl},'',newurl);               
    }else{
        if(ls.split("&").length == 1){
            var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?view='+changeText;
            window.history.pushState({path:newurl},'',newurl);   
        }else{
        var lang_key = "view=";
        var ps = ls.split("&");
        var flag, i;
        if(ps) {
            for(i=0; i<ps.length; i++){
                if(ps[i].indexOf(lang_key) == 0){
                    flag = true;
                    break;
                }
                else{
                    flag = false;
                }
            }

            if(flag){
                ps[i] = lang_key + changeText;
                s = ps.join("&");
            }
            else{
                s += "&" + lang_key + changeText;
            }
        }

        newurl = window.location.href.replace(window.location.search, "?"+s);
        window.history.pushState({path:newurl},'',newurl);

        }
    }
}
$(document).ready(function(){
    $(document).on('click','#obvList',function(){           
            checkUrl("grid","list");
            params['view'] = "list"; 
            checkView = true;           
            $(this).addClass('active');
            $('#obvGrid').removeClass('active');
            addListLayout();
    });

    $(document).on('click','#obvGrid',function(){              
            checkUrl("list","grid");
            params['view'] = "grid"; 
            checkView = false;
            $(this).addClass('active');
            $('#obvList').removeClass('active');
            addGridLayout();
    });

    $(document).on('click','.clickSuggest',function(){  
        $(this).next().toggle('slow');
    });

       $(document).on('submit','.addRecommendation', function(event) {
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
                        $(".addRecommendation_"+data.instance.observation)[0].reset();
                        $("#canName").val("");   
                    } else {
                        showUpdateStatus(data.msg, data.success?'success':'error');
                    }                    
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


<r:script>
$(document).ready(function(){
    $(".selected_group").off('click').on('click',function(){
        $(this).closest(".groups_super_div").find(".group_options").toggle();
        //$(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
    });

    $(document).on('click',".group_option",function(){
        var is_save_btn_exists = $(this).closest(".groups_super_div").parent().parent().find('.save_group_btn');
           if(is_save_btn_exists.length == 1){
                is_save_btn_exists.show();
           }
       

        $(this).closest(".groups_super_div").find(".group").val($(this).val());
        $(this).closest(".groups_super_div").find(".selected_group").html($(this).html());
        $(this).closest(".group_options").hide();
        //$(this).closest(".groups_super_div").find(".selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        if($(this).closest(".groups_super_div").find(".selected_group b").length == 0){
            $('<b class="caret"></b>').insertAfter($(this).closest(".groups_super_div").find(".selected_group .display_value"));
        }
    });

});

</r:script>
<g:if test="${params?.view == 'list'}">
  <script type="text/javascript">
  $(document).ready(function(){     
        checkView = true;   
        $('#obvList').trigger('click');
        $('.obvListwrapper').show();
    });
  </script>
</g:if>
</g:if>
</body>
</html>
