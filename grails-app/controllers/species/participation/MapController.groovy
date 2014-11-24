package species.participation

import grails.plugin.springsecurity.annotation.Secured
import grails.converters.JSON
import java.util.List;

class MapController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def mapService;

    def index = {
        redirect(action: "show", params: params)
    }

    def show = {
    }
/*
    def getLinkTableEntries = {
        String msg = '';
        if (params['layer_tablename'] && params['layerdata_id'] && params['link_tablename']) {
            String layer_tablename = params['layer_tablename'];
            def row_id = params['layerdata_id'];
            String link_tablename = params['link_tablename'];
        }
        else {
            msg = 'Required parameters are not set';
            status = 'error'
        }
        get_link_table_entries($layer_tablename, $row_id, $link_tablename);
        break;
    }

    private def get_link_table_entries(String layer_tablename, long row_id, String link_tablename) {
        def arr_result = [];
        def column_names = [];
        def arr_mlocate = [];
        int r=1;

        int participation_type = getLayerParticipationType(layer_tablename);

        Map table_info = GetTableColsOfType(link_tablename, 'link', 'italics');
        def italics_columns = table_info['italics_columns'];

        String query="select description, linked_column, layer_column from \"Meta_LinkTable\" where link_tablename = '%s'";
        def result=db_query($query, $link_tablename);
        if(!result) {
            //Error occured
            arr_result['error']='Error fetching data from DB';
        }
        else {

                def obj=db_fetch_object(result);
                String link_description = obj.description;
                $linked_column=str_replace("'", "", $obj->linked_column);
                $layer_column=str_replace("'", "", $obj->layer_column);

                $linked_value= - 1;

                $col_info=getDBColDesc($link_tablename);

                $global_resource_mapping=getResourceTableMapping($link_tablename);

                $query='SELECT "%s".* FROM "%s", "%s" where "%s"."%s" = "%s"."%s" and "%s".' . AUTO_DBCOL_PREFIX . 'id = %d';
                $query_args=array($link_tablename, $link_tablename, $layer_tablename, $link_tablename, $linked_column, $layer_tablename, $layer_column, $layer_tablename, $row_id);
                $result=db_query($query, $query_args);
                if(!$result) {
                    //Error occured
                    $arr_result['error']='Error fetching links';
                }
                else {
                    $arr_result['description']=($metalink[$link]['description'] == "" ? '' : $metalink[$link]['description']);

                    foreach($col_info as $key=>$val) {
                        if(substr($key, 0, strlen(AUTO_DBCOL_PREFIX)) != AUTO_DBCOL_PREFIX) {
                            $column_names[$key]=($col_info[$key] == "" ? str_replace(" ", "&nbsp;", $key) : str_replace(" ", "&nbsp;", $col_info[$key]));
                        }
                    }
                    if($participation_type == 1 || $participation_type == 2 || $participation_type == 3) {
                        $column_names['created_by']='Created By';
                        $column_names['created_date']='Created Date';
                        $column_names['modified_by']='Modified By';
                        $column_names['modified_date']='Modified Date';
                    }
                    $arr_result['col_names']=$column_names;

                    $tbody="";
                    $i=0;
                    $r=0;
                    $data=array();
                    while($obj=db_fetch_object($result)) {
                        if($i == 0) {
                            $linked_value=$obj-> {
                                $linked_column
                            };
                            $i+=1;
                        }
                        $row=array();
                        $rw=array();
                        foreach($obj as $key=>$value) {
                            if((($participation_type == 1 || $participation_type == 2 || $participation_type == 3) && ($key == AUTO_DBCOL_PREFIX . "created_by" || $key == AUTO_DBCOL_PREFIX . "created_date" || $key == AUTO_DBCOL_PREFIX . "modified_by" || $key == AUTO_DBCOL_PREFIX . "modified_date"))) {
                                switch($key) {
                                    case AUTO_DBCOL_PREFIX . 'created_by':
                                    $rw['created_by']='<a href="' . $base_path . 'user/' . $value . '" target="_blank">' . getUserName($value) . '</a>';
                                    break;

                                    case AUTO_DBCOL_PREFIX . 'modified_by':
                                    $rw['modified_by']='<a href="' . $base_path . 'user/' . $value . '" target="_blank">' . getUserName($value) . '</a>';
                                    break;

                                    case AUTO_DBCOL_PREFIX . 'created_date':
                                    $rw['created_date']=($value == '' ? '&nbsp;' : str_replace(" ", "&nbsp;", $value));
                                    break;

                                    case AUTO_DBCOL_PREFIX . 'modified_date':
                                    $rw['modified_date']=($value == '' ? '&nbsp;' : str_replace(" ", "&nbsp;", $value));
                                    break;
                                }
                            }
                            elseif(substr($key, 0, strlen(AUTO_DBCOL_PREFIX)) != AUTO_DBCOL_PREFIX) {
                                if(array_key_exists($key, $global_resource_mapping)) {
                                    if($value == "") {
                                        $val='&nbsp;';
                                    }
                                    else {
                                        $href="{$base_path}ml_orchestrator.php?action=getResourceTableEntry&resource_tablename=" . urlencode($global_resource_mapping[$key]['resource_tablename']) . "&resource_column=" . urlencode($global_resource_mapping[$key]['resource_column']) . "&value=" . urlencode($value);
                                        $val=($value == '' ? '&nbsp;' : "<a id='a_{$value}' name='" . $global_resource_mapping[$key]['resource_tablename'] . "' href='{$href}' class='jTip1' onClick='javascript:showAjaxLinkPopup(this.href, this.name);return false;'>" . str_replace(" ", "&nbsp;", $value) . "</a>");
                                    }
                                }
                                elseif(strpos($italics_columns, "'{$key}'") !== FALSE) {
                                    $val=($value == '' ? '&nbsp;' : '<i>' . str_replace(" ", "&nbsp;", $value) . '</i>');
                                }
                                else {
                                    $val=($value == '' ? '&nbsp;' : str_replace(" ", "&nbsp;", $value));
                                }
                                $row[$key]=$val;
                            }
                        }
                        $row=array_merge($row, $rw);
                        $data[]=$row;
                        $r++;
                    }
                    $arr_result['data_count']=$r;
                    $arr_result['data']=$data;
                    if($i == 0) {
                        $arr_result['no_record']="Sorry! No records found.";
                    }
                    if(userHasAddLinkedDataPerm($layer_tablename)) {
                        $arr_result['add_linked_data_lnk']=$base_path . 'ml_orchestrator.php?action=getLinkTableSchema&link_tablename=' . $link_tablename . '&linked_column=' . $linked_column . '&linked_value=' . $linked_value;
                    }
                    $arr_result['link_tablename']=$link_tablename;
                    $arr_result['linked_column']=$linked_column;
                    $arr_result['linked_value']=$linked_value;

                    print json_encode($arr_result);
                }
            }
        }

    }

private def get_link_table_entry(String layer_tablename, long row_id) {
    $html="";

    $query="select mlt.link_tablename, mlt.description, mlt.linked_column, mlt.layer_column from \"Meta_LinkTable\" mlt join \"Meta_Layer\" ml on mlt.layer_id = ml.layer_id and ml.layer_tablename = '%s'";
    $result=db_query($query, $layer_tablename);
    if(!$result) {
        //Error occured
        die(return_error('Error fetching data from DB'));
    }
    else {
        $metalink=array();
        $tablenames=array();
        while($obj=db_fetch_object($result)) {
            $tablenames[]=$obj->link_tablename;
            $metalink[$obj->link_tablename]['description']=$obj->description;
            $metalink[$obj->link_tablename]['linked_column']=$obj->linked_column;
            $metalink[$obj->link_tablename]['layer_column']=$obj->layer_column;
        }
        foreach($tablenames as $link) {
            $linked_column=$metalink[$link]['linked_column'];
            $layer_column=$metalink[$link]['layer_column'];

            $col_info=getDBColDesc($link);

            $global_resource_mapping=getResourceTableMapping($link);

            $query='SELECT "%s".* FROM "%s", "%s" where "%s"."%s" = "%s"."%s" and "%s".' . AUTO_DBCOL_PREFIX . 'id = %d';
            $query_args=array($link, $link, $layer_tablename, $link, $linked_column, $layer_tablename, $layer_column, $layer_tablename, $row_id);
            $result=db_query($query, $query_args);
            if(!$result) {
                //Error occured
                die(return_error('Error fetching links'));
            }
            else {
                $html.='<div id="" style=font:arial><b><u>' . $metalink[$link]['description'] . '</b></u><table id="linkedData">';
                $tbody="";
                $i=0;
                while($obj=db_fetch_object($result)) {
                    if($i == 0) {
                        $html.='<thead><tr align=center>';
                        foreach($obj as $key=>$value) {
                            if(array_key_exists($key, $col_info)) {
                                $html.='<th align=center>' . ($col_info[$key] == "" ? $key : $col_info[$key]) . '</th>';
                            }
                            else {
                                $html.='<th align=center>' . $key . '</th>';
                            }
                        }
                        $html.='</tr></thead><tbody>';
                        $i+=1;
                    }

                    $tbody.='<tr align=center>';
                    foreach($obj as $key=>$value) {
                        if(array_key_exists($key, $global_resource_mapping)) {
                            if($value == "") {
                                $tbody.='<td align=center>&nbsp;</td>';
                            }
                            else {
                                $href="{$base_path}ml_orchestrator.php?action=getResourceTableEntry&resource_tablename=" . $global_resource_mapping[$key]['resource_tablename'] . "&resource_column=" . $global_resource_mapping[$key]['resource_column'] . "&value={$value}";
                                $tbody.='<td align=center>' . ($value == '' ? '&nbsp;' : "<a id='a_{$value}' name='" . $global_resource_mapping[$key]['resource_tablename'] . "' href='{$href}' class='jTip1' onClick='javascript:showAjaxLinkPopup(this.href, this.name);return false;'>" . str_replace(" ", "&nbsp;", $value) . "</a>") . '</td>';
                            }
                        }
                        else {
                            $tbody.='<td align=center>' . ($value == '' ? '&nbsp;' : $value) . '</td>';
                        }
                    }
                    $tbody.='</tr>';
                }
                $html.=$tbody . '</tbody></table></div>';
            }
        }
    }
    echo $script . $html;
}

private int getLayerParticipationType(String layer_tablename) {
    int participation_type = 0;
    String query = "select participation_type from \"Meta_Layer\" where layer_tablename = '%s'";
    $result = db_query($query, $layer_tablename);
    if(!$result) {
    } else {
        $obj = db_fetch_object($result);
        participation_type = obj.participation_type;
    }
    return participation_type;
}

function GetTableColsOfType($tablename, $table_type, $col_type, $layer_id=NULL) {
    $table_info=array();
    $Meta_tablename="";
    $table_type=strtolower($table_type);

    if($table_type == 'layer' || $table_type == 'link') {
        if($table_type == 'layer') {
            $Meta_tablename="Meta_Layer";
        }
        elseif($table_type == 'link') {
            $Meta_tablename="Meta_LinkTable";
        }
        if($tablename == "") {
            $query='select %s_tablename, %s_columns from "%s" where layer_id = %d';
            $query_args=array($table_type, $col_type, $Meta_tablename, $layer_id);
            $result=db_fetch_array(db_query($query, $query_args));
            $table_info['layer_id']=$layer_id;
            $table_info["{$table_type}_tablename"]=$result["{$table_type}_tablename"];
        }
        else {
            $query="select layer_id, %s_columns from \"%s\" where %s_tablename = '%s'";
            $query_args=array($col_type, $Meta_tablename, $table_type, $tablename);
            $result=db_fetch_array(db_query($query, $query_args));
            $table_info['layer_id']=$result['layer_id'];
            $table_info["{$table_type}_tablename"]=$tablename;
        }
        $table_info["{$col_type}_columns"]=$result["{$col_type}_columns"];
    }
    elseif($table_type == 'resource') {
        $Meta_tablename="Meta_Global_Resource";
        $query="select %s_columns from \"%s\" where %s_tablename = '%s'";
        $query_args=array($col_type, $Meta_tablename, $table_type, $tablename);
        $result=db_fetch_array(db_query($query, $query_args));
        $table_info["{$table_type}_tablename"]=$tablename;
        $table_info["{$col_type}_columns"]=$result["{$col_type}_columns"];
    }

    return $table_info;
}
*/


public def getLayerColumns(){
    render mapService.getLayerColumns(params, response);
}


public def getSummaryColumns(){
    render mapService.getSummaryColumns(params, response);
}

public def getLayersAccessStatus(){
    render mapService.getLayersAccessStatus(params, response);    
}


public def getLayerLinkTables(){
    render mapService.getLayerLinkTables(params, response);    
}


public def getLayersByTheme(){
    render mapService.getLayersByTheme(params, response);    
}

public def getLayerSummary(){
    render mapService.getLayerSummary(params, response);    
}

public def getLayerDetails(){
    render mapService.getLayerDetails(params, response);    
}

public def getLayerAttribution(){
    render mapService.getLayerAttribution(params, response);
}

}
