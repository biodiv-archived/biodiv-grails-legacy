<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<html>
<head>
<g:set var="title" value="Observations"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_list" />
<style>
    
    .map_wrapper {
        margin-bottom: 0px;
    }

    .ellipsis {
        white-space:inherit;
    }

    #carousel_featureBy .snippet {
        height:150px;
    }

    .list_view .observation_story {
        width:470px;
    }

    #carousel_featureBy .observation_story {
        width:780px;
    }
</style>
</head>
<body>

	<div class="span12">
            <obv:showSubmenuTemplate/>

            <div class="page-header clearfix">
                <div style="width:100%;">
                    <div class="main_heading" style="margin-left:0px;">

                        <h1>Observations</h1>

                    </div>
                </div>
                <div style="clear:both;"></div>
            </div>



            <uGroup:rightSidebar/>
            <obv:featured 
            model="['controller':params.controller, 'action':'related', 'filterProperty': 'featureBy', 'id':'featureBy', 'userGroupInstance':userGroupInstance]" />

            <h5 style="margin-top:40px;">Browse Observations</h5>
            <obv:showObservationsListWrapper />
	</div>

	<g:javascript>
		$(document).ready(function() {
			window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}";
			initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
		});
	</g:javascript>
</body>
</html>
