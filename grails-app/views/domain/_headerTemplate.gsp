<%@page import="species.utils.Utils"%>

<div class="container group-theme navbar" style="width:100%;margin-bottom:0px;">
	<div>
		<g:if test="${userGroupInstance  && userGroupInstance.id }">
			<uGroup:showHeader model="[ 'userGroupInstance':userGroupInstance]" />
		</g:if>
		<g:else>
			<a class="pull-left" href="${createLink(url:grailsApplication.config.grails.serverURL+"/..") }" style="margin-left: 0px;"> <img
                            class="logo" src="${Utils.getIBPServerDomain()+'/'+grailsApplication.config.speciesPortal.app.logo}"
                            title="${grailsApplication.config.speciesPortal.app.siteName}" alt="${grailsApplication.config.speciesPortal.app.siteName}">
			</a>
			<a href="${createLink(url:grailsApplication.config.grails.serverURL+"/..") }" class="brand" style="margin-left: 150px; text-align: center;">
               <img src="${Utils.getIBPServerDomain()+'/'+grailsApplication.config.speciesPortal.app.dzongkha}" id="imagelogo"> <br />
               <h1>${grailsApplication.config.speciesPortal.app.siteName}</h1>
            </a>
                                                  
            <a class="pull-right" style="margin-right: 0px;"> <img class="logo" src="${Utils.getIBPServerDomain()+'/'+grailsApplication.config.speciesPortal.app.govlogo}" title="Royal Government of Bhutan" alt="Royal Government of Bhutan">                                                           </a> 

        

		</g:else>
	</div>
</div>
<div class="siteNav navbar navbar-static-top btn"
	style="margin-bottom: 0px; position: relative; width: 100%;padding: 0px; border: 0;">
	<div class="navbar-inner"
		style="box-shadow: none; background-color: transparent; background-image: none;height:30px;border:0px;">
		<div class="container outer-wrapper"
			style="background-color: transparent; padding-bottom: 0px;text-align:center;height:30px;">
			<!-- Navigation -->
      <nav class="nav-collapse collapse">
        <ul class="siteNav nav">
        <li class="dropdown ${(['species','namelist','trait'].contains(params.controller))?'active':''}">
	        <a href="#" title="${g.message(code:'default.species.label')}" class="dropdown-toggle" data-toggle="dropdown">
								<span  title="${g.message(code:'updated.today')}" class="statsTicker speciesUpdateCount"> </span>
								<g:message code="default.species.label" />
								<b class="caret"></b>
			</a>
		
            <ul class="dropdown-menu mega-menu">
              <li class="mega-menu-column">
                <ul>
                  <li><a href="${uGroup.createLink('controller':'species', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.speciesPage.label')}"><g:message code="default.speciesPage.label" /></a></li>
                  <li><a href="${uGroup.createLink('controller':'namelist', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.taxonNamelist.label')}"><g:message code="default.taxonNamelist.label" /></a></li>
                  <li><a href="${uGroup.createLink('controller':'trait', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.speciesTrait.label')}"><g:message code="default.speciesTrait.label" /></a></li>
                </ul>
              </li>
            </ul>
            <!-- dropdown-menu --> 
            
          </li>
          <!-- /.dropdown -->

		 <li class="dropdown ${(['observation','checklist','dataTable', 'dataset'].contains(params.controller))?'active':''}">
		 <a	href="#" title="${g.message(code:'default.observation.label')}" class="dropdown-toggle" data-toggle="dropdown">
		 		<span title="${g.message(code:'created.today')}" class="statsTicker obvUpdateCount"> </span>
		 		<g:message code="default.observation.label" />
		 		<b class="caret"></b>
		  </a>
            <ul class="dropdown-menu mega-menu">
              <li class="mega-menu-column">
                <ul>
                  <li><a href="${uGroup.createLink('controller':'observation', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.observation.label')}"><g:message code="default.observation.label" /></a></li>
                  <li><a href="${uGroup.createLink('controller':'checklist', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.list.label')}"><g:message code="default.list.label" /></a></li>
                  <li><a href="${uGroup.createLink('controller':'dataTable', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.dataTables.label')}"><g:message code="default.dataTables.label" /></a></li>
                  <li><a href="${uGroup.createLink('controller':'dataset', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.datasets.label')}"><g:message code="default.datasets.label" /></a></li>
                  <!--li><a href="${uGroup.createLink('controller':'datasource', 'userGroup':userGroupInstance)}" title="${g.message(code:'default.datasource.label')}"><g:message code="default.datasource.label" /></a></li-->
 
                </ul>
              </li>
            </ul>
            <!-- dropdown-menu --> 
            
          </li>
          <!-- /.dropdown -->
          <% def map_href = '/map';
          	if(userGroupInstance && userGroupInstance.webaddress){
          		map_href = uGroup.createLink('mapping':'userGroup', 'action':'map', 'userGroup':userGroupInstance)
          	}
          %>
          <li class="${(request.getHeader('referer')?.contains('/map') && params.action == 'header')?' active':''}">
          		<a href="${map_href}" title="${g.message(code:'button.maps')}"><g:message code="button.maps" /></a>
          </li>          
		<li	class="${((params.controller == 'document' && params.action == 'browser') ||(params.controller == 'browser'))?' active':''}">
			<a href="${uGroup.createLink('controller':'document', 'action':'browser', 'userGroup':userGroupInstance)}"
               title="${g.message(code:'button.documents')}">
               <span title="${g.message(code:'updated.today')}" class="statsTicker docUpdateCount"> </span>
               <g:message code="button.documents" />
               </a>
         </li>	
          <li class="dropdown" > 
          		<a href="javascript:void(0);" class="dropdown-toggle contributeButton" data-toggle="dropdown" ><g:message code="button.contribute" /> <b class="caret"></b></a>
            <ul class="dropdown-menu mega-menu pull-right contributeUL" style="width:280px;">
              <li class="mega-menu-column">
                <ul>
                  <li class="contributeUlLi"><a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.species.detailed.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'species', action:'contribute', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="${g.message(code:'link.contribute.to')}" title="${g.message(code:'link.contribute.to')}"> <i class="icon-plus"></i><g:message code="link.contribute.to" /></a>
