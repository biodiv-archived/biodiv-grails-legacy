<html>
    <head>
        <g:set var="title" value="Search Results"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="search" />
        <style>
            .thumbnails>li {
            margin:2px;
            }
            .map_wrapper {
            margin-bottom: 0px;
            }
        </style>

    </head>
    <body>

        <div class="span12">
            <search:searchResultsHeading />
            <g:if test="${instanceTotal}">
            <% objectTypes.push(['name':'All', 'count':'-'])%>
            <g:render template="/search/sidebar" model="[modules:objectTypes, sGroups:sGroups, tags:tags, contributors:contributors]"/>
            <div class="searchResults span8" style="margin-left: 0px;">
                <g:render template="/search/showSearchResultsListTemplate"/>
            </div>
            </g:if>
        </div>
    </div>
</body>
</html>
