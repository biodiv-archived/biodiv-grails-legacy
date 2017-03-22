<%@page import="species.utils.Utils"%>
<html>
<head>
<meta name="layout" content="main" />
<title><g:message code="link.feedbak" /></title>
<style>
	.form-item label{
		display: -webkit-inline-box;
}
</style>
</head>

<body>
<!--div id="content" class="span8">
					
					
	<div id="contentDiv">
		
		<div class="return">
			
		</div>
		<h3>Feedback or Suggestions</h3>																														
		
		
	<div id="node-101" class="node clear-block">

	  <div class="meta">
	  
	    </div>

	 <div class="content">
		 <p>Please use this page to post any feedback you may have about the site. This is a public participatory site where we provide a platform for aggregating and publishing information on biodiversity and conservation of the India.</p>
		<p>If there are errors or mistakes on the information provided on the site we would greatly appreciate if you bring them to our notice. We expect to evolve and grow by your participation.</p>
		<form action="${uGroup.createLink(action:'feedback_form', controller:'species' )}" accept-charset="UTF-8" method="post" id="webform-client-form-101" class="webform-client-form" enctype="multipart/form-data">
		<div><div class="webform-component webform-component-textfield" id="webform-component-name"><div class="form-item" id="edit-submitted-name-wrapper">
		 <label for="edit-submitted-name">Name: <span class="form-required" title="This field is required.">*</span></label>
		 <input type="text" maxlength="128" name="name" id="edit-submitted-name" size="60" value="" class="form-text required">
		 <div class="description">
		</div>
		</div>
		</div><div class="webform-component webform-component-email" id="webform-component-email"><div class="form-item" id="edit-submitted-email-wrapper">
		 <label for="edit-submitted-email">Email: <span class="form-required" title="This field is required.">*</span></label>
		 <input type="email" maxlength="128" name="email" id="edit-submitted-email" size="60" value="" class="form-text required email">
		</div>
		</div><div class="webform-component webform-component-textarea" id="webform-component-message"><div class="form-item" id="edit-submitted-message-wrapper">
		 <label for="edit-submitted-message">Feedback: <span class="form-required" title="This field is required.">*</span></label>
		 <div class="resizable-textarea"><span><textarea cols="60" rows="5" name="message" id="edit-submitted-message" class="form-textarea resizable required textarea-processed"></textarea><div class="grippie" style="margin-right: -14px;"></div></span></div>
		 <div class="description"><p>Any feedback you may have</p>
		</div>
		</div>
		</div><input type="hidden" name="details[sid]" id="edit-details-sid" value="">
		<input type="hidden" name="details[page_num]" id="edit-details-page-num" value="1">
		<input type="hidden" name="details[page_count]" id="edit-details-page-count" value="1">
		<input type="hidden" name="details[finished]" id="edit-details-finished" value="0">
		<input type="hidden" name="form_build_id" id="form-bf1d23b44d57a0b7c3c017bf3b8159d1" value="form-bf1d23b44d57a0b7c3c017bf3b8159d1">
		<input type="hidden" name="form_id" id="edit-webform-client-form-101" value="webform_client_form_101">
		<div id="edit-actions" class="form-actions form-wrapper" style="background-color:transparent"><input type="submit" name="op" id="edit-submit" value="Submit" class="form-submit">
		</div>
		</div></form>
		  </div>

		  </div>						
		</div>
	</div-->

	<div class="observation  span12">

			<!--Write up for About Us Page -->
			<div class="writeup">
			<h2>Feedback or Suggestions</h2>
			<p>Please use this page to post any feedback you may have about the site. This is a public participatory site where we provide a platform for aggregating and publishing information on biodiversity and conservation of the Western Indian Oceean.</p>
		<p>If there are errors or mistakes on the information provided on the site we would greatly appreciate if you bring them to our notice. We expect to evolve and grow by your participation.</p>
			<p>For any technical feedback, please contact us at projectwikwio[at] gmail.com <br><br>For any content feedback, please contact Dr. Thomas Le Bourgeois at thomas.le_bourgeois [at] cirad.fr<br>
			<p>&nbsp;</p>

			<!--Feedback Form -->
			
			<!-- END Feedback Form -->
			</div>
			 
			<!-- End of Write Up for ABout Us Page -->
	</div>
</body>
</html>