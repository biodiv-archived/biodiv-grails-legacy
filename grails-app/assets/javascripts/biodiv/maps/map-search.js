
function uniquify(arr) {
    var new_arr = new Array();

    outerloop: for (var i=0; i<arr.length; i += 1){
        for (var j=0; j<new_arr.length; j += 1){
            if(new_arr[j] === arr[i]){
                continue outerloop;
            } 
        }
        new_arr.push(arr[i]);
    }

    return new_arr;
}

function process_highlights(highlighting){
    for (var key in highlighting) {
        if (!highlighting.hasOwnProperty(key)) {
            continue;
        }
        
        highlighting[key] = uniquify(highlighting[key]);
    }

    return highlighting;
}

function process_search_result(data){

    processed_result = {};

    if (data === undefined)
        return processed_result;

    layer2features = {};
    layer2desc = {};
    layer2highlighting = {};
    layer2search_columns = {};

    response = data.response;
    highlighting = data.highlighting;

    numFound = response.numFound;
    docs = response.docs;

    processed_result.numFound = numFound;

    var i;
    for ( i=0; i<numFound; i +=1 ){
        if (layer2features[docs[i].layer] !== undefined) {
            layer2features[docs[i].layer].push(docs[i].id);
        } else {
            layer2features[docs[i].layer] = [docs[i].id];
        }
       
        var highlight = highlighting[docs[i].id].all.pop();

        if(highlight !== undefined){
            if (layer2highlighting[docs[i].layer] !== undefined) {
                layer2highlighting[docs[i].layer].push(highlight);
            } else {
                layer2highlighting[docs[i].layer] = [highlight];
            }
        }

        layer2desc[docs[i].layer] = [docs[i].layer_name, docs[i].layer_description];

        layer2search_columns[docs[i].layer] = docs[i].search_columns;

        if (numFound > 2000)
            numFound = 2000;
    }


    processed_result.features = layer2features;
    processed_result.desc = layer2desc;
    processed_result.highlighting = process_highlights(layer2highlighting);
    processed_result.search_columns = layer2search_columns;

    return processed_result;
}

function keyCount(dict){
    var count = 0;
    for (var k in dict) {
        if (dict.hasOwnProperty(k)) {
            ++count;
        }
    }

    return count;
}

function create_cql_filter(search_columns, query_string) {
    var cols = search_columns.split(":");

    var cql_filters = [];

    for (var i=0; i<cols.length; ++i){
        var cql_filter = cols[i] + ' like \\x27%' + query_string + '%\\x27';    
        cql_filters.push(cql_filter);
    }

    return cql_filters.join(' or ');
}

