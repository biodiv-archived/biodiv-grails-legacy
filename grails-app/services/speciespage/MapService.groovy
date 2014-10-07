package speciespage


import java.io.IOException;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import groovy.sql.Sql
import java.sql.Driver

class MapService {
	def grailsApplication;
   
    public static String FIELD_SEP = "///";

     private Sql getConnection() throws SQLException {
       // return dataSourceibp.getConnection();
        def db = [url:grailsApplication.config.speciesPortal.ibpmapdatabase.url, user:grailsApplication.config.speciesPortal.ibpmapdatabase.username, password:grailsApplication.config.speciesPortal.ibpmapdatabase.password, driver:grailsApplication.config.speciesPortal.ibpmapdatabase.driver];
        def driver = Class.forName(db.driver).newInstance() as Driver;
        
        def props = new Properties()
        props.setProperty("user", db.user)
        props.setProperty("password", db.password)
        def conn = driver.connect(db.url, props)
        def sql = new Sql(conn);
        
      return sql;
    }    

    /**
     *  get column description (the display title for column) for 
     *  a column name in the layer table
     */
    public getColumnTitle( request,  response)
    {     
    		Sql sql; 
            String layer = request["layer"];
            String columnName = request["columnName"];

            if (layer == null || columnName == null)
                return;

                sql = getConnection();
                if (sql == null)
                    return;

             sql.query("select col_description((select oid from pg_class where relname = '" + layer + "'), (select ordinal_position from information_schema.columns where table_name='" + layer + "' and column_name='" + columnName + "'))") { 
				ResultSet rs ->
					response = "";
			     while (rs.next()) {
			     	response += rs.getString(1);
			     }
			     response += "}";
			}
			sql.close();
		return response;	
    } 
       
    /**
     * get summary columns for given layer
     */
    public getSummaryColumns( request,  response)
    {

            Sql sql = null;        
            String layer = request["layer"];
            if (layer == null)
                return; 
                sql = getConnection();

            if (sql == null)
                return;
        	sql.query("select summary_columns from \"Meta_Layer\" where layer_tablename = '" +layer +"'") { 
        		ResultSet rs ->
			     while (rs.next()) {					     	
			     	response = "["+rs.getString(1)+"]";		                    
                }
			}
			sql.close();
    	return response;
    }

    /**
     * get column name and column title mapping
     */
    public getLayerColumns( request,  response)
    {
            
            Sql sql = null;  
            String layer = request["layer"];             
            if (layer == null)
                return;
           
                sql = getConnection();
                if (sql == null)
                    return; 

                sql.query("select column_name, col_description((select oid from pg_class where relname = '" + layer + "'), ordinal_position) from information_schema.columns where table_name='" + layer + "'") {  
                		ResultSet rs ->				    

                		response = "{";
					     while (rs.next()) {
					     	response += "'";
					     	response += rs.getString(1);
					     	response += "'";
					     	response += ":";
					     	response += "'";

					     	if (rs.getString(2) != null)
					     		response += rs.getString(2);
					     	else
					     		response += rs.getString(1);	

					     	response += "'";	

					     	if (!rs.isLast())
                        		response += ",";
					     }
					     response += "}";
					}
					sql.close();
            return response;

    }

    /**
     * get access information for all layers
     */
    public getLayersAccessStatus( request, response)
    {
            
    	Sql sql;
        sql = getConnection();

        if (sql == null)
            return;

        sql.query("select layer_tablename, access from \"Meta_Layer\"") { 
        		ResultSet rs ->

        			response = "{";
			     while (rs.next()) {
			     	response += rs.getString(1);
			     	response += ":"
			     	response += rs.getString(2);

			     	if (!rs.isLast())
                		response += ",";

			     }
			     response += "}";
			}
			sql.close();	     
		return response;
    }

    /**
     * get themes names for theme_type
     *
     * theme_type is 1 from Themes and 2 for Geography
     */
    public getThemeNames(request, response)
    {
        	Sql sql;
            String theme_type = request["theme_type"];

            if (theme_type == null)
                return;

                sql = getConnection();

                if (sql == null)
                    return;

            sql.query("select theme_name from \"Theme\" where theme_type=" + theme_type) { 
				ResultSet rs ->
					response = "";
			     while (rs.next()) {
			     	response += rs.getString(1);
			     	response += FIELD_SEP
			     }
			     
			}
			sql.close();	   
		return response;

    }

