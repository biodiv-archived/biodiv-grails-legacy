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
        <link type="image/x-icon" href="/${grailsApplication.config.speciesPortal.app.siteCode}/images/favicon.ico" rel="shortcut icon"></link>
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
        <asset:script>
            $(document).ready(function() {
                $('.accordion ul').kwicks({max:400, duration: 300, easing: 'easeOutQuad'});
             });
        </asset:script>
        <div id="home" class="observation  span12">
           <div class="about-us"> 
              <h2 style="text-align:center;">Welcome to the Bhutan Biodiversity Portal </h2>
                 <blockquote><em>   
                 <img src="/assets/all/openquote.gif" class="quote" />
                 <strong>The Government shall ensure that, in order to conserve the country’s natural resources and to prevent degradation of the ecosystem, a minimum of sixty percent of Bhutan’s total land shall be maintained under forest cover for all time.
                 </strong><img src="/assets/all/endquote.gif" class="quote" />
                 </em></blockquote> 
                 <a style="text-align:right;"><strong> -- Article 5:3, The Constitution of the Kingdom of Bhutan </strong></a>
                 <br />
                 <br />             
                 <p>Bhutan is a small, landlocked country with an area of 38,394 km<sup>2</sup> situated on the southern slope of the Eastern Himalayas. Straddling the two major Indo-Malayan and Palearctic biogeographic realms, Bhutan is part of the Eastern Himalayan biodiversity hotspot and contains 23 Important Bird Areas (IBA), 8 ecoregions, a number of Important Plant Areas (IPA) and wetlands, including two Ramsar Sites. The diverse ecosystems and eco-floristic zones have made Bhutan home to a wide array of flora and fauna.</p>
                                                                                                                                               <a href="bbp/theportal"> read more &raquo; </a>
                                                                                                                                                           <br />
                                                                                                                                                         </div>  
          <div class="accordion">
              <ul id="thumbsList">
                  <li id="species"><a href="/species"><div class="textoverlay">${Species.countByPercentOfInfoGreaterThan(0)} Species Pages</div></a></li>
                  <li id="observations"><a href="/observation"><div class="textoverlay">${Observation.countObservations()} Observations</div></a></li>
                  <li id="maps"><a href="/map"><div class="textoverlay">24 Map layers</div></a></li>
                  <li id="documents"><a href="/document/list"><div class="textoverlay">${Document.count()} Documents</div></a></li>
                  <li id="groups"><a href="/group/list"><div class="textoverlay">${UserGroup.count()} User Groups</div></a></li>
                  <li id="about-us"><a href="/bbp/aboutus"><div class="textoverlay">${ActivityFeed.count()} User Activities</div></a></li>
               </ul>          
          </div>
           <div class="navblock" style="margin-top:20px;">
                <b><big>&nbsp;<a name="latestObservations"><g:message code="index.button.latest.observations" /></a></big></b>
                <div class="sidebar_section" style="margin: 5px; overflow: hidden; background-color: white;">
                    <div class="jcarousel-skin-ie7" data-contextfreeurl="/observation/show&quot;" data-url="/biodiv-api/naksha/search/observation/observation?sort=lastrevised&max=11" id="carousel_latestUpdatedObservations" style="clear: both; width: 880px; margin-top: 23px;">
                        <ul style="list-style: none; width: 880px; margin-left: 0px;">
                        </ul>
                        <div class="observation_links" style="margin-top: 5px; margin-bottom: 3px;">
                            <a class="btn btn-mini" href="/observation/list?sort=lastRevised"><g:message code="button.show.all" /></a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <asset:script>
        $(document).ready(function() {
            relatedStory_latestObvs([], "latestUpdatedObservations", "latestUpdatedObservations", "", "");
        });
        </asset:script>
    </body>
</html>
