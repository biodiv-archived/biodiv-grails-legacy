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
        <style>
            #home .entry {
                border-radius: 7px 7px 0px 0px;
            }
            #stats .entry {
                height: 40px;
                padding: 20px 0px 0px 0px;
                border-radius: 0px 0px 7px 7px;
            }
        </style>
    </head>

    <body>
        <div id="home" class="observation  span12">

            <div class="navblock" style="margin-top:20px;">
                <h2 style="text-align:center;color: #db7421;font-size: 1.5em;margin: 0;">Welcome to ${grailsApplication.config.speciesPortal.app.siteName}</h2>
                <p style="line-height:1.5">${grailsApplication.config.speciesPortal.app.homepageDescription}
                <a href="about">More ...</a>
                </p>
            </div>
        
            <div class="navblock" style="margin-top:20px;">
                <div id="species_entry" class="entry" onclick="location.href='${createLink(controller:'species', action:'list', absolute:true)}'";></div>
                <div id="observations" class="entry" onclick="location.href='${createLink(controller:'observation', action:'list', absolute:true)}'"></div>
                <div id="explore" class="entry"  onclick="location.href='${createLink(controller:'map', action:'show', absolute:true)}'"></div>

                <div id="documents" class="entry" onclick="location.href='${createLink(controller:'documents', action:'list', absolute:true)}'"></div>
                <div id="groups_entry" class="entry"  onclick="location.href='${createLink(controller:'group', action:'list', absolute:true)}'";></div>
                <div id="dashboard" class="entry" onclick="location.href='${createLink(controller:'chart', action:'show', absolute:true)}'"></div>

            </div>

            <div id="stats" class="navblock" style="margin-top:-20px">
                <div class="entry">
                    <div class="stats_number" title="Number of Species">${Species.count()}</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="Number of Observations">${Observation.countObservations()}</div>
                </div>

                <div class="entry">
                    <div class="stats_number" title="Number of Maps">0</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="Number of Documents">${Document.count()}</div>
                </div>
 
                <div class="entry">
                    <div class="stats_number" title="Number of Groups">${UserGroup.count()}</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="Number of Activity">${ActivityFeed.count()}</div>
                </div>

            </div>

            <div class="navblock" style="margin-top:20px;">
            <obv:featured 
                model="['controller':'featured', 'action':'related', 'filterProperty': 'featureBy', 'filterPropertyValue':true , 'id':'featureBy', 'userGroupInstance':userGroupInstance, hideShowAll:true]" />
            </div>

        </div>
    </body>
</html>
