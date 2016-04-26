<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>
<style>
.observation_story{width:auto;margin-top:10px;}
.observation_story .prop .name {width:90px;}
.observation_story .prop .width {margin:2px 0 2px 90px;}
.addmargin{border:3px solid #a6dfc8 !important;}
.block-ellipsis {
  display: block;
  display: -webkit-box;
  max-width: 100%;
  height: 43px;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
<ul class="grid_view thumbnails obvListwrapper">
		<g:each in="${documentInstanceList}" status="i" var="documentInstance">
		<li class="thumbnail addmargin" style="clear: both; margin-left: 0px; width: 100%;height:160px;margin-bottom:3px;">
			<g:render template="/document/listDocumentStoryTemplate" model="['documentInstance':documentInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'featuredNotes':featuredNotes, featuredOn:featuredOn, showDetails:showDetails, showFeatured:showFeatured]"></g:render>
			</li>
		</g:each>
		</ul>