function display_results(search_results, search_results_div, type, query_string){

    function initialize(layers) {
        var map = window.map;
        var i;
        for (i = 0; i < layers.length; i += 1) {
            if (hasLayer(map, layers[i])) {
                setElementVisible(layers[i] + '_a_add_search', false);
                setElementVisible(layers[i] + '_a_remove_search', true);
                setElementVisible(layers[i] + '_zoom_to_extent_search', true);
            } else {
                setElementVisible(layers[i] + '_a_add_search', true);
                setElementVisible(layers[i] + '_a_remove_search', false);
                setElementVisible(layers[i] + '_zoom_to_extent_search', false);
            }
        }
    }

    if (type === undefined)
        type = 'layers';

    var html = '';

    var layers = [];
    layer2features = search_results.features;
    layer2desc = search_results.desc;
    layer2highlighting = search_results.highlighting;
    layer2search_columns = search_results.search_columns;

    var resultsCount = 0;

    if (type === 'layers')
        resultsCount = keyCount(layer2features);
    else if (type === 'occurrences')
        resultsCount = search_results.numFound;

    html = html + '<div class="info_box" style="margin:10px;">Found <span style="font-style:italic;">' + resultsCount + '</span> ' + type + '</div>';

    for (var key in layer2features) {
        if (!layer2features.hasOwnProperty(key)) {
            continue;
        }

        query_string = query_string.replace(/\*/g, '');
        query_string = query_string.replace(/"/g, '');

        if ( key === 'occurrence'){
            //cql_filter = "id in \(" + layer2features[key].join() + "\)";
            cql_filter = "species_name like \\x27%" + query_string + "%\\x27";
        }else{
            //cql_filter = "__mlocate__id in \(" + layer2features[key].join() + "\)";
            cql_filter = create_cql_filter(layer2search_columns[key], query_string);
        }

        layer = getWorkspace() + ":" + key;
        layers.push(layer);

        var metadata = getLayerMetaData(key);

        html = html + '<ul>';
        html = html + '<li class="layer_in_search_results_panel">';
        html = html + '<div class="search_snippet">';
        html = html + '<span class="feature_title">' + metadata.title + '</span><br>';
        html = html + '<span class="abstrct">' + metadata.abstrct + '</span>';
 
        html = html + '</div>';

        html = html + '<div class="search_highlights">' + layer2highlighting[key].join(" ... ") + '</div>'; 


        html = html + getMapImage(layer, cql_filter);
        html = html + '<ul class="layer_options">';
        html = html + '<li class="add_to_map" id="' +
                layer +
                '_a_add_search" href="#" onclick="addFilteredLayer(\'' +
                layer + '\', \'' +
                metadata.title + '\', \'' +
                cql_filter +
                '\');">add to map</li>';
        
        html = html + '<li class="remove_from_map" id="' +
                layer +
                '_a_remove_search" href="#" onclick="removeLayer(\'' +
                layer +
                '\');">remove from map</li>';


        html = html + '</ul>';
        html = html + '</li>'
        html = html + '</ul>';
    }

    document.getElementById(search_results_div).innerHTML = html;
    initialize(layers);


}

function get_selected_item(radio_group) {

    var chosen = "";
    var len = radio_group.length;

    for (var i = 0; i<len; i++) {
        if (radio_group[i].checked) {
                chosen = radio_group[i].value;
            }
        }
    
    return chosen;
}

function search(query_string, search_results_div, radio_group){
    

    var type = (radio_group !== undefined) ? get_selected_item(radio_group) : 'all';
    if (type === 'all'){
        return search_all(query_string, search_results_div);
    } else if (type === 'occurrence'){
        return search_occurrence(query_string, search_results_div);
    }
}

// method to request solr search server with search query
function search_all(query_string, search_results_div){
    var url = 'http://' + getHost() + '/wgp_maps/search?wt=json&start=0&rows=2000&hl=on&q=' + query_string;
    
    var params = {
        q: query_string
    };

    var search_results;
    $.ajax({
        url: url,
        type: 'GET',
        async: false,
        cache: false,
        timeout: 30000,
        dataType: 'text',
        error: function(){
            return true;
        },
        success: function(data, msg){
            d = eval('(' + data + ')');
            search_results = process_search_result(d);
        }
    });

    display_results(search_results, search_results_div, undefined, query_string);

    bakeCookie("last_search_query", query_string, 7);
    
    return false;
}

// method to request solr search server with search query
function search_occurrence(query_string, search_results_div){
    var url = 'http://' + getHost() + '/wgp_maps/search?wt=json&start=0&rows=2000&hl=on&q=occurrence_species:' + query_string;
    
    var params = {
        q: query_string
    };

    var search_results;
    $.ajax({
        url: url,
        type: 'GET',
        async: false,
        cache: false,
        timeout: 30000,
        dataType: 'text',
        error: function(){
            return true;
        },
        success: function(data, msg){
            d = eval('(' + data + ')');
            search_results = process_search_result(d);
        }
    });

    display_results(search_results, search_results_div, 'occurrences', query_string);

    bakeCookie("last_search_query", query_string, 7);
    
    return false;
}


// to add search box
function addSearchBox(search_box_div) {
    var html = '<div>';
    //html = html + '<form id="search_form" name="search_form" action="javascript:" onsubmit="search(q.value, \'search_results_panel\')">';
    //html = html + '<script> $(\'#search_form\').submit(function () { alert("test2"); return false; });</script>';
    html = html + '<form id="search_form" name="search_form" action="javascript:" onsubmit="search(q.value, \'search_results_panel\', g)">';
    //html = html + '<form id="search_form" name="search_form" action="javascript:">';
    
    var last_query = eatCookie("last_search_query")||'';

    html = html + '<input id="map_search_text_field" type="text" name="q" size="35" value="' + last_query + '"/>';
    html = html + '<input type="submit" value="'+window.i8ln.observation.maps.msearch+'" /><br>';
    html = html + '<input type="radio" name="g" value="all" checked>'+window.i8ln.observation.maps.mrecord+'';
    html = html + '<input type="radio" name="g" value="occurrence">'+window.i8ln.observation.maps.moccur+'';
 
    html = html + '</form>';
    html = html + '</div>';
    
    document.getElementById(search_box_div).innerHTML = html;
}

function addSearchResultsPanel(search_results_panel_div) {
    var html = '';

    document.getElementById(search_results_panel_div).innerHTML = html;
}

function get_suggestions(response){

    var spellcheck = response.spellcheck;
    var suggestions = spellcheck.suggestions;

    return suggestions[1].suggestion
}

