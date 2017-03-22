
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="entityName"
	value="${(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)}" />

<g:set var="title" value="${g.message(code:'ugroup.value.pages')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<style>
    #contentMenu > .nav-tabs > .active > a {
        font-weight:normal;
        color: black;
        background-color:rgba(98, 100, 39, 0.14);
        background: transparent;
        -ms-filter: "progid:DXImageTransform.Microsoft.gradient(startColorstr=#23626427,endColorstr=#23626427)"; /* IE8 */
        filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#23626427,endColorstr=#23626427);   /* IE6 & 7 */
              zoom: 1;
    }
    #contentMenu > .nav-tabs > li > a {
       color: #16509E;
        font-weight:bold;
        background-color: #CEEBD3;
        border: none
    }
    .page_chevron{
    	position:absolute;
    }
</style>
</head>
<body>
	<div class="observation span12">
		<!-- uGroup:showSubmenuTemplate model="['entityName':'Pages']" /-->
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]" />
		<div class="userGroup-section">
			<div class="pull-right">
				<g:if test="${userGroupInstance}">
					<sec:permitted className='species.groups.UserGroup'
						id='${userGroupInstance.id}'
						permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

						<a style="margin-bottom: 10px;"
							href="${uGroup.createLink(mapping:"userGroup", action:"pageCreate", 'userGroup':userGroupInstance)}"
							class="btn  btn-success"> <i class="icon-plus"></i><g:message code="default.add.page.label" /></a>
					</sec:permitted>
				</g:if>
				<g:else>
					<sUser:isAdmin>
						<a style="margin-bottom: 10px;"
							href="${uGroup.createLink(mapping:"userGroupGeneric", controller:'userGroup', action:"pageCreate") }"
							class="btn btn-success"> <i class="icon-plus"></i><g:message code="default.add.page.label" /></a>
					</sUser:isAdmin>
				</g:else>
			</div>
			<div class="list" style="clear: both;">
				<div id="contentMenu" class="tabbable tabs-right" style="">

            					<ul class="nav nav-tabs sidebar_section span4" id="pageTabs" style="float: left;margin-left: 0px;  margin-right: 20px;list-style: outside none none;">
                                                <li><h5><g:message code="default.pages.label" /></h5></li>
						<g:each in="${newsletters}" var="newsletterInstance" status="i">
	                        <g:if test="${newsletterInstance.parentId ==0}">                        
		                        <li class="newsletter_parent" id="newsletter_${newsletterInstance.id}">
		                            <a data-toggle="tab" class="pageTab" href="#${newsletterInstance.id}">
		                            	<p style="width: 300px;overflow:hidden;padding-top: 5px; margin-bottom: 5px;text-overflow:ellipsis;white-space:nowrap;height:13px;margin-bottom:2px;">
		                                	${fieldValue(bean: newsletterInstance, field: "title")}
		                                </p>
		                            	<sUser:permToReorderPages model="['userGroupInstance':userGroupInstance]"><i class="icon-circle-arrow-down pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'newsletter', action:'changeDisplayOrder', 'userGroup':userGroupInstance)}","${newsletterInstance.id}", "down", "newsletter")'></i><i class="icon-circle-arrow-up pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'newsletter', action:'changeDisplayOrder', 'userGroup':userGroupInstance)}", "${newsletterInstance.id}", "up", "newsletter")'></i>
		                                </sUser:permToReorderPages>
		                            </a>
		                            <span class="newsletter_sub_c" style="float: right;top: -25px;position: relative;right: 25px;cursor: pointer;"></span>
		                                <ul style="margin:0px;list-style: outside none none;" class="subnewsl">
		                                <g:each in="${newsletters}" var="subnewsl" status="j">
                   	                       <g:if test="${subnewsl.parentId ==newsletterInstance.id}">                        
			                                	<li id="newsletter_${subnewsl.id}"  style="  border: 1px solid #ccc;">
			                                		<a data-toggle="tab" class="pageTab subAnchor" href="#${subnewsl.id}">
					                            	<p style=" width: 270px; padding-top: 5px; margin-bottom: 5px; margin-left: 30px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
					                                	${fieldValue(bean: subnewsl, field: "title")}
					                                </p>
					                                </a>
			                                	</li>
			                                </g:if>
		                                </g:each>
		                                </ul>		                                
		                        </li>
	                        </g:if>
						</g:each>
                        <g:if test="${userGroupInstance && userGroupInstance.name.equals('The Western Ghats')}">
							<li><a href="/project/list"><g:message code="link.cepf.projects" />  
									</a></li>
						</g:if>

                                            </ul>
					<div class="tab-content">
						<g:each in="${newsletters}" var="newsletterInstance" status="i">
							<div class="tab-pane active" id=${newsletterInstance.id}></div>
						</g:each>
                                            </div>
				</div>
                            </div>
			</div>
		</div>
	</div>


	<asset:script>
		$(document).ready(function(){
			$('#pageTabs ul').hide();
			$('#pageTabs .newsletter_parent').each(function(){
				var t = $(this).find('ul li').length;
				if(t >0 ){ $(this).find('.newsletter_sub_c').html(t+'<i class="icon-chevron-right page_chevron"></i>'); }
			});

			/*$('.newsletter_sub_c').click(function(){
				$('#pageTabs ul').hide();
				$(this).parent().find('ul').show();
			});*/

			var baseURL = "${uGroup.createLink('controller':'newsletter', 'action':'show', 'userGroup':userGroupInstance) }";
			<%if(userGroupInstance ) {%>
				var pageURL = "${uGroup.createLink('mapping':'userGroup', 'action':'page', 'userGroup':userGroupInstance) }";
			<%} else {%>
				var pageURL = "/page";
			<%}%>
	        $('#pageTabs a').click(function (e) {
  				$('#pageTabs li').removeClass('active');  	
  				if(!$(this).hasClass('subAnchor')){
  					$('#pageTabs .subnewsl').hide(); 
  					$('.page_chevron').removeClass('icon-chevron-down').addClass('icon-chevron-right');
  					$(this).parent().find('.page_chevron').removeClass('icon-chevron-right').addClass('icon-chevron-down');
  				}
  				$(this).parent().find('.subnewsl').show(); 
  				
  				var me = $(this);
  				var contentID = me.attr('href');//e.target.hash; //get anchor
  				if(contentID && contentID != '/project/list') {
	  				e.preventDefault();
	  				var History = window.History;
		           	$(contentID).load(baseURL+'/'+contentID.replace('#','')+' #pageContent', function(){
				    	History.pushState({state:1}, document.title, pageURL+'/'+contentID.replace('#',''));
		            	me.tab('show');
		            	$( '.cycle-slideshow' ).cycle();
		           	});
	           	} 
			});
			<%if(params.newsletterId) { %>
				$('a.pageTab[href~="#${params.newsletterId}"]').click();
			<%} else {%>
				$('a.pageTab:first').click();
			<%}%>
		});
	</asset:script>
</body>
</html>
