<%@ page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<html>
    <head>
        <g:set var="title" value="${params?.title?:'Map'}"/>
        <g:render template="/common/titleTemplate"/>
        <r:require modules="maps" />
        <style>
            .container {
                width:100%;
            }
        </style>
    </head>
    <body>
        <div>
            <div id="main_wrapper" style="position:relative">

                <div id="parent_panel">
                    <div>
                        <a href="#" id="panel_show_bttn" style="display:none;"></a>
                    </div>
                    <div id="panel">
                        <div id="top_bar">
                            <ul id="top_bar_links">
                                <li><a href="#" id="explore_bttn"><g:message code="link.explore" /> </a></li>
                                <li><a href="#" id="search_bttn"><g:message code="default.search" /></a></li>
                                <!--li><a href="#" id="share_bttn">Share</a></li-->
                                <li><a href="#" id="selected_layers_bttn"><g:message code="link.selected.layers" /></a></li>
                                <li><a href="#" id="selected_features_bttn"><g:message code="link.selected.features" /></a></li>
                            </ul>


                            <a href="#" id="panel_hide_bttn"></a>
                        </div>
                        <div id="feature_info_panel" class="side_panel" style="overflow:auto; display:none;"></div>
                        <div id="layers_list_panel" class="side_panel"></div>
                        <div id="search_panel" class="side_panel" style="display:none;">
                            <div id="search_box"></div>
                            <div id="search_results_panel"></div>
                        </div>
                        <div id="share_panel" class="side_panel" style="display:none;"></div>
                        <div id="selected_layers_panel" class="side_panel" style="display:none;"></div>
                    </div>
                </div>
                <div id="map"></div>
                <div id="footer"></div>
            </div>

        </div>
        <r:script>
            $(document).ready(function() {
            loadGoogleMapsAPI(function() {

                india_baundary_lyr = getWorkspace() + ':lyr_121_india_boundary';
                var mapOptions = {
                    popup_enabled: false,
                    toolbar_enabled: true,
                    baselayers_switcher_enabled: true,
                    feature_info_panel_div: 'feature_info_panel'
                };


                var layerOptions = [
                    {
                    title:"${params.title}",
                    layers:"${params.layers}",
                    styles:"${params.styles}",
                    cql_filter:"${params.cql_filter}",
                    opacity:0.7
                    }
                ]


                var map = showMap('map', mapOptions, layerOptions);
                showLayersExplorer('layers_list_panel', map);

                if (layerOptions[0].layers === ''){
                    loadLayersFromCookie();
                }

                addSearchBox('search_box');
                addSearchResultsPanel('search_results_panel');

                var cache = {},
                lastXhr;
                $( "#map_search_text_field" ).autocomplete({
                    minLength: 2,
                    source: function( request, response ) {
                        var term = request.term;
                        if ( term in cache ) {
                        response( cache[ term ] );
                        return;
                        }

                        var q = "wt=json&q=" + term;

                        $.ajax({
                            url: "/wgp_maps/suggest",
                            data: q,
                            type: 'GET',
                            async: false,
                            cache: false,
                            timeout: 30000,
                            dataType: 'text',
                            error: function(){
                            return true;
                            },
                            success: function(data, msg){
                                d = eval('(' + data + ')');
                                response(get_suggestions(d));
                            }
                        });
                    }
                });
                })
            });
        </r:script>
    </body>

</html>
