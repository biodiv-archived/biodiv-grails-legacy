package species.formatReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONElement;
import org.codehaus.groovy.grails.web.json.JSONObject;

public class SpreadsheetWriter {

    static final String KEYVALUE_SEP = "#11#";
    static final String COLUMN_SEP = "#12#";
    static final String FIELD_SEP = "#13#";
    
    public static void writeSpreadsheet(File f, InputStream inp, JSONArray gridData, JSONElement headerMarkers, String writeContributor, String contEmail, JSONArray orderedArray) {
        //System.out.println ("params in write SPREADSHEET " + gridData + " ----- " + headerMarkers);
        try {
            Workbook wb = WorkbookFactory.create(inp);
            int sheetNo = 0;
            writeDataInSheet(wb, gridData, sheetNo, writeContributor, contEmail, orderedArray);
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

    public static void writeDataInSheet(Workbook wb, JSONArray gridData, int sheetNo, String writeContributor, String contEmail, JSONArray orderedArray) {
        //System.out.println("================================" + writeContributor +"===" + contEmail );
        /*if(writeContributor.equals("true")){
            JSONObject r =  gridData.getJSONObject(0);
            if(!r.has("contributor")){
                for(int k = 0; k < gridData.length();k++){
                    JSONObject r1 =  gridData.getJSONObject(k);
                    r1.put("contributor", contEmail);
                }
            }
        }*/
        Sheet sheet = wb.getSheetAt(sheetNo);
        Iterator<Row> rowIterator = sheet.iterator();
        int index = 0;
        int i = 0;
        boolean headerRow = true;
        //System.out.println("===JSON ARRAY LENGTH==============");
        //System.out.println(gridData.length());
        int gDataSize = gridData.length();
        JSONObject rowData = gridData.getJSONObject(index);
        Iterator<String> keys = rowData.keys();
        int numKeys = 0;
        while(keys.hasNext()){
            String kk = keys.next();
            numKeys++;
        }
        String[] keysArray = new String[numKeys];
        //String[] keysArray = orderedArray;
        for (int k = 0; k< numKeys; k++){
            keysArray[k] = orderedArray.getString(k); 
        }
        Row row = rowIterator.next();
        for(int a = 0; a < numKeys; a++){
            Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
            //System.out.println("======PRINTING THIS TO HEADER CELL===== " + keysArray[a]);
            cell.setCellValue(keysArray[a]);
            i++;
        }
        int lastHeaderCellNum = row.getLastCellNum();
        for(int j = i; j <= lastHeaderCellNum; j++) {
            Cell cell = row.getCell(j, Row.CREATE_NULL_AS_BLANK);
            cell.setCellValue("");
        }

        for (int k = 0; k < gDataSize; k++) {
            //System.out.println("REACHED FOR LOOP");
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
            //System.out.println("============ "); 
            //System.out.println(gridData);

            //for ( Map.Entry<String, String> entry : mapRow.entrySet()) {
            //while( keys.hasNext() ){
            for(int a = 0; a < numKeys; a++){
                //String key = (String)keys.next();
                Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
                //System.out.println ("=====PRINTING THIS TO NORMAL CELL====== " + rowData.getString(keysArray[a]));
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
    
    public static void writeHeadersInFormat(Workbook wb, JSONElement headerMarkers1) {
    	JSONObject headerMarkers = (JSONObject) headerMarkers1;
        //System.out.println("=CLASS===="+headerMarkers.getClass());
        Object o = headerMarkers.remove("undefined");
        Sheet sheet = wb.getSheet("headerMetadata");
        if(sheet != null) {
            int sindex = wb.getSheetIndex(sheet);
            wb.removeSheetAt(sindex);
        }
        sheet = wb.createSheet("headerMetadata");
        Map <String, Map<String,String>> reverseMarkers = new HashMap<String, Map<String, String>>();
        int rownum = 0;

        for(Object entry1 : headerMarkers.entrySet()) {
        	Entry<String, Map<String, String>> entry = (Entry<String, Map<String, String>>) entry1;
            String headerName = entry.getKey();
            headerName = headerName.trim().toLowerCase();
            Map<String,String> headerValues = new HashMap<String, String>();
            for(Map.Entry<String, String> en : entry.getValue().entrySet()) {
                //System.out.println("=======HERE======" + en.getKey());
                if(en.getKey() != "undefined"){
                	String val = (en.getValue() instanceof String ? en.getValue() : null);
                    headerValues.put(en.getKey(), val);
                }
            }
            //System.out.println("==========NEW FUNC==============");
            //System.out.println(headerName);
            //System.out.println("---------" + entry.getValue() + entry.getValue().getClass());
            System.out.println("-------==============" + headerValues + headerValues.getClass());
            String dataColumns = "";
            if(headerValues.get("dataColumns") != null){
                dataColumns = headerValues.get("dataColumns");
                dataColumns = dataColumns.trim();
            }
            String group = "";
            if(headerValues.get("group") != null){
                //System.out.println(headerValues.get("group"));
                group = headerValues.get("group");
                group = group.trim();
            }

            //System.out.println("====ERROR FINIDING==" + headerValues.get("group"));
            String includeHeadings = "";
            if(headerValues.get("header") != null){
                includeHeadings = headerValues.get("header");
                includeHeadings = includeHeadings.trim();
            }
            
            String append = "";
            if(headerValues.get("append") != null){
                append = headerValues.get("append");
                append = append.trim();
            }

            String delimiter = "";
            if(headerValues.get("delimiter") != null){
                delimiter = headerValues.get("delimiter");
                delimiter = delimiter.trim();
            }
            
            String images = "";
            if(headerValues.get("images") != null){
                images = headerValues.get("images");
                images = images.trim();
            }

            String contributor = "";
            if(headerValues.get("contributor") != null){
                contributor = headerValues.get("contributor");
                contributor = contributor.trim();
            }

            String attributions = "";
            if(headerValues.get("attributions") != null){
                attributions = headerValues.get("attributions");
                attributions = attributions.trim();
            }

            String references = "";
            if(headerValues.get("references") != null){
                references = headerValues.get("references");
                references = references.trim();
            }
            
            String license = "";
            if(headerValues.get("license") != null){
                license = headerValues.get("license");
                license = license.trim();
            }
            
            String audience = "";
            if(headerValues.get("audience") != null){
                audience = headerValues.get("audience");
                audience = audience.trim();
            }

            String language = "";
            if(headerValues.get("language") != null){
                language = headerValues.get("language");
                language = language.trim();
            }

            //System.out.println("=======" + dataColumns);
            List<String> dcList = Arrays.asList(dataColumns.split(","));
            Iterator<String> dcIterator = dcList.iterator();
            while (dcIterator.hasNext()) {
                String nextVal = dcIterator.next();
                nextVal = nextVal.trim();
                //System.out.println("==THIS IS THE VALUE=== " + nextVal);
                if(!nextVal.equals("") && !nextVal.equals(null)){
                    if(reverseMarkers.containsKey(nextVal)){
                        Map<String, String> m = reverseMarkers.get(nextVal);
                        String fieldNames = m.get("fieldNames");
                        if(fieldNames != "") {
                            fieldNames += FIELD_SEP + headerName;
                            m.put("fieldNames", fieldNames);
                        }
                        else {
                            m.put("fieldNames", headerName);
                        }
                        String contentDelimiter = m.get("contentDelimiter");
                        if(contentDelimiter != "") {
                            contentDelimiter += COLUMN_SEP + headerName + KEYVALUE_SEP + delimiter;
                            m.put("contentDelimiter", contentDelimiter);
                        }
                        else {
                            m.put("contentDelimiter", headerName + KEYVALUE_SEP + delimiter);
                        }
                        String contentFormat = m.get("contentFormat");
                        if(contentFormat != "") {
                            contentFormat += COLUMN_SEP + headerName + KEYVALUE_SEP + "Group=" + group +";" + "includeheadings=" + includeHeadings +";" + "append=" + append + ";";
                            m.put("contentFormat", contentFormat);
                        }
                        else {
                            m.put("contentFormat",  headerName + KEYVALUE_SEP + "Group=" + group +";" + "includeheadings=" + includeHeadings +";" + "append=" + append + ";");
                        }
                        String imagesCol = m.get("images");
                        if(imagesCol != "") {
                            imagesCol += COLUMN_SEP + headerName + KEYVALUE_SEP  + images;
                            m.put("images", imagesCol);
                        }
                        else {
                            m.put("images",  headerName + KEYVALUE_SEP + images);
                        }
                        String contributorCol = m.get("contributor");
                        if(contributorCol != "") {
                            contributorCol += COLUMN_SEP + headerName + KEYVALUE_SEP  + contributor;
                            m.put("contributor", contributorCol);
                        }
                        else {
                            m.put("contributor",  headerName + KEYVALUE_SEP + contributor);
                        }
                        String attributionsCol = m.get("attributions");
                        if(attributionsCol != "") {
                            attributionsCol += COLUMN_SEP + headerName + KEYVALUE_SEP  + attributions;
                            m.put("attributions", attributionsCol);
                        }
                        else {
                            m.put("attributions",  headerName + KEYVALUE_SEP + attributions);
                        }
                        String referencesCol = m.get("references");
                        if(referencesCol != "") {
                            referencesCol += COLUMN_SEP + headerName + KEYVALUE_SEP  + references;
                            m.put("references", referencesCol);
                        }
                        else {
                            m.put("references",  headerName + KEYVALUE_SEP + references);
                        }

                        String licenseCol = m.get("license");
                        if(licenseCol != "") {
                            licenseCol += COLUMN_SEP + headerName + KEYVALUE_SEP  + license;
                            m.put("license", licenseCol);
                        }
                        else {
                            m.put("license",  headerName + KEYVALUE_SEP + license);
                        }
                        String audienceCol = m.get("audience");
                        if(audienceCol != "") {
                            audienceCol += COLUMN_SEP + headerName + KEYVALUE_SEP  + audience;
                            m.put("audience", audienceCol);
                        }
                        else {
                            m.put("audience",  headerName + KEYVALUE_SEP + audience);
                        }
            
                        String languageCol = m.get("language");
                        if(languageCol != "") {
                            languageCol = language;
                            m.put("language", languageCol);
                        }
                        else {
                            m.put("language",  language);
                        }


                    }else {
                        Map<String, String> m1 = new HashMap();
                        m1.put("fieldNames", headerName);
                        m1.put("contentDelimiter", headerName + KEYVALUE_SEP + delimiter);
                        m1.put("contentFormat",  headerName + KEYVALUE_SEP + "Group=" + group +";" + "includeheadings=" + includeHeadings +";" + "append=" + append + ";");
                        m1.put("images",  headerName + KEYVALUE_SEP + images);
                        m1.put("contributor",  headerName + KEYVALUE_SEP + contributor);
                        m1.put("attributions",  headerName + KEYVALUE_SEP + attributions);
                        m1.put("references",  headerName + KEYVALUE_SEP + references);
                        m1.put("license",  headerName + KEYVALUE_SEP + license);
                        m1.put("audience",  headerName + KEYVALUE_SEP + audience);
                        m1.put("language",  language);


                        reverseMarkers.put(nextVal, m1);
                    }
                }
            }

        }

        Row row = sheet.createRow(rownum++);
        
        String[] headerRowValues = {"CONCEPT", "CATEGORY", "SUBCATEGORY", "FIELD NAME(S)", "CONTENT DELIMITER", "CONTENT FORMAT", "IMAGES", "CONTRIBUTOR", "ATTRIBUTIONS", "REFERENCES", "LICENSE","AUDIENCE", "LANGUAGE"};
        int numOfColumns = headerRowValues.length;
        for (int cellNum = 0; cellNum < numOfColumns; cellNum++ ){
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(headerRowValues[cellNum]);
        }
        //System.out.println("====REVERSE MARKERS=====" + reverseMarkers);
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
            arr[6] = m2.get("images");
            arr[7] = m2.get("contributor");
            arr[8] = m2.get("attributions");
            arr[9] = m2.get("references");
            arr[10] = m2.get("license");
            arr[11] = m2.get("audience");
            arr[12] = m2.get("language");

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
                //System.out.println("===HEADER NAME IN MARKER SHEET==== " );

                cell.setCellValue("column_name");
            }
            else if (cellNum == 1) {
                //System.out.println("===TAGS IN MARKER SHEET==== " );
                cell.setCellValue("type");
            }
            else if (cellNum == 2) {
                //System.out.println("===VALUES IN MARKER SHEET==== " );
                cell.setCellValue("value");
            }
        }
        for(Map.Entry<String , Map<String,String> > entry : headerMarkers.entrySet()) {
            headerName = entry.getKey();
            //System.out.println("===HEADER NAME IN MARKER SHEET==== " + headerName );
            for(Map.Entry<String, String> en : entry.getValue().entrySet()) {
                row = sheet.createRow(rownum++);
                for (int cellNum = 0; cellNum <= 2; cellNum++ ){
                    Cell cell = row.createCell(cellNum);
                    if(cellNum == 0) {
                        //System.out.println("===HEADER NAME IN MARKER SHEET==== " + headerName );

                        cell.setCellValue(headerName);
                    }
                    else if (cellNum == 1) {
                        //System.out.println("===TAGS IN MARKER SHEET==== " + en.getKey());
                        cell.setCellValue(en.getKey());
                    }
                    else if (cellNum == 2) {
                        //System.out.println("===VALUES IN MARKER SHEET==== " + en.getValue());
                        cell.setCellValue(en.getValue());
                    }
                }

            }
        }

        return;
    }

        
}
