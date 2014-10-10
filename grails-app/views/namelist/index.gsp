<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="layout" content="main"/>
<title>Insert title here</title>
<style type="text/css">
	.listarea{
		border:1px solid #ccc;
		height:200px;
		margin:0px !important;
	}
	.taxon_selector_wrapper{
		width:285px!important;
	}

	.taxon_selector_wrapper_span{
		color:white;
		padding: 3px 65px;
	}
	.taxon_selector_span{
		background-color:#00FFFF;			
	}
	.dirty_list_span{
		background-color:#66FF33;
	}
	.working_list_span{
		background-color:#FF99CC;
	}

	.clean_list_span{
		background-color:#9933FF;
	}

	.listarea_content{
		height:170px;
		overflow-y:scroll;
	}

	.taxon_selector_list{

	}
	.taxon_selector_final{

	}
</style>

</head>
<body>

  <div class="row-fluid">
  	<div class="span3 listarea taxon_selector_wrapper">
  		<span class="taxon_selector_span taxon_selector_wrapper_span">
  			Taxon Selector
  		</span>
  		<div class="taxon_selector_list listarea_content">
  		</div>
  		<div class="taxon_selector_final">
  			<div class="row-fluid">

  				<div class="span6">	
  					<input type="text" placeholder="Search" class="span12"/>
  				</div>
  				
  				<div class="span2">
  					DEl
  				</div>
  				<div class="span2">
  					Ref		
  				</div>
  				<div class="span2">
  					Bac
  				</div>
  			</div>
  		</div>
  	</div>
  	<div class="span3 listarea">
  		<span class="dirty_list_span taxon_selector_wrapper_span">
  			Dirty List
  		</span>
  		<div class="taxon_selector_list listarea_content">

  		</div>

  		<div class="row-fluid">

  				<div class="span6">	
  					<input type="text" placeholder="Search" class="span12"/>
  				</div>
  				
  				<div class="span2">
  					DEl
  				</div>
  				<div class="span2">
  					Ref		
  				</div>
  				<div class="span2">
  					Bac
  				</div>
  			</div>


  	</div>
  	<div class="span3 listarea">
  		<span class="working_list_span taxon_selector_wrapper_span">
  			Working List
  		</span>
  		<div class="taxon_selector_list listarea_content">

  		</div>

  		<div class="row-fluid">

  				<div class="span6">	
  					<input type="text" placeholder="Search" class="span12"/>
  				</div>
  				
  				<div class="span2">
  					DEl
  				</div>
  				<div class="span2">
  					Ref		
  				</div>
  				<div class="span2">
  					Bac
  				</div>
  			</div>
  	</div>
  	<div class="span3 listarea">
  		<span class="clean_list_span taxon_selector_wrapper_span">
  			Clean List
  		</span>
  		<div class="taxon_selector_list listarea_content">

  		</div>

  		<div class="row-fluid">

  				<div class="span6">	
  					<input type="text" placeholder="Search" class="span12"/>
  				</div>
  				
  				<div class="span2">
  					DEl
  				</div>
  				<div class="span2">
  					Ref		
  				</div>
  				<div class="span2">
  					Bac
  				</div>
  		</div>


  	</div>


  </div>




<style type="text/css">
	.detailsarea{
		height: 250px;
    	border: 1px solid#ccc;
	}
	.detailsareaLeft{
	    border: 1px solid #ccc;
	}
	.detailsareaRight{
	    margin: 0px !important;
	    border: 1px solid #ccc;
	    width: 161px !important;
	    text-align:center;
	}
	.connection_wrapper{
		background-color:#00FFCC;
	}
	.connection_wrapper_row1{
		background-color: rgb(218, 150, 70);
	}
	.connection_wrapper_row2{
		background-color: burlywood;
	}
</style>

 <div class="row-fluid detailsarea">




  	<div class="span10 detailsareaLeft">

  			<div class="row-fluid">

  					<div class="span3">
  							<div  class="row-fluid">
  								<div class="span3">
  									<label>Status</label>
  								</div>
  								<div class="span9">	
  									<select class="span12" ><option>Name Status</option></select>
  								</div>
  							</div>
  							<div class="row-fluid">		
  								<div class="span3">	
  									<label>Rank</label>
  								</div>
  								<div class="span9">	
  									<select class="span12"><option>Name Rank</option></select>
  								</div>	
  							</div>
  					</div>

  					<div class="span9">
  						<div class="row-fluid">
	  						<div class="pull-right">
	  							<select><option>Database Query</option></select>
	  							<a class="btn btn-mini">Query</a>
	  						</div>	
	  					</div>
  							<div class="row-fluid">
  								<div class="span4">
									<label class="span3">Source</label>
  									<input type="text" placeholder="Source" class="span9" />
  								</div>	
  								<div class="span4">
	  								<label class="span3">Via</label>
	  								<input type="text" placeholder="Via" class="span9" />
	  							</div>
	  							<div class="span4">	
	  								<label class="span3">ID</label>
	  								<input type="text" placeholder="ID" class="span9" />
	  							</div>	
  							</div>
  					</div>

  			</div>


  	</div>














  	<div class="span2 detailsareaRight">
  
		  <div class="connection_wrapper">Connections</div>
		  
		  <div class="connection_wrapper_row1">Species Page</div>
		  
		  <div class="connection_wrapper_row2">123456</div>

		  <div class="connection_wrapper_row1">Observations</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
		  
		  <div class="connection_wrapper_row1">Lists</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
		  
		  <div class="connection_wrapper_row1">Maps</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
		  
		  <div class="connection_wrapper_row1">Documents</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
  
	</div>
 </div>

</body>
</html>