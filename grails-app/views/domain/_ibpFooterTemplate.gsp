<%@page import="species.utils.Utils"%>
<div id="ibp-footer" class="gradient-bg-reverse navbar">
<g:set var="userGroupService" bean="userGroupService"/>
<%
userGroupInstance = userGroupInstance && userGroupInstance.id ? userGroupInstance : null;
	def newsletters = userGroupService.getNewsLetters(userGroupInstance, -1, 0, "displayOrder", "asc", null, ['showInFooter':true]);
    def pages = [:];
    def pagesWithNoParent = [:];
    newsletters.each {
        if(it.parentId) {
            if(!pages[it.parentId]) pages[it.parentId] = [];
            pages[it.parentId] << it;
        } else {
            pagesWithNoParent[(int)it.id] = it;
        }
    }

%>
	<div class="container outer-wrapper" style="width:940px">
		<!--div class="links_box_column">
			<ul>
				<li
					class=" nav-header bold${(params.controller == 'species')?' active':''}"><a
					href="${uGroup.createLink(controller:'species', action:'list')}"
					title="Species"><g:message code="link.all.species" /></a>
				</li>
				<li
					class="nav-header bold ${(request.getHeader('referer')?.contains('/map'))?' active':''}"><a
					href="${ '/map'}" title="Maps"><g:message code="link.all.maps" /></a></li>
				<li
					class=" nav-header bold${(params.controller == 'checklist')?' active':''}"><a
					href="${uGroup.createLink(controller:'checklist', action:'list')}" title="Checklists"><g:message code="link.all.checklists" /> </a></li>
				<li
					class="nav-header bold ${(params.controller == 'userGroup' && params.action== 'list')?' active':''}"><a
					href="${ uGroup.createLink(controller:"userGroup", "action":"list")}"
					title="Groups is in Beta. We would like you to provide valuable feedback, suggestions and interest in using the groups functionality.">All
						Groups<sup>Beta</sup> </a>
				</li>
			</ul>
		</div-->

        <g:each var="page" in="${pages}">
        	<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href="${userGroupInstance?uGroup.createLink('mapping':'userGroup', 'action':'page', 'id':pagesWithNoParent[page.key]?.id, 'userGroup':userGroupInstance):'/page/'+pagesWithNoParent[page.key]?.id }">${pagesWithNoParent[page.key]?.title}</a></li>
                <%pagesWithNoParent.remove(page.key)%>
                <g:each var="sub_page" in="${page.value}">
				    <li><a href="${userGroupInstance?uGroup.createLink('mapping':'userGroup', 'action':'page', 'id':sub_page.id, 'userGroup':userGroupInstance):'/page/'+sub_page.id }">${sub_page.title}</a></li>
                </g:each>
	        </ul>	
            </div>
        </g:each>
		<!--div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/theportal'><g:message code="link.the.portal" /></a></li>               
				<li><a href="${ '/biodiversity_in_india'}"><g:message code="link.biodiversity.india" /></a>
				</li>
				<li><a href="${ '/about/whats-new'}"><g:message code="link.what.new" /></a></li>
				<li><a href="${ '/about/technology'}"><g:message code="link.technology" /></a></li>
				<li><a href="${ '/help/faqs'}"><g:message code="link.faq" /></a></li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/people'><g:message code="link.people" /></a></li>
				<li><a href="${ '/people/partners'}"><g:message code="link.partners" /></a></li>
				<li><a href="${ '/people/donors'}"><g:message code="link.donors" /></a></li>
				<li><a href="${ '/people/fraternity'}"><g:message code="link.fraternity" /></a></li>
				<li><a href="${ '/people/team'}"><g:message code="link.team" /></a></li>
			</ul>
		</div-->

		<div class="links_box_column">
			<ul>
				<li class="nav-header bold"  style="padding-left: 0px;"><a href='/page/4250187'><g:message code="link.policy" /></a>
				</li>
				<li><a href="/page/4250189"><g:message code="link.data.sharing" /></a>
				</li>
				<li><a href="/page/4250212"><g:message code="default.licenses.label" /></a>
				</li>
				<li><a href="/page/4250246"><g:message code="link.terms.conditions" /></a>
				</li>

			</ul>
		</div>
		<div class="links_box_column">
			<ul>
				<li class="nav-header bold" style="color:#5E5E5E; padding-left: 0px;"><g:message code="default.others.label" /></li>
        <g:if test="${pagesWithNoParent}">
                <g:each var="page" in="${pagesWithNoParent}">
				    <li><a href="">${page.value.title}</a></li>
                </g:each>
        </g:if>

                <li><a href="http://blog.indiabiodiversity.org/"><g:message code="link.blog" /></a></li>
				<li><a href="${ '/sitemap'}"><g:message code="link.sitemap" /></a>
				</li>
                <li><a href="${ '/biodiv/docs'}"><g:message code="link.apidocs" /></a>
				</li>

				<li><a href="${ '/feedback_form'}"><g:message code="link.feedbak" /></a>
				</li>
				<li><a href="${ '/contact'}"><g:message code="link.contact.us" /></a>
				</li>

			</ul>
		</div>



            </div>
    <div class="powered" style="text-align:center;">
        <p title="India Biodiversity Portal Mobile App">
            <a href="https://play.google.com/store/apps/details?id=com.mobisys.android.ibp&hl=en">
                <asset:image src="/all/googleplayicon1.png" class="mobile_app_icon" absolute="true"/>
            </a>
            <a href="https://itunes.apple.com/in/app/india-biodiversity-portal/id1072650706?mt=8&ign-mpt=uo%3D4">
                <asset:image src="/all/apple_store1.png" class="mobile_app_icon" absolute="true"/>
            </a>
        </p>
        <p> <a target="_blank" href="${grailsApplication.config.speciesPortal.app.facebookUrl}">
            <asset:image src="/all/facebook.png" absolute="true"/>
        </a> | <a  target="_blank" href="${grailsApplication.config.speciesPortal.app.twitterUrl}">
            <asset:image src="/all/twitter.png" absolute="true"/>
        </a>| <a  target="_blank" href="${grailsApplication.config.speciesPortal.app.googlePlusUrl}">
            <asset:image src="/all/google_plus.png" absolute="true"/>
        </a><br />
<g:message code="text.supported.on" /><br />
<g:message code="text.powered.by" /> <a href="https://github.com/strandls/biodiv" target="_blank"><g:message code="link.informatics.platform" /></a>
<g:message code="text.technology.partner" /> <a href="http://www.strandlifefoundation.org/" target="_blank"><g:message code="technology.partner.strandlifesciences" /></a>
</p>
	</div>
</div>
<asset:script>
$(document).ready(function(){
	last_actions();
});

//Twitter
!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');

//google plus
(function() {
var po = document.createElement('script'); 
po.type = 'text/javascript'; po.async = true;
po.src = 'https://apis.google.com/js/plusone.js?onload=renderGooglePlus';
var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
})();


(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-3185202-1', 'auto');
ga('send', 'pageview');
</asset:script>


