<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'showusergroupsig.title.observations')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
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
    /*.groups_super_div{
        margin-top: -15px;
        margin-right: 10px;
    }
    .groups_div > .dropdown-toggle{
          height: 25px;
    }*/
    .group_options, .group_option{
          min-width: 110px;
    }
    /*.save_group_btn{
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
    }*/    

    .view_bootstrap_gallery{
          margin-top: -20px;
          position: absolute;
          color: white;
          font-weight: bold;
          padding: 0px 25px;
          text-decoration: none;
          text-shadow: #000 0px 0px 2px;
    }
    .view_bootstrap_gallery:hover, .view_bootstrap_gallery:visited, .view_bootstrap_gallery:focus{
        color: white;
        text-decoration: none;
    }
    .addmargin{
        margin:10px 0px !important;
        border: 1px solid #a6dfc8 !important;
    }
    .showObvDetails{
      height: 180px;
      position: relative;
      margin-left: 160px;
    }
    .snippettablet{
        padding: 5px;
    }
    .signature .snippettablet{
         padding: 0px;
     }
    .recoName, #recoComment{
        width:419px !important;
    }
    /*.reco_block{
         margin-bottom:0px !important;
     }*/
     .resource_in_groups{
         background-color: #ccc;
         /*margin-top: -10px;
         margin-bottom: 5px !important;
         padding: 8px 0px;
         margin-top: 5px;*/
     }
     /*.clickSuggest{
          margin: 0% 39%;         
          margin-top: -22px;
          padding: 2px 0px 0px 5px;
          background-color: #a6dfc8;
          text-decoration: none;
     }
     .clickSuggest i{
        margin-left:3px;
     }*/
     .resource_in_groups .tile{
         margin-top:0px;
      }

    .observation .prop .value {
      margin-left: 90px;
    }
    .bottom_user_fixed{
     /* clear: inherit;*/
      position: absolute;
      bottom: 20px;
      width: 100%;
      overflow:visible;
    }

.users{float:none; }
.btnagree{float:right;}
.reco_block{line-height:18px;margin:2px 0px !important;}
     
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

            <obv:featured model="['controller':params.controller, 'action':'related', 'filterProperty': 'featureBy', 'filterPropertyValue':true , 'id':'featureBy', 'userGroupInstance':userGroupInstance, 'userLanguage' : userLanguage]" />

            <obv:showObservationsListWrapper />

	</div>


<%-- For AddReco Component --%>

<div id="addRecommendation_wrap" style="display:none;">
 <form id="addRecommendation" name="addRecommendation"
    action="${uGroup.createLink(controller:'observation', action:'addRecommendationVote')}"
    method="GET" class="form-horizontal addRecommendation ">
    <div class="reco-input">
        <reco:create/>
        <input type="hidden" name='obvId'
        value="" />

        <input type="submit"
        value="${g.message(code:'title.value.add')}" class="btn btn-primary btn-small pull-right" style="position: relative; border-radius:4px;  right: -9px;" />
    </div>
    
</form>
</div>


<div id="links" class="links12" style="display:none;"></div>
<div id="blueimp-gallery" class="blueimp-gallery blueimp-gallery-controls">
    <div class="slides"></div>
    <h3 class="title"></h3>
    <a class="prev">‹</a>
    <a class="next">›</a>
    <a class="close">×</a>
    <a class="play-pause"></a>
    <ol class="indicator"></ol>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        window.params.observation.getRecommendationVotesURL = "${uGroup.createLink(controller:'observation', action:'getRecommendationVotes', userGroupWebaddress:params.webaddress) }";
        window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}";
        //initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
    });
</script>

<g:if test="${!activeFilters.isChecklistOnly}">
<g:if test="${!params?.view || params?.view != 'grid'}">
  <asset:script type="text/javascript">
  $(document).ready(function(){     
        checkView = true;   
        $('#obvList').trigger('click');
        $('.obvListwrapper').show();
    });
  </asset:script>
</g:if>
</g:if>
</body>
</html>
