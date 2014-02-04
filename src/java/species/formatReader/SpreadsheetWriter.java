package species.formatReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DataFormatter;

public class SpreadsheetWriter {

    static final String keyValueSep = "#11#";
    static final String columnSep = "#12#";
    
    public static void writeSpreadsheet(File f, InputStream inp, JSONArray gridData, Map headerMarkers) {
        System.out.println ("params in write SPREADSHEET " + gridData + " ----- " + headerMarkers);
        try {
            Workbook wb = WorkbookFactory.create(inp);
            int sheetNo = 0;
            writeDataInSheet(wb, gridData, sheetNo);
            writeHeadersInFormat(wb, headerMarkers);
            FileOutputStream out = new FileOutputStream(f);
            wb.write(out);
            out.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }

    public static void writeDataInSheet(Workbook wb, JSONArray gridData, int sheetNo) {
        Sheet sheet = wb.getSheetAt(sheetNo);
        Iterator<Row> rowIterator = sheet.iterator();
        int index = 0;
        int i = 0;
        boolean headerRow = true;
        System.out.println("===JSON ARRAY LENGTH==============");
        System.out.println(gridData.length());
        int gDataSize = gridData.length();
        JSONObject rowData = gridData.getJSONObject(index);
        Iterator<String> keys = rowData.keys();
        int numKeys = 0;
        while(keys.hasNext()){
            System.out.println("HERE " + (String)keys.next());
            numKeys++;
        }
        System.out.println("==NUM KEYS== " + numKeys);
        String[] keysArray = new String[numKeys];
        int z = 0;
        keys = rowData.keys();
        while (keys.hasNext()) {
            //System.out.println()
            keysArray[z] = (String)keys.next();
            z++;
        }
        //rowIterator.hasNext();
        Row row = rowIterator.next();

        //while( keys.hasNext() )
        for(int a = 0; a < numKeys; a++){
            //String key = (String)keys.next();
            Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
            System.out.println("======PRINTING THIS TO HEADER CELL===== " + keysArray[a]);
            cell.setCellValue(keysArray[a]);
            i++;
        }
        int lastHeaderCellNum = row.getLastCellNum();
        for(int j = i; j <= lastHeaderCellNum; j++) {
            Cell cell = row.getCell(j, Row.CREATE_NULL_AS_BLANK);
            cell.setCellValue("");
        }

        for (int k = 0; k < gDataSize; k++) {
            System.out.println("REACHED FOR LOOP");
            rowData = gridData.getJSONObject(index);
            //mapRow.put(gridData.get(count));
            //rowIterator.hasNext();
            //
            if(rowIterator.hasNext()){
                row = rowIterator.next();
            }
            else{
                row = sheet.createRow(k+1);
                for(int a = 0; a < numKeys; a++){
                    Cell cell = row.createCell(a);
                }
            }
            i = 0;
            System.out.println("============ "); 
            System.out.println(gridData);

            //for ( Map.Entry<String, String> entry : mapRow.entrySet()) {
            //while( keys.hasNext() ){
            for(int a = 0; a < numKeys; a++){
                //String key = (String)keys.next();
                Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
                System.out.println ("=====PRINTING THIS TO NORMAL CELL====== " + rowData.getString(keysArray[a]));
                cell.setCellValue(rowData.getString(keysArray[a]));
                i++;
            }  
            index++;
            headerRow = false;
            // rest cells in that row overwritten with empty string
            int lastCellNum = row.getLastCellNum();
            for(int j = i; j <= lastCellNum; j++) {
                Cell cell = row.getCell(j, Row.CREATE_NULL_AS_BLANK);
                cell.setCellValue("");
            }
        }
        //overwrite rest row data in sheet
        while(rowIterator.hasNext()) {
            row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) { 
                Cell cell = cellIterator.next();
                cell.setCellValue("");
            }
        }
        return;
    }
    
