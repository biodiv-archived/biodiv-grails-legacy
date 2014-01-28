<%@ page import="species.utils.Utils"%>
<html>

<head>
<g:set var="title" value="User"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="susers_list" />
<style type="text/css">
.thumbnails>li {
        margin:0 1px 5px 0px;
        padding:0px;
    }

.thumbnail .observation_story {
    width: 784px;
}
</style>
</head>

<body>

	<div class="span12">
		<search:searchResultsHeading />
		<!-- uGroup:rightSidebar/-->

		<div class="searchResults" style="margin-left: 0px;">
			<sUser:showUserListWrapper />
		</div>
	</div>


</body>
</html>
