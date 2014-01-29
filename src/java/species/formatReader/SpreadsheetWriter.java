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

    public static void writeSpreadsheet(File f, InputStream inp, JSONArray gridData, Map headerMarkers) {
        System.out.println ("params in write SPREADSHEET " + gridData + " ----- " + headerMarkers);
        try {
            Workbook wb = WorkbookFactory.create(inp);
            int sheetNo = 0;
            writeDataInSheet(wb, gridData, sheetNo);
            writeHeaderMarkersInSheet(wb, headerMarkers);
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

    public static void writeHeaderMarkersInSheet(Workbook wb, Map<String, Map<String,String> > headerMarkers) {
        Sheet sheet = wb.getSheet("headerMetadata");
        if(sheet != null) {
            int sindex = wb.getSheetIndex(sheet);
            wb.removeSheetAt(sindex);
        }
        sheet = wb.createSheet("headerMetadata");
        int rownum = 0;
        String headerName;
        int headerMarkersSize = 0;
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
