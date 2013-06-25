<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.utils.ImageType"%>
<%@page	import="org.springframework.web.context.request.RequestContextHolder"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="java.util.Arrays"%>

<html>
<head>
<g:set var="title" value="Observations"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="checklist_create"/>
<uploader:head />
<%-- <style>--%>
<%--    .cell-title {--%>
<%--      font-weight: bold;--%>
<%--    }--%>
<%----%>
<%--    .cell-effort-driven {--%>
<%--      text-align: center;--%>
<%--    }--%>
<%--  </style>--%>
</head>
<body>
		<div class="observation_create">
			<div class="span12">
				<obv:showSubmenuTemplate model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Observation']"/>
				<%
					def allowedExtensions = "['csv']"
				%>
				<g:render template='/UFile/docUpload' model="['name': 'checklistStartFile', allowedExtensions:allowedExtensions,uploadCallBack:'showGrid()']" />
				<div>
  					<div>
						<div id="myGrid" style="width:620px;height:350px;display:none;"></div>
					</div>
				</div>
            </div>
       </div>

<r:script>
  function f(data, columns){
    var grid;
  	var options = {
    editable: true,
    enableAddRow: true,
    enableCellNavigation: true,
    asyncEditorLoading: false,
    autoEdit: false
  };
  
  

  $(function () {
	  	grid = new Slick.Grid("#myGrid", data, columns, options);
	
	    grid.setSelectionModel(new Slick.CellSelectionModel());
	
	    grid.onAddNewRow.subscribe(function (e, args) {
	      var item = args.item;
	      grid.invalidateRow(data.length);
	      data.push(item);
	      grid.updateRowCount();
	      grid.render();
	    });
	   
	   $("#myGrid").show();
	   $('#checklistStartFile_uploaded').hide();
	});
	
  } 

  function showGrid(){
 	var input = $("#checklistStartFile_path").val(); 
 	parseData("/biodiv/content" + input , {callBack:f});
  }

  function requiredFieldValidator(value) {
    if (value == null || value == undefined || !value.length) {
      return {valid: false, msg: "This is a required field"};
    } else {
      return {valid: true, msg: null};
    }
  }
</r:script>
</body>
</html>
