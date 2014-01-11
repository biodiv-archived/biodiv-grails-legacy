<%@page import="species.utils.Utils"%>
<%@page import="species.Species"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.ActivityFeed"%>
<%@page import="species.groups.UserGroup"%>
<%@page import="content.eml.Document"%>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>${grailsApplication.config.speciesPortal.app.siteName}</title>
        <r:require modules="core" />
    </head>

    <body>
        <div id="home" class="observation  span12">
            <center>
                <b>Welcome to ${grailsApplication.config.speciesPortal.app.siteName}</b>
            </center>
            <div class="navblock" style="margin-top:20px;">
                <div id="species_entry" class="entry" onclick="location.href='${createLink(controller:'species', action:'list', absolute:true)}'";></div>
                <div id="observations" class="entry" onclick="location.href='${createLink(controller:'observation', action:'list', absolute:true)}'"></div>
                <div id="explore" class="entry"  onclick="location.href='${createLink(controller:'map', action:'show', absolute:true)}'"></div>

                <div id="documents" class="entry" onclick="location.href='${createLink(controller:'documents', action:'list', absolute:true)}'"></div>
                <div id="groups_entry" class="entry"  onclick="location.href='${createLink(controller:'group', action:'list', absolute:true)}'";></div>
                <div id="dashboard" class="entry" onclick="location.href='${createLink(controller:'chart', action:'show', absolute:true)}'"></div>

            </div>

            <div id="stats" class="navblock">
                <div class="entry">
                    <span class="stats_normal">Number of</span><br><span class="stats_big_bold">SPECIES</span> <span class="stats_big">PAGES</span>
                    <div class="stats_number">${Species.count()}</div>
                </div>
                <div class="entry"><span class="stats_normal">Number of</span><br><span class="stats_big_bold">OBSERVATIONS</span>
                    <div class="stats_number">${Observation.countByIsDeleted(false)}</div>
                </div>

                <div class="entry">
                    <span class="stats_normal">Number of</span><br><span class="stats_big">MAP</span> <span class="stats_big_bold">LAYERS</span>
                    <div class="stats_number">0</div>
                </div>
                <div class="entry">
                    <span class="stats_normal">Number of</span><br><span class="stats_big_bold">Documents</span>
                    <div class="stats_number">${Document.count()}</div>
                </div>
 
                <div class="entry">
                    <span class="stats_normal">Number of</span><br><span class="stats_big_bold">GROUPS</span>
                    <div class="stats_number">${UserGroup.count()}</div>
                </div>
                <div class="entry">
                    <span class="stats_normal">Number of</span><br><span class="stats_big_bold">ACTIVITY</span>
                    <div class="stats_number">${ActivityFeed.count()}</div>
                </div>

                        </div>
        </div>
    </body>
</html>
