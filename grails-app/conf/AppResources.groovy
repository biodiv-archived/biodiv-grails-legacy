
//adhoc.patterns.excludes = ["*.css"]
//mappers.hashandcache.excludes = ["**/*.css"]
//

modules = {
	overrides {
		jquery { 
            defaultBundle 'core' 
        }

		/*'jquery-ui' {
			defaultBundle 'core'
			resource id:'js', url:[dir:'plugins',file:'jquery-ui-1.8.15/jquery-ui/js/jquery-ui-1.8.15.custom.min.js'],
					nominify: true, disposition: 'defer'
					
		}

		'jquery-theme' {
			resource id:'theme',
					url:[dir: 'css',
						file:'jquery-ui.css'],
					attrs:[media:'screen, projection']
		
        }*/

	}

	core {
		dependsOn 'jquery, jquery-ui,carousel,leaflet'
		defaultBundle 'core'

		resource url:'/bootstrap/css/bootstrap.min.css'
		resource url:'/css/bootstrap-combobox.css'
		resource url:'/js/jquery/jquery.jqGrid-4.1.2/css/ui.jqgrid.css'
		resource url:'/css/auth.css'
//		resource url:[dir:'css',file:'spring-security-ui.css',plugin:'spring-security-ui']
//		resource url:[dir:'css',file:'jquery.safari-checkbox.css',plugin:'spring-security-ui']
//		resource url:'/css/text.css'
		resource url:'/css/navigation.css'
//		resource url:'/css/jquery.rating.css'
		resource url:'/css/daterangepicker.css'
		resource url:'/css/speciesGroups.css'
		resource url:'/css/habitats.css'
		resource url:'/css/tableSorter.css'
		resource url:'/css/bootstrap-editable.css'
		resource url:'/css/biodiv.css'
		resource url:"/css/${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.app.siteCode}.css"


		resource url:'/bootstrap/js/bootstrap.min.js'
		resource url:'/js/species/main.js'
		resource url:'/js/species/util.js'
		resource url:'/js/readmore/readmore.js'
		resource url:'/js/jquery/jquery.form.js'
//		resource url:'/js/jquery/jquery.rating.js'
		resource url:'/js/jquery/jquery.raty.js'
		resource url:'/js/species/rating.js'
		resource url:'/js/jquery/jquery.cookie.js'
		//resource url:'/js/jquery/jquery.checkbox.js'
		resource url:'/js/jquery/jquery.ellipses.js'
		resource url:'/js/species/popuplib.js', disposition: 'head'
		resource url:'/js/species/ajaxLogin.js'
		resource url:'/js/species/users.js'
		resource url:'/js/jquery/jquery.linkify-1.0.js'
		resource url:'/js/timeago.js'
		//resource url:'/js/species/ajaxLogin.js'
		resource url:[dir:'js',file:'jquery/jquery.checkbox.js',plugin:'spring-security-ui']
		resource url:[dir:'js',file:'spring-security-ui.js', plugin:'spring-security-ui']
		resource url:'/js/jquery/trunk8.min.js'
		resource url:'/js/species/membership.js'
		resource url:'/js/date.js'
		resource url:'/js/daterangepicker.js'
		resource url:'/js/stats.js'
		resource url:'/js/jquery.tablesorter.js'
		resource url:'/js/bootstrap-editable.min.js'
		resource url:'/js/species/posting.js'
        resource url:'/js/feature.js'
        resource url:'/js/flag.js'
	}

	auth {
		dependsOn 'core'
	}

	gallery {
//		resource url:[dir:'js/galleria/1.2.7/themes/classic/',file:'galleria.classic.css']
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

	location_utils { 
        resource url:'/js/location/google/markerclusterer.js'		 
    }

	observations {
		dependsOn 'core, tagit'
		defaultBundle 'core'

		resource url:'/js/species/names.js'
        resource url:'/css/location_picker.css'
        resource url:'/js/location/location-picker.js'
		resource url:'/js/jquery/jquery.watermark.min.js'
		resource url:'/js/jsrender.js'
		//resource url:'/js/bootstrap-typeahead.js'
		resource url:'/js/bootstrap-combobox.js'
		resource url:'/js/species/observations/map.js'
	}

	observations_show {
		dependsOn 'observations, gallery, carousel, comment, activityfeed'

		resource url:'/js/species/observations/show.js'
		resource url:'/js/jquery/jquery.sparkline.min.js'
	} 

	observations_create {
		dependsOn 'observations'

		resource url:'/css/create.css'
		resource url:'/js/jquery/jquery.exif.js'
		resource url:'/js/species/observations/create.js'
		resource url:'/js/jquery/jquery.tmpl.min.js'
	}

	observations_list { 
		dependsOn 'observations, list_utils, comment, activityfeed'
		
        resource url:'/js/species/observations/list.js'
	}

	susers_list { 
		dependsOn 'core, list_utils'
	
        resource url:'/css/location_picker.css'
        resource url:'/js/location/location-picker.js'
	
		resource url:'/js/species/observations/list.js'
		//resource url:'/js/species/users/list.js'
	}

	species {
		dependsOn 'core, list_utils, tagit'

		resource url:'/css/960.css'
		resource url:'/css/main.css'
		resource url:'/css/biodiv.css'

		resource url:'/js/species/species.js'

	}

	species_show {
		dependsOn 'species, gallery, comment, activityfeed'

		resource url:'/css/augmented-maps.css'
		resource url:[dir:'js/jquery/jquery.jqGrid-4.1.2/css',file:'ui.jqgrid.css']
		resource url:'/css/bootstrap-wysihtml5-0.0.2.css'
		resource url:'/css/jquery.tocify.css'
		
		resource url:'/js/jquery/jquery.jqGrid-4.1.2/js/i18n/grid.locale-en.js'
		resource url:'/js/jquery/jquery.jqGrid-4.1.2/js/jquery.jqGrid.src.js'
		resource url:'/js/jquery/jquery.tocify.min.js'
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
	
	slickgrid {
		resource url:'/js/SlickGrid-2.0.2/slick.grid.css'
		resource url:'/js/SlickGrid-2.0.2/css/smoothness/jquery-ui-1.8.16.custom.css'
		resource url:'/js/SlickGrid-2.0.2/examples/examples.css'
		resource url:'/js/SlickGrid-2.0.2/plugins/slick.headerbuttons.css'
		resource url:'/js/SlickGrid-2.0.2/plugins/slick.headermenu.css'


		resource url:'/js/SlickGrid-2.0.2/lib/jquery.event.drag-2.0.min.js'
		resource url:'/js/SlickGrid-2.0.2/slick.core.js'
		resource url:'/js/SlickGrid-2.0.2/slick.formatters.js'
		resource url:'/js/SlickGrid-2.0.2/slick.editors.js'
		resource url:'/js/SlickGrid-2.0.2/slick.grid.js'
		resource url:'/js/SlickGrid-2.0.2/plugins/slick.headerbuttons.js'
		resource url:'/js/SlickGrid-2.0.2/plugins/slick.headermenu.js'
		resource url:'/js/SlickGrid-2.0.2/plugins/slick.cellrangedecorator.js'
		resource url:'/js/SlickGrid-2.0.2/plugins/slick.cellrangeselector.js'
		resource url:'/js/SlickGrid-2.0.2/plugins/slick.cellselectionmodel.js'
	}
	
	checklist {
		dependsOn 'location_utils, list_utils, tagit, comment, activityfeed'

		resource url:'/js/bootstrap-rowlink.min.css'
		resource url:'/js/species/checklist.js'
		resource url:'/js/bootstrap-rowlink.min.js'
		resource url:'/js/location/location-picker.js'
		resource url:'/js/species/observations/map.js'
	}
	
    checklist_list {
		dependsOn 'checklist'

		resource url:'/js/species/observations/list.js'
    }

	checklist_create {
		dependsOn 'observations_create, checklist, slickgrid, add_file'
		
		resource url:'/js/species/parseUtil.js'
		resource url:'/js/species/jquery.csv-0.71.min.js'
	}
	
	chart {
		dependsOn 'core'
		
		resource url:'/js/chart.js'
	}

	add_file {
		dependsOn 'core, tagit, list_utils'
		
		resource url:'/css/content.css'
		resource url:'/js/content.js'
		resource url:'/css/location_picker.css'
		resource url:'/js/location/location-picker.js'
	}
	
	
	content_view {
		dependsOn 'core,  tagit'
		resource url:'/css/main.css'
		
		resource url:'/css/content.css'
		resource url:'/js/content.js'
		
	}

    prettyPhoto {
        resource url:'/css/prettyPhoto.css'
		resource url:'/js/jquery/jquery.prettyPhoto.js'
    }

    leaflet {
        resource url:'js/Leaflet/dist/leaflet.css'
        resource url:'js/Leaflet/dist/leaflet.ie.css', wrapper: { s -> "<!--[if IE]>$s<![endif]-->" }
        resource url:'js/Leaflet/dist/leaflet.js'
        resource url:'js/Leaflet/plugins/leaflet-plugins/layer/tile/Google.js'
        resource url:'js/Leaflet/plugins/Leaflet.Coordinates/dist/Leaflet.Coordinates-0.1.1.css'
        resource url:'js/Leaflet/plugins/Leaflet.Coordinates/dist/Leaflet.Coordinates-0.1.1.min.js'
        resource url:'js/Leaflet/plugins/Leaflet.label/dist/leaflet.label.css'
        resource url:'js/Leaflet/plugins/Leaflet.label/dist/leaflet.label.js'
        resource url:'js/Leaflet/plugins/leaflet-locatecontrol/src/L.Control.Locate.css'
        resource url:'js/Leaflet/plugins/leaflet-locatecontrol/src/L.Control.Locate.ie.css', wrapper: { s -> "<!--[if IE]>$s<![endif]-->" }
        resource url:'js/Leaflet/plugins/leaflet-locatecontrol/src/L.Control.Locate.js'
        resource url:'js/Leaflet/plugins/Leaflet.awesome-markers/dist/leaflet.awesome-markers.css'
        resource url:'js/Leaflet/plugins/Leaflet.awesome-markers/dist/leaflet.awesome-markers.min.js'
        resource url:'js/Leaflet/plugins/leaflet.fullscreen/Control.FullScreen.js'
        resource url:'js/Leaflet/plugins/leaflet.fullscreen/Control.FullScreen.css'
        resource url:'js/Leaflet/plugins/Leaflet.draw/dist/leaflet.draw.css'
        resource url:'js/Leaflet/plugins/Leaflet.draw/dist/leaflet.draw.ie.css', wrapper: { s -> "<!--[if IE]>$s<![endif]-->" }
        resource url:'js/Leaflet/plugins/Leaflet.draw/dist/leaflet.draw.js'
        resource url:'js/Wicket/wicket.js'
        resource url:'js/Wicket/wicket-leaflet.js'
        resource url:'js/Leaflet/plugins/Leaflet.markercluster/dist/leaflet.markercluster-src.js'
        resource url:'js/Leaflet/plugins/Leaflet.markercluster/dist/MarkerCluster.css'
        resource url:'js/Leaflet/plugins/Leaflet.markercluster/dist/MarkerCluster.Default.css'
        resource url:'js/Leaflet/plugins/Leaflet.markercluster/dist/MarkerCluster.Default.ie.css', wrapper: { s -> "<!--[if IE]>$s<![endif]-->" }
 
    }

    images{
//        resource url:"${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.grails.serverURL}/images/spinner.gif", attrs:[type:'gif', width='20', height='20', alt='Loading ...'], disposition:'inline'

    }
}
