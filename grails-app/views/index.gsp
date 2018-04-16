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
				<div id="explore" class="entry"  onclick="location.href='/map'"><span class="content">${g.message(code:'button.maps')}</span></div>

                <div id="documents" class="entry" onclick="location.href='${uGroup.createLink(controller:'document', action:'list', absolute:true)}'"><span class="content">${g.message(code:'button.documents')}</span></div>
                <div id="groups_entry" class="entry"  onclick="location.href='${uGroup.createLink(controller:'group', action:'list', absolute:true)}'";><span class="content">${g.message(code:'default.groups.label')}</span></div>
                <div id="dashboard" class="entry" onclick="location.href='${uGroup.createLink(controller:'discussion', action:'list', absolute:true)}'"><span class="content">${g.message(code:'button.discussions')}</span></div>

            </div>

            <div id="stats" class="navblock" style="margin-top:-20px">
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.species')}">${Species.countSpecies()}</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.observations')}">${Observation.countObservations()}</div>
                </div>

                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.maps')}">206</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.documents')}">${Document.countDocuments()}</div>
                </div>
 
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.groups')}">${UserGroup.countUserGroups()}</div>
                </div>
                <div class="entry">
                    <div class="stats_number" title="${g.message(code:'title.number.discussions')}">${Discussion.countDiscussions()}</div>
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

            <div class="navblock" style="margin-top:20px;">
                <b><big>&nbsp;<a name="partners"><g:message code="link.partners" /></a></big></b>
                <div class="partners">
<%--                            <a href="http://www.altlawforum.org/"><img class="img-polaroid" src="/sites/default/files/alf_standard.png"></a>--%>
                            <a><img class="img-polaroid" src="/sites/default/files/abct_standard.png"></a>
                            <a href="http://www.atree.org/"><img class="img-polaroid" src="/sites/default/files/atree.png"></a>
                            <a href="http://www.azimpremjiuniversity.edu.in/"><img class="img-polaroid" src="/sites/default/files/apu_standard.png"></a>
                            <a href="http://bnhs.org/bnhs/"><img class="img-polaroid" src="/sites/default/files/bnhsmod.png"></a>
                            <a href="http://careearthtrust.org/"><img class="img-polaroid" src="/sites/default/files/careearthmod.png"></a>
                            <a href="http://www.feralindia.org/"><img class="img-polaroid" src="/sites/default/files/feral_standard.png"></a>
                            <a href="http://fes.org.in//"><img class="img-polaroid" src="/sites/default/files/fes.png"></a>
                            <a href="http://www.frlht.org/"><img class="img-polaroid" src="/sites/default/files/frlht_standardmod.png"></a>
                            <a href="http://ihstuniversity.org/"><img class="img-polaroid" src="/sites/default/files/frlht_standard.png"></a>
                            <a href="http://www.hornbillfoundation.org/"><img class="img-polaroid" src="/sites/default/files/wghf_standard.png"></a>
                            <a href="http://www.ifpindia.org/"><img class="img-polaroid" src="/sites/default/files/ifp_logo.png"></a>
                            <a href="http://www.ifoundbutterflies.org/"><img class="img-polaroid" src="/sites/default/files/ifoundbutterflies_standard.png"></a>
                            <a href="http://keystone-foundation.org/"><img class="img-polaroid" src="/sites/default/files/keystone_standard.png"></a>
                            <a href="http://www.madrascrocodilebank.org/"><img class="img-polaroid" src="/sites/default/files/mcbt_standard.png"></a>
                            <a href="http://www.ncbs.res.in/"><img class="img-polaroid" src="/sites/default/files/ncbs_standard.png"></a>
                            <a href="http://osgeo.in/"><img class="img-polaroid" src="/sites/default/files/osgeo_standard.png"></a>
                            <a href="http://sacon.in/"><img class="img-polaroid" src="/sites/default/files/sacon_standard.png"></a>
                            <a href="http://strandls.com/"><img class="img-polaroid" src="/sites/default/files/strand.png"></a>
                            <a href="http://www.wwfindia.org/"><img class="img-polaroid" src="/sites/default/files/wwf_standard.png"></a>
                            <a href="http://zooreach.org/"><img class="img-polaroid" src="/sites/default/files/zoo_standard.png"></a>

            </div>
            </div>


        </div>
        <asset:script>
        $(document).ready(function() {
            relatedStory([], "latestUpdatedObservations", "latestUpdatedObservations", "", "");
        });
        </asset:script>
    </body>
</html>
