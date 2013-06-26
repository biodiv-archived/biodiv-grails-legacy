<%@page import="species.utils.Utils"%>
<%@ page import="content.Project"%>
<html>
<head>
<g:set var="title" value="Projects" />
<g:render template="/common/titleTemplate" model="['title':title]" />
<r:require modules="content_view" />

<style>
<!--
.project-list .odd {
	background-color: ghostwhite;
}

-->
.item {
	border-top: 5px solid #c2c2c2;
	border-bottom: 2px solid #c2c2c2;
	background-color: #ffffff;
	width: 590px;
}

.project-list-item {
	margin: 20px;
}

.thumbnails>.thumbnail {
	margin: 0 0 10px 0px;
	width: 100%;
}

.file-image {
	float: left;
	background-image: url(/biodiv/images/tick.png);
	background-repeat: no-repeat;
	padding: 10px 10px 10px 28px;
	margin: 10px;
	font-weight: bold;
}
</style>

</head>
<body>

	<div class="span12">
		<g:render template="/project/projectSubMenuTemplate"
			model="['entityName':'Western Ghats CEPF Projects']" />
		<uGroup:rightSidebar />
		<div class="span8 right-shadow-box" style="margin: 0px;">
			<g:render template="/project/search" model="['params':params]" />

			<obv:showObservationFilterMessage />

			<div class="observations_list_wrapper" style="top: 0px;">
				<g:render template="/project/projectListTemplate" />
			</div>

		</div>
		<g:render template="/project/projectSidebar" />
	</div>
	<r:script>

$(document).ready(function(){

	$(".list_view").show();
	
    $('.observations_list_wrapper').on('updatedGallery', function(event) {
    	$(".list_view").show();
    });
	
});


</r:script>

</body>
</html>
