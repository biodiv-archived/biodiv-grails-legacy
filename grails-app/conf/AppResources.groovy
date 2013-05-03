
//adhoc.patterns.excludes = ["*.css"]
//mappers.hashandcache.excludes = ["**/*.css"]
//
modules = {
	overrides {
		jquery { defaultBundle 'core' }
		'jquery-ui' {
			defaultBundle 'core'
			resource id:'js', url:[dir:'plugins',file:'jquery-ui-1.8.15/jquery-ui/js/jquery-ui-1.8.15.custom.min.js'],
					nominify: true, disposition: 'defer'
					
		}

		'jquery-theme' {
			resource id:'theme',
					url:[dir: 'css',
						file:'jquery-ui.css'],
					attrs:[media:'screen, projection']
		}

	}

	core {
		dependsOn 'jquery, jquery-ui,carousel'
		defaultBundle 'core'

		resource url:'/bootstrap/css/bootstrap.min.css'
		resource url:'/css/bootstrap-combobox.css'
		resource url:'/js/jquery/jquery.jqGrid-4.1.2/css/ui.jqgrid.css'
		resource url:'/css/auth.css'
//		resource url:[dir:'css',file:'spring-security-ui.css',plugin:'spring-security-ui']
//		resource url:[dir:'css',file:'jquery.safari-checkbox.css',plugin:'spring-security-ui']
//		resource url:'/css/text.css'
		resource url:'/css/navigation.css'
		resource url:'/css/jquery.rating.css'
		resource url:'/css/daterangepicker.css'
		resource url:'/css/speciesGroups.css'
		resource url:'/css/habitats.css'
		resource url:'/css/tableSorter.css'
		resource url:'/css/bootstrap-editable.css'
        resource url:'/css/prettyPhoto.css'
		resource url:'/css/wgp.css'

		resource url:'/bootstrap/js/bootstrap.min.js'
		resource url:'/js/species/main.js'
		resource url:'/js/species/util.js'
		resource url:'/js/readmore/readmore.js'
		resource url:'/js/jquery/jquery.form.js'
		resource url:'/js/jquery/jquery.rating.js'
		resource url:'/js/species/rating.js'
		resource url:'/js/jquery/jquery.cookie.js'
		//resource url:'/js/jquery/jquery.checkbox.js'
		resource url:'/js/jquery/jquery.ellipses.js'
		resource url:'/js/jquery/jquery.prettyPhoto.js'
		resource url:'/js/species/popuplib.js', disposition: 'head'
		resource url:'/js/species/ajaxLogin.js'
		resource url:'/js/species/users.js'
		resource url:'/js/jquery/jquery.linkify-1.0.js'
		resource url:'/js/timeago.js'
		//resource url:'/js/species/ajaxLogin.js'
		resource url:[dir:'js',file:'jquery/jquery.checkbox.js',plugin:'spring-security-ui']
		resource url:[dir:'js',file:'spring-security-ui.js', plugin:'spring-security-ui']
		resource url:'/js/jquery/trunk8.js'
		resource url:'/js/species/membership.js'
		resource url:'/js/date.js'
		resource url:'/js/daterangepicker.js'
		resource url:'/js/stats.js'
		resource url:'/js/jquery.tablesorter.js'
		resource url:'/js/bootstrap-editable.min.js'
	}

	auth {
		dependsOn 'core'
	}

	gallery {
		resource url:[dir:'js/galleria/1.2.7/themes/classic/',file:'galleria.classic.css']
		resource url:'/js/galleria/1.2.7/galleria-1.2.7.min.js'
	}

	carousel {
		resource url:[dir:'js/jquery/jquery.jcarousel-0.2.8/themes/classic/',file:'skin.css']
		resource url:'/js/jquery/jquery.jcarousel-0.2.8/jquery.jcarousel.js'
		resource url:'/js/species/carousel.js'
	}

	tagit {
		resource url:'/css/tagit/jquery.tagit.css'
		resource url:'/css/tagit/tagit-custom.css'
		resource url:'/js/tag-it.js'
	}

	list_utils {
		resource url:'/js/jquery/jquery-history-1.7.1/scripts/bundled/html4+html5/jquery.history.js'
		resource url:'/js/jquery/jquery.url.js'
		resource url:'/js/jquery/jquery.autopager-1.0.0.js'
	}

	location_utils { resource url:'/js/location/google/markerclusterer.js'		 }

	observations {
		dependsOn 'core, tagit'
		defaultBundle 'core'

		
		resource url:'/js/jquery/jquery.watermark.min.js'
		resource url:'/js/jsrender.js'
		//resource url:'/js/bootstrap-typeahead.js'
		resource url:'/js/bootstrap-combobox.js'
	}

	observations_show {
		dependsOn 'observations, gallery, carousel, comment, activityfeed'

		resource url:'/js/jsrender.js'
		resource url:'/js/species/observations/show.js'
	}

	observations_create {
		dependsOn 'observations'

		resource url:'/css/location_picker.css'
		resource url:'/js/location/location-picker.js'
		resource url:'/js/jquery/jquery.exif.js'
	}

	observations_list { 
		dependsOn 'observations, location_utils, list_utils, comment'
		
		resource url:'/js/species/observations/list.js'
	}

	susers_list { 
		dependsOn 'core, list_utils'
		
		resource url:'/js/species/observations/list.js'
		//resource url:'/js/species/users/list.js'
	}

	species {
		dependsOn 'core, list_utils, tagit'

		resource url:'/css/960.css'
		resource url:'/css/main.css'
		resource url:'/css/wgp.css'

		resource url:'/js/species/species.js'

	}

	species_show {
		dependsOn 'species, gallery, comment, activityfeed'

		resource url:'/css/augmented-maps.css'
		resource url:[dir:'js/jquery/jquery.jqGrid-4.1.2/css',file:'ui.jqgrid.css']
		resource url:'/css/bootstrap-wysihtml5-0.0.2.css'
		
		resource url:'/js/jquery/jquery.jqGrid-4.1.2/js/i18n/grid.locale-en.js'
		resource url:'/js/jquery/jquery.jqGrid-4.1.2/js/jquery.jqGrid.src.js'
		resource url:'/js/galleria/1.2.7/plugins/flickr/galleria.flickr.min.js'
		//resource url:'/js/jquery.collapser/jquery.collapser.min.js'
		resource url:'/js/jquery/jquery.jqDock-1.8/jquery.jqDock.min.js'
		resource url:'/js/floating-1.7.js'
		resource url:'/js/wysihtml5-0.3.0_rc2.min.js'
		resource url:'/js/bootstrap-wysihtml5-0.0.2.min.js'		
		resource url:'/js/wysihtml5.js'
	}
	
	species_list {
		dependsOn 'observations_list'
	}

	search {
		dependsOn 'core'

		resource url:'/css/960.css'
		resource url:'/css/main.css'
	}

	admin { dependsOn	'core' }

	userGroups_show {
		dependsOn 'observations, gallery, carousel, activityfeed'

		resource url:'/js/jsrender.js'
		resource url:'/js/species/observations/show.js'
		resource url:'/js/species/userGroups/main.js'
	}

	userGroups_create {
		dependsOn 'observations'	
		resource url:'/js/species/userGroups/main.js'
	}

	userGroups_list {
		dependsOn 'observations, location_utils, list_utils'
		
		resource url:'/js/species/observations/list.js'
		resource url:'/js/species/userGroups/main.js'		
	}

	comment{
		resource url:'/css/comment.css'
		
		resource url:'/js/comment.js'
	}
	
	activityfeed {
		dependsOn 'core'
		
		resource url:'/css/comment.css'
		resource url:'/css/activityfeed.css'
		
		resource url:'/js/activityfeed.js'
	}
	
	checklist {
		dependsOn 'core, location_utils, list_utils, tagit, comment, activityfeed'
		
		resource url:'/css/main.css'
		
		resource url:'/js/species/observations/list.js'
		resource url:'/js/species/checklist.js'
	}
	
	chart {
		dependsOn 'core'
		
		resource url:'/js/chart.js'
	}

	add_file {
		dependsOn 'core, tagit'

		resource url:'/css/content.css'
		resource url:'/js/content.js'
		resource url:'/css/location_picker.css'
		resource url:'/js/location/location-picker.js'
		resource url:'/js/species/observations/list.js'
	}

}