    /**
     * get all layers for given theme
     */
    public getLayersByTheme( request,  response)
    {            
        Sql sql; 
        String theme = request["theme"];
        if (theme == null)
        	return;
        sql = getConnection();
        if (sql == null)
            return;
        sql.query("select (select layer_tablename from \"Meta_Layer\" where layer_id=tlm.layer_id) from \"Theme_Layer_Mapping\" as tlm where theme_id=(select theme_id from \"Theme\" where theme_name='" + theme + "')"){
        	ResultSet rs ->
        		response =""
	        while (rs.next()) {
	            response +=rs.getString(1);
	            response +=FIELD_SEP;
	        }
    	}
    	sql.close();
        return response;

    }

    /**
     * get attribution for given layer
     */
    public getLayerAttribution( request,  response)
    {
    
        	Sql sql;
            String layer = request["layer"];

            if (layer == null)
                return;

            sql = getConnection();

            if (sql == null)
                return;

            sql.query("select license, attribution from \"Meta_Layer\" where layer_tablename = '" + layer + "'") { 
				ResultSet rs ->		
				response = "";			
			     while (rs.next()) {
			     	response += rs.getString(1);
			     	response += FIELD_SEP
			     	response += rs.getString(2);			     	
			     }			     
			}
			sql.close();	
		return response;	  

    }

    /**
     * get layer details
     */
    public getLayerDetails(request, response)
    {
			Sql sql;            
            String layer = request["layer"];
            if (layer == null)
                return;
            sql = getConnection();
            if (sql == null)
                return;

			sql.query("select title, body from node_revisions where nid=(select m.nid from \"Meta_Layer\" as m where m.layer_tablename='" + layer + "') order by vid desc limit 1") { 
				ResultSet rs ->
					response = ""
			     while (rs.next()) {
			     	response += "<span class='layer_details_title'>";
			     	response += rs.getString(1);
			     	response += "</span>"
			     	response += rs.getString(2);
			     }
			    
			}
			sql.close();
			return response;
	}

    /**
     * get layer summary in json
     */
    public  getLayerSummary( request,  response)
    {
            Sql sql;        
            String layer = request["layer"];

            if (layer == null)
                return;
           
                sql = getConnection();

                if (sql == null)
                    return;
			sql.query("select layer_name, layer_description, status, pdf_link, url, comments, created_by, created_date, modified_by, modified_date from \"Meta_Layer\" where layer_tablename=\'" + layer + "\'") { 
				ResultSet rs ->

					response = "{";
			     while (rs.next()) {
			     		ResultSetMetaData rsmetadata = rs.getMetaData();
			     		def column_count = rsmetadata.columnCount;
			     	for (int i = 1; i <= column_count; ++i) {
			                String columnName = rsmetadata.getColumnName(i);
			                response += columnName + ":'";
			                if (rs.getString(i) != null)
			                    response +=rs.getString(i);
			                	response +="'";
			                
			                if (i < column_count)
			                    response += ",";
			            }
			     }
			     response += "}";
			}
			sql.close();
	return response;	  
}



    /**
     * get layer link tables
     */
    public  getLayerLinkTables( request,  response)
    {  
    		Sql sql;
            String layer = request.layer;

            if (layer == null)
                return;

                sql = getConnection();
                if (sql == null)
                    return;

                 sql.query("select mlt.link_tablename, mlt.link_name from \"Meta_LinkTable\" mlt join \"Meta_Layer\" ml on mlt.layer_id = ml.layer_id and ml.layer_tablename = \'" + layer + "\'") { 
					ResultSet rs ->
						response = "{";
				     while (rs.next()) {
				     	response += rs.getString(1);
				     	response += ":'"
				     	response += rs.getString(2);
				     	response += "'"

				     	if (!rs.isLast())
				    		response += ",";

				     }
				     response += "}";
				}
				sql.close();
			return 	response;
    }

    /**
     * get number of occurrences of a species
     */
    public getOccurrenceCount(request, response)
    {
			Sql sql;        
            String species_name = request["species_name"];

            if (species_name == null)
                return;

                sql = getConnection();

                if (sql == null)
                    return;
              
                sql.query("select count(species_name) from occurrence where species_name='" +  species_name + "'") { 
					ResultSet rs ->
                response ="getOccurrenceCount({count:";
                while (rs.next()) {
                    response += rs.getString(1);
                 }
                response += "});";
			}
			sql.close();
		return response;
    }
}
