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
                border-radius: 0px 0px 7px 7px;
            }
            #home #species_entry {
                background-image: url('../images/species.png');
            }
            #home #IDAOTool {
                background-image: url('../images/IDAO.png');
            }
            #home #explore {
                background-image: url('../images/maps.png');
            }
            #home #documents {
                background-image: url('../images/documents.png');
            }
            #home #checklists {
                background-image: url('../images/checklists.png');
            }

            #home #observations {
                background-image: url('../images/observations.png');
            }
            #home #groups_entry {
                background-image: url('../images/groups.png');
            }
            #home #dashboard {
                background-image: url('../images/dashboard.png');
            }  
        </style>
    </head>

    <body>
        <div id="home" class="observation  span12">
            <div class="navblock" style="margin-top:20px;background-color:white;padding:10px;">
                <h2 style="text-align:center;color: #db7421;font-size: 1.5em;margin: 0;"><g:message code="index.welcome" /> <g:message code="app.siteName" />  </h2>
                <p style="line-height:1.5">${g.message(code:'app.homepageDescription')}
                
                </p>
            </div>
        
            <div class="navblock" style="margin-top:20px;">

                <div id="species_entry" class="entry" onclick="location.href='${uGroup.createLink(controller:'species', action:'list', absolute:true)}'";><span class="content">${g.message(code:'default.species.label')}</span></div>
                <div id="observations" class="entry" onclick="location.href='${uGroup.createLink(controller:'observation', action:'list', absolute:true)}'"><span class="content">${g.message(code:'default.observation.label')}</span></div>
                <div id="explore" class="entry"  onclick="location.href='${uGroup.createLink(controller:'map', action:'show', absolute:true)}'"><span class="content">${g.message(code:'button.maps')}</span></div>

                <div id="documents" class="entry" onclick="location.href='${uGroup.createLink(controller:'document', action:'list', absolute:true)}'"><span class="content">${g.message(code:'button.documents')}</span></div>

                <div id="checklists" class="entry" onclick="location.href='${uGroup.createLink(controller:'checklist', action:'list', absolute:true)}'"><span class="content">${g.message(code:'title.checklists')}</span></div>
                <a href="http://www.wikwio.org/idao" target="_blank">   <div id="IDAOTool" class="entry"><span class="content">${g.message(code:'idao.tool')}</span></div></a>                
            </div>

        

            <div id="stats" class="navblock" style="margin-top:-20px">
                <div class="entry">
				${g.message(code:'title.number.species')}
                    <div class="stats_number" >${Species.countByPercentOfInfoGreaterThan(0)}</div>
                </div>
                <div class="entry">
                    ${g.message(code:'title.number.observations')}
                    <div class="stats_number" >${Observation.countObservations()}</div>
                </div>

                <div class="entry">
		    ${g.message(code:'title.number.maps')}
                    <div class="stats_number" >1</div>
                </div>
                <div class="entry">
 		    ${g.message(code:'title.number.documents')}
                    <div class="stats_number" >${Document.count()}</div>
                </div>

            <div class="entry">
                ${g.message(code:'title.number.checklist')}
                <div class="stats_number">${Observation.countChecklists()}</div>
            </div>
            <div class="entry">
                ${g.message(code:'title.species.id')}
                <div class="stats_number">345</div>
            </div>
        </div>

        <!--div class="navblock" style="margin-top:20px;">
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
        </div>--> 
        </div>
        <div class="navblock" style="margin-top:20px;margin-left: 25px;">
                <b><big>&nbsp;<a name="partners"><g:message code="link.partners" /></a></big></b>
                <div class="partners">
                <!--table border="0" width="940" align="center">
                             <tbody><tr>
      <td align="center">
                            <a href="http://www.cirad.fr"><img class="img-polaroid" src="/assets/all/partnerlogo/Cirad_sans_texte.jpg"></a>
                            <a href="http://www.ifpindia.org"><img class="img-polaroid" src="/assets/all/partnerlogo/IFP-anglais_logo.png"></a>
                            <a href="http://www.fofifa.mg"><img class="img-polaroid" src="/assets/all/partnerlogo/FOFIFA_logo.png"></a>
                            <a href="http://www.msiri.mu/"><img class="img-polaroid" src="/assets/all/partnerlogo/MCIA-LOGO1.png"></a>
                            <a href="http://www.msiri.mu/"><img class="img-polaroid" src="/assets/all/partnerlogo/MSIRI-2_logo.png"></a>
                            <a href="http://www.cndrs-comores.org"><img class="img-polaroid" src="/assets/all/partnerlogo/CNDRS2_logo.png"></a>
                            </td>
      </tr>
      <tr>
                             <td align="center">
                             <a href="http://www.europa.eu/index_fr.htm"><img class="img-polaroid" src="/assets/all/partnerlogo/UE-TC_logo.png"></a>
                            <a href="http://www.acp-st.eu/"><img class="img-polaroid" src="/assets/all/partnerlogo/acp-st_logo.png"></a>
                            <a href="http://www.acp.int/"><img class="img-polaroid" src="/assets/all/partnerlogo/ACP.jpg"></a>
                            
                              </td>
      </tr>
      </tbody></table!-->
                             <a href="http://www.cirad.fr"><img class="img-polaroid" src="/assets/all/partnerlogo/Cirad_sans_texte.jpg"></a>
                            <a href="http://www.ifpindia.org"><img class="img-polaroid" src="/assets/all/partnerlogo/IFP-anglais_logo.png"></a>
                            <a href="http://www.fofifa.mg"><img class="img-polaroid" src="/assets/all/partnerlogo/FOFIFA_logo.png"></a>
                            <a href="http://www.msiri.mu/"><img class="img-polaroid" src="/assets/all/partnerlogo/MCIA-LOGO1.png"></a>
                            <a href="http://www.msiri.mu/"><img class="img-polaroid" src="/assets/all/partnerlogo/MSIRI-2_logo.png"></a>
                            <a href="http://www.cndrs-comores.org"><img class="img-polaroid" src="/assets/all/partnerlogo/CNDRS2_logo.png"></a>
                            <a href="http://www.europa.eu/index_fr.htm"><img class="img-polaroid" src="/assets/all/partnerlogo/UE-TC_logo.png"></a>
                            <a href="http://www.acp-st.eu/"><img class="img-polaroid" src="/assets/all/partnerlogo/acp-st_logo.png"></a>
                            <a href="http://www.acp.int/"><img class="img-polaroid" src="/assets/all/partnerlogo/ACP.jpg"></a>
                            

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