</li>

                  <li class="contributeUlLi"><a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.observation.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add an Observatios" title="${g.message(code:'link.add.observation')}"> <i class="icon-plus"></i><g:message code="link.add.observation" /></a>
                                </li>
                  <li class="contributeUlLi">
                   <a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.observation.multiple.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'observation', action:'bulkCreate', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add Multiple Observations" title="${g.message(code:'title.add.multiple')}"> <i class="icon-plus"></i><g:message code="link.add.multiple" /></a>
                  </li>
                  <li class="contributeUlLi"><a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.list.description')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'dataTable', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" data-original-title="Add a List" title="${g.message(code:'link.add.list')}"> <i class="icon-plus"></i><g:message code="link.add.list" /></a>
 </li>
                  
                  <li class="contributeUlLi"><a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.document.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                    data-original-title="Add Document" title="${g.message(code:'link.add.document')}">
                                    <i class="icon-plus"></i><g:message code="link.add.document" />  
                                </a>
                  </li> 
                  <sUser:isAdmin>
                  <li class="contributeUlLi"><a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.trait.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'trait', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                    data-original-title="Add Trait" title="${g.message(code:'link.add.trait')}">
                                    <i class="icon-plus"></i><g:message code="link.add.trait" />  
                                </a>
                  </li>

                  <li class="contributeUlLi"><a class="btn btn-success" data-toggle="popover" data-placement="right" data-content="${g.message(code:'title.fact.info')}" data-trigger="hover"
                                    href="${uGroup.createLink(
                                    controller:'fact', action:'upload', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                    data-original-title="Add Fact" title="${g.message(code:'link.add.fact')}">
                                    <i class="icon-plus"></i><g:message code="link.add.fact" />  
                                </a>
                  </li>
                  </sUser:isAdmin>
                </ul>
              </li>
            </ul>
            <!-- dropdown-menu --> 
 







					<li class=" ${(params.controller == 'discussion')?'active':''}"><a
                                            href="${uGroup.createLink("controller":"discussion", "action":"list")}" title="${g.message(code:'button.discussions')}"><g:message code="button.discussions" /><span title="${g.message(code:'updated.today')}" class="statsTicker disUpdateCount"> </span></a>
					</li>

                    <li class=" ${(params.controller == 'datasource')?'active':''}"><a
                                            href="${uGroup.createLink("controller":"datasource", "action":"list")}" title="${g.message(code:'button.datasources')}"><g:message code="button.datasources" /></a>
					</li>


										<li class=" ${(params.controller == 'activityFeed')?'active':''}"><a
                                href="${uGroup.createLink("controller":"activityFeed")}" title="${g.message(code:'button.activity')}"><g:message code="button.activity" /></a>
					</li>

					<li class="dropdown"><a href="#" class="dropdown-toggle"
						data-toggle="dropdown"> <g:message code="link.moree" /> <b class="caret"></b> </a>

						<ul class="dropdown-menu" style="text-align: left; color: #000">
						
                            <li class="${(params.action == 'pages')?' active':''}"><a
                                href="${uGroup.createLink(mapping:"pages", controller:"userGroup", 'action':"pages")}"
                                title="${g.message(code:'default.pages.label')}"><g:message code="default.pages.label" /></a>
                            </li>


							<li
								class="${((params.controller == 'user' || params.controller == 'SUser') && params.action != 'header')?' active':''}"><a
								href="${uGroup.createLink(controller:'user', action:'list')}"
								title="${g.message(code:'default.members.label')}"><g:message code="default.members.label" /></a></li>
