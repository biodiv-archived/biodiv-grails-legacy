<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.search.heading')}"/>
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

            <g:set var="modules"  value="[All:[name:'All',displayName:g.message(code:'default.all.label') ], Species:[name:'Species', template:'species',displayName:g.message(code:'default.species.label')], Observation:[name:'Observation', template:'observation',displayName:g.message(code:'observation.label')], Document:[name:'Document', template:'document',displayName:g.message(code:'feature.part.document')], SUser:[name:'SUser', template:'SUser',displayName:g.message(code:'search.suser')], UserGroup:[name:'UserGroup', template:'userGroup',displayName:g.message(code:'userGroup.label')], Resource:[name:'Resource', template:'resource',displayName:g.message(code:'resource.label')], Checklists:[name:'Checklists', template:'checklists',displayName:g.message(code:'checklists.label')]]"/>

            <g:render template="/search/sidebar" model="[modules:modules, objectTypes:objectTypes, sGroups:sGroups, tags:tags, contributors:contributors]"/>
            <div class="searchResults span8" style="margin-left: 0px;">
                <g:render template="/search/showSearchResultsListTemplate"/>
            </div>
            </g:if>
        </div>
    </div>
</body>
</html>
