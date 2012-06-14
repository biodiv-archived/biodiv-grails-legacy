<%@ page import="species.Language"%>
<style>
	.ui-autocomplete {
		max-height: 250px;
		overflow-y: auto;
		/* prevent horizontal scrollbar */
		overflow-x: hidden;
		/* add padding to account for vertical scrollbar */
		padding-right: 20px;
	}
	/* IE 6 doesn't support max-height
	 * we use height instead, but this forces the menu to always be this tall
	 */
	* html .ui-autocomplete {
		height: 100px;
	}
</style>
<script>
$(document).ready(function() {
	var availableLanguages;
	function initLanguage(){
		$.ajax({
			  url: '${createLink(controller:'observation', action: 'getFilteredLanguage')}',
			  dataType: 'json', 
			  async: false,
			  success: function(data) {
				 availableLanguages = data;
			  }
		});
	}
	initLanguage();	
	
	$("#languageName").autocomplete({
	 	source: availableLanguages,
	});

	$( "#toggle" ).click(function() {
		// close if already visible
		var input = $("#languageName");
		if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
			input.autocomplete( "close" );
			return false;
		}
		// work around a bug (likely same cause as #5265)
		$( this ).blur();

		// pass 'a' string as value to search for, displaying all results
		input.autocomplete( "search", "a");
		input.focus();
		return false;
	});
});

</script>
<input type="text" name="languageName" id="languageName" value="${Language.getLanguage(null).name}"
		placeholder='${Language.getLanguage(null).name}' style="width:20%"
		class="input ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" />
<a id="toggle" class="btn btn-mini" style="float:none; margin-left: -4px; min-height:22px; max-width:15px;" href="#" data-toggle="dropdown"><i class="icon-arrow-down"></i></a>