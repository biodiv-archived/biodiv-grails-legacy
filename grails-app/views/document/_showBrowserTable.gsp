<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>
<style>
.showObvDetails{width:auto;margin-top:10px;}
.observation_story .prop .name {width:90px;}
.observation_story .prop .width {margin:2px 0 2px 90px;}
.addmargin{border:3px solid #a6dfc8 !important;}
.snippet.tablet{height:120px; width:128px;}
.signature{margin-top:5px;margin-right:20px;}
.twoellipse {
overflow: hidden;
 overflow: hidden;
   text-overflow: ellipsis;
   display: -webkit-box;
   line-height: 16  px;     /* fallback */
   max-height: 40px;      /* fallback */
   -webkit-line-clamp: 2; /* number of lines to show */
   -webkit-box-orient: vertical;
}
.observation_links{margin-top:-60px;}
</style>
<ul class="grid_view thumbnails obvListwrapper">
		<g:each in="${documentInstanceList}" status="i" var="documentInstance">
		<li class="thumbnail addmargin" style="clear: both; margin-left: 0px; width: 100%;height:170px;margin-bottom:3px;">
			<g:render template="/document/listDocumentStoryTemplate" model="['documentInstance':documentInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'featuredNotes':featuredNotes, featuredOn:featuredOn, showDetails:showDetails, showFeatured:showFeatured]"></g:render>
			</li>
		</g:each>
		</ul>