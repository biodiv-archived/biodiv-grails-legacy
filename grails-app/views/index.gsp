<%@page import="species.utils.Utils"%>
<%@page import="species.Species"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.Discussion"%>
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
            <div class="navblock" style="margin-top:20px;background-color:white;padding:10px;">
                <h2 style="text-align:center;color: #db7421;font-size: 1.5em;margin: 0;"><g:message code="index.welcome" /> ${grailsApplication.config.speciesPortal.app.siteName}</h2>
                <p style="line-height:1.5">${grailsApplication.config.speciesPortal.app.homepageDescription}
                <a href="about"><g:message code="link.more" /></a>
                </p>
            </div>
        
            <div class="navblock" style="margin-top:20px;">
                <div id="species_entry" class="entry" onclick="location.href='${uGroup.createLink(controller:'species', action:'list', absolute:true)}'";><span class="content">${g.message(code:'default.species.label')}</span></div>
                <div id="observations" class="entry" onclick="location.href='${uGroup.createLink(controller:'observation', action:'list', absolute:true)}'"><span class="content">${g.message(code:'species.observation')}</span></div>
                <div id="explore" class="entry"  onclick="location.href='${uGroup.createLink(controller:'map', action:'show', absolute:true)}'"><span class="content">${g.message(code:'button.maps')}</span></div>

                <div id="documents" class="entry" onclick="location.href='${uGroup.createLink(controller:'document', action:'list', absolute:true)}'"><span class="content">${g.message(code:'button.documents')}</span></div>
                <div id="groups_entry" class="entry"  onclick="location.href='${uGroup.createLink(controller:'group', action:'list', absolute:true)}'";><span class="content">${g.message(code:'default.groups.label')}</span></div>
                <div id="dashboard" class="entry" onclick="location.href='${uGroup.createLink(controller:'discussion', action:'list', absolute:true)}'"><span class="content">${g.message(code:'button.discussions')}</span></div>

            </div>

            <div id="stats" class="navblock" style="margin-top:-20px">
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.species')}">${Species.countByPercentOfInfoGreaterThan(0)}</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.observations')}">${Observation.countObservations()}</div>
                </div>

                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.maps')}">206</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.documents')}">${Document.count()}</div>
                </div>
 
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.groups')}">${UserGroup.count()}</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.discussions')}">${Discussion.count()}</div>
                </div>

            </div>

            <div class="navblock" style="margin-top:20px;">
                <b><big>&nbsp;<a name="latestObservations"><g:message code="index.button.latest.observations" /></a></big></b>
                <div class="sidebar_section" style="margin: 5px; overflow: hidden; background-color: white;">
                    <div class="jcarousel-skin-ie7" data-contextfreeurl="/observation/show&quot;" data-url="/observation/related" id="carousel_latestUpdatedObservations" style="clear: both; width: 880px; margin-top: 23px;">
                        <ul style="list-style: none; width: 880px; margin-left: 0px;">
                        </ul>
                        <div class="observation_links" style="margin-top: 5px; margin-bottom: 3px;">
                            <a class="btn btn-mini" href="/observation/list?sort=lastRevised"><g:message code="button.show.all" /></a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <r:script>
        $(document).ready(function() {
            relatedStory([], "latestUpdatedObservations", "latestUpdatedObservations", "", "");
        });
        </r:script>
    </body>
</html>