<%--							<li--%>
<%--								class="${(request.getHeader('referer')?.contains('/calendar') && params.action == 'header')?' active':''}"><a--%>
<%--								href="/calendar" title="Events">Events</a></li>--%>
<%--							<li--%>
<%--								class="${(request.getHeader('referer')?.contains('/biodiversity_news') && params.action == 'header')?' active':''}"><a--%>
<%--								href="/biodiversity_news" title="News">News</a></li>--%>
							
							<li
								class="${(params.controller == 'chart')?' active':''}"><a
								href="${uGroup.createLink(controller:'chart')}"
								title="${g.message(code:'button.dashboard')}"><g:message code="button.dashboard" /></a> </li>
							<li
								class="${(request.getHeader('referer')?.contains('/about') && params.action == 'header')?' active':''}"><a
								href="/bbp/aboutus" title="${g.message(code:'button.about.us')}" > <g:message code="button.about.us" /> </a></li>	
						</ul>
					</li>
				        <!-- /.nav --> 
      </nav>
      <!--/.nav-collapse --> 
			
		</div>
	</div>
</div>
<script type="text/javascript">
$(document).ready(function(){
	//IMP:Header is loaded in drupal pages as well. Any code in this block is not run when loaded by ajax
	//So please don't put any code here. Put it in init_header function in membership.js
	 init_header("${uGroup.createLink('controller':'activityFeed', 'action':'stats', 'userGroup':userGroupInstance)}");
});

</script>
<script>
// Dropdown Menu Fade    
jQuery(document).ready(function(){
           
    $(".siteNav .dropdown").hover(
        function() { $('.dropdown-menu', this).fadeIn("fast");
        },
        function() { $('.dropdown-menu', this).fadeOut("fast");
    });

    $(".siteNav .dropdown").click(function() { 
        var targetComp = $(this).find('.dropdown-menu');
        if($(targetComp).parent().hasClass('open')){
          $(targetComp).hide(); 
        }else{
          $(targetComp).show();
        }
    });

    
});
function loadPages(target,url,pageURL){	
	//alert($(target).length);
	if(typeof offset == "undefined"){ offset = 0; }
  var countUGL = $('.pagelist').size();
  if(countUGL != 0){
    return
  }
  var parCnt = 0;  
	$.ajax({
 		url: url,
 		type: 'POST',
		dataType: "json",
		data: {"offset":offset},
		success: function(data) {
			console.log(data);
			var ulLi = '';
			$.each(data.newsletters,function(key,parENT){
				
				if(parENT.parentId == 0){
          parCnt++;
					ulLi += '<li class="mega-menu-column" style="width:250px;"><ul>';
					ulLi += '<li class="nav-header ellipsis pagelist"><a href="'+pageURL+'/'+parENT.id+'" title="'+parENT.title+'" >'+parENT.title+'</li>';
					$.each(data.newsletters,function(key,subparent){            
						if(subparent.parentId == parENT.id){	
              parCnt++;				
							ulLi += '<li class="ellipsis"><a href="'+pageURL+'/'+subparent.id+'" title="'+subparent.title+'" >'+subparent.title+'</a></li>';							
						}
					});
					//ulLi +='</li>';
					ulLi +='</ul></li>';
				}
			
			});

      if(parCnt >15 && parCnt >20){
        $('.pageUL').width(550);
      }else if(parCnt > 30 && parCnt >40){
        $('.pageUL').width(820);
      }
			
			target.html(ulLi);
		/*			
				$(targetComp).find('.load_more_usergroup').remove();
				$(targetComp).find('.group_load').remove();
				$(targetComp).append($(data.suggestedGroupsHtml));			
				$(targetComp).show();
				return false;
		*/
		}, error: function(xhr, status, error) {
			alert(xhr.responseText);
	   	}
	});

}
</script>