    public static void writeHeadersInFormat(Workbook wb, Map<String, Map<String,String> > headerMarkers) {
        System.out.println("=CLASS===="+headerMarkers.getClass());
        Object o = headerMarkers.remove("undefined");
        Sheet sheet = wb.getSheet("headerMetadata");
        if(sheet != null) {
            int sindex = wb.getSheetIndex(sheet);
            wb.removeSheetAt(sindex);
        }
        sheet = wb.createSheet("headerMetadata");
        Map <String, Map<String,String>> reverseMarkers = new HashMap();
        int rownum = 0;

        for(Map.Entry<String , Map<String,String> > entry : headerMarkers.entrySet()) {
            String headerName = entry.getKey();
            headerName = headerName.trim().toLowerCase();
            Map<String,String> headerValues = new HashMap();
            for(Map.Entry<String, String> en : entry.getValue().entrySet()) {
                System.out.println("=======HERE======" + en.getKey());
                if(en.getKey() != "undefined"){
                    headerValues.put(en.getKey(), en.getValue());
                }
            }
            System.out.println("==========NEW FUNC==============");
            System.out.println(entry);
            System.out.println("---------" + entry.getValue() + entry.getValue().getClass());
            System.out.println("-------" + headerValues + headerValues.getClass());
            String dataColumns = "";
            if(headerValues.get("dataColumns") != null){
                dataColumns = headerValues.get("dataColumns");
                dataColumns = dataColumns.trim();
            }
            String group = "";
            if(headerValues.get("group") != null){
                group = headerValues.get("group");
                group = group.trim();
            }

            System.out.println("====ERROR FINIDING==" + headerValues.get("group"));
            String includeHeadings = "";
            if(headerValues.get("header") != null){
                includeHeadings = headerValues.get("header");
                includeHeadings = includeHeadings.trim();
            }
            String delimiter = "";
            if(headerValues.get("delimiter") != null){
                delimiter = headerValues.get("delimiter");
                delimiter = delimiter.trim();
            }

            System.out.println("=======" + dataColumns);
            List<String> dcList = Arrays.asList(dataColumns.split(","));
            Iterator<String> dcIterator = dcList.iterator();
            while (dcIterator.hasNext()) {
                String nextVal = dcIterator.next();
                nextVal = nextVal.trim();
                System.out.println("==THIS IS THE VALUE=== " + nextVal);
                if(!nextVal.equals("") && !nextVal.equals(null)){
                    if(reverseMarkers.containsKey(nextVal)){
                        Map<String, String> m = reverseMarkers.get(nextVal);
                        String fieldNames = m.get("fieldNames");
                        if(fieldNames != "") {
                            fieldNames += "," + headerName;
                            m.put("fieldNames", fieldNames);
                        }
                        else {
                            m.put("fieldNames", headerName);
                        }
                        String contentDelimiter = m.get("contentDelimiter");
                        if(contentDelimiter != "") {
                            contentDelimiter += columnSep + headerName + keyValueSep + delimiter;
                            m.put("contentDelimiter", contentDelimiter);
                        }
                        else {
                            m.put("contentDelimiter", headerName + keyValueSep + delimiter);
                        }
                        String contentFormat = m.get("contentFormat");
                        if(contentFormat != "") {
                            contentFormat += columnSep + headerName + keyValueSep + "Group=" + group +";" + "includeHeadings=" + includeHeadings +";";
                            m.put("contentFormat", contentFormat);
                        }
                        else {
                            m.put("contentFormat",  headerName + keyValueSep + "Group=" + group +";" + "includeHeadings=" + includeHeadings +";");
                        }
                    }else {
                        Map<String, String> m1 = new HashMap();
                        m1.put("fieldNames", headerName);
                        m1.put("contentDelimiter", headerName + keyValueSep + delimiter);
                        m1.put("contentFormat",  headerName + keyValueSep + "Group=" + group +";" + "includeHeadings=" + includeHeadings +";");


                        reverseMarkers.put(nextVal, m1);
                    }
                }
            }

        }

        Row row = sheet.createRow(rownum++);
        int numOfColumns = 6;
        String[] headerRowValues = {"CONCEPT", "CATEGORY", "SUBCATEGORY", "FIELD NAME(S)", "CONTENT DELIMITER", "CONTENT FORMAT"};
        for (int cellNum = 0; cellNum < numOfColumns; cellNum++ ){
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(headerRowValues[cellNum]);
        }
        System.out.println("====REVERSE MARKERS=====" + reverseMarkers);
        for(Map.Entry<String , Map<String,String> > entry : reverseMarkers.entrySet()) {
            String[] arr = new String[numOfColumns];
            String headerName = entry.getKey();
            List<String> pipedNameList = Arrays.asList(headerName.split("\\|"));
            Iterator<String> pnlIterator = pipedNameList.iterator();
            for(int i = 0; i < 3; i++){
                if(pnlIterator.hasNext()){
                    arr[i] = pnlIterator.next();
                }
                else{
                    arr[i] = "";
                }
            }
            Map<String,String> m2 = new HashMap(entry.getValue());
            arr[3] = m2.get("fieldNames");
            arr[4] = m2.get("contentDelimiter");
            arr[5] = m2.get("contentFormat");
            row = sheet.createRow(rownum++);
            for (int cellNum = 0; cellNum < numOfColumns; cellNum++ ){
                Cell cell = row.createCell(cellNum);
                cell.setCellValue(arr[cellNum]);
            }

        }

    }

    public static void writeHeaderMarkersInSheet(Workbook wb, Map<String, Map<String,String> > headerMarkers) {
        Sheet sheet = wb.getSheet("headerMetadata");
        if(sheet != null) {
            int sindex = wb.getSheetIndex(sheet);
            wb.removeSheetAt(sindex);
        }
        sheet = wb.createSheet("headerMetadata");
        int rownum = 0;
        String headerName;
        //int headerMarkersSize = 0;
        /*
           for(int i = 0; i<headerMarkersSize; i++){
           JSONObject marker = headerMarkers.get(i);

           }*/

        Row row = sheet.createRow(rownum++);
        for (int cellNum = 0; cellNum <= 2; cellNum++ ){
            Cell cell = row.createCell(cellNum);
            if(cellNum == 0) {
                System.out.println("===HEADER NAME IN MARKER SHEET==== " );

                cell.setCellValue("column_name");
            }
            else if (cellNum == 1) {
                System.out.println("===TAGS IN MARKER SHEET==== " );
                cell.setCellValue("type");
            }
            else if (cellNum == 2) {
                System.out.println("===VALUES IN MARKER SHEET==== " );
                cell.setCellValue("value");
            }
        }
        for(Map.Entry<String , Map<String,String> > entry : headerMarkers.entrySet()) {
            headerName = entry.getKey();
            System.out.println("===HEADER NAME IN MARKER SHEET==== " + headerName );
            for(Map.Entry<String, String> en : entry.getValue().entrySet()) {
                row = sheet.createRow(rownum++);
                for (int cellNum = 0; cellNum <= 2; cellNum++ ){
                    Cell cell = row.createCell(cellNum);
                    if(cellNum == 0) {
                        System.out.println("===HEADER NAME IN MARKER SHEET==== " + headerName );

                        cell.setCellValue(headerName);
                    }
                    else if (cellNum == 1) {
                        System.out.println("===TAGS IN MARKER SHEET==== " + en.getKey());
                        cell.setCellValue(en.getKey());
                    }
                    else if (cellNum == 2) {
                        System.out.println("===VALUES IN MARKER SHEET==== " + en.getValue());
                        cell.setCellValue(en.getValue());
                    }
                }

            }
        }

        return;
    }

        
}
