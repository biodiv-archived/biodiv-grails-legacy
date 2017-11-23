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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DataFormatter;

public class SpreadsheetReader {

	private static DataFormatter formatter = new DataFormatter();
	private static final Log log = LogFactory.getLog(SpreadsheetReader.class);
	
	public static List<List<Map>> readSpreadSheet(String file) {
		log.info("Reading spreadsheet "+file);
		List<List<Map>> sheetContent = new ArrayList<List<Map>>();
		InputStream inp;
		try {
			inp = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(inp);
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				log.info("Reading sheet : "+i);
				List<Map> content = readSpreadSheet(wb, i, 0);
				log.info("Reading sheet : "+i+" done");
				sheetContent.add(content);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sheetContent;
	}

	
	public static List<Map> readSpreadSheet(String file, String sheetName,
			int headerRowNo) {
		InputStream inp;
		try {
			inp = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheet(sheetName);
			if(sheet !=null )
				return readSpreadSheet(wb, wb.getSheetIndex(sheet), headerRowNo);
			else
				return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public static List<Map> readSpreadSheet(String file, int sheetNo,
			int headerRowNo) {
		InputStream inp;
		try {
			inp = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(inp);
			return readSpreadSheet(wb, sheetNo, headerRowNo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Map> readSpreadSheet(Workbook wb, int sheetNo,
			int headerRowNo) {
		List<Map> content = new ArrayList<Map>();

		Sheet sheet = wb.getSheetAt(sheetNo);
		Row headerRow = sheet.getRow(headerRowNo);
        List<Map> headerList = new ArrayList<Map>();
        if(headerRow !=null){
            for (Cell cell : headerRow) {
                String cellVal = getCellText(cell);
                HashMap headerConfig = new LinkedHashMap();
                if (cellVal != null && !cellVal.equals("")) {
                    headerConfig.put("name", cellVal.trim().toLowerCase());
                    headerConfig.put("position", cell.getColumnIndex() + "");
                    headerList.add(headerConfig);
                }
            }
        }
        
		for (Row row : sheet) {
			if (row.getRowNum() <= headerRowNo)
				continue;
			Map rowData = new LinkedHashMap();
			boolean validRow = false;
                        for (int i = 0; i < headerList.size(); i++) {
				Map headerConfig = (Map) headerList.get(i);
				String key = (String) headerConfig.get("name");
				int index = Integer.parseInt((String) headerConfig
                                        .get("position"));
				String value;
								try{
									value = getCellText(row.getCell(index, Row.CREATE_NULL_AS_BLANK));
								}catch(RuntimeException e){
									System.out.println("Error in Sheet Reading ||| Row Num " + row.getRowNum() + " === index " + index + "  column Name " + key);
									throw(e);
								}
                                // String validTagName =
                                // DocumentUtils.convertToValidXMLTagName(key);
                                validRow = validRow || !value.equals("");
                                rowData.put(key, value);
                        }
                        if(validRow)
                            content.add(rowData);
                }
        if (content.size() == 0){
            Map rowData = new LinkedHashMap();
            for (int i = 0; i < headerList.size(); i++) {
                Map headerConfig = (Map) headerList.get(i);
                String key = (String) headerConfig.get("name");
                int index = Integer.parseInt((String) headerConfig
                        .get("position"));
                String value = "";
                // String validTagName =
                // DocumentUtils.convertToValidXMLTagName(key);
                rowData.put(key, value);
            }
            content.add(rowData);
        }
		return content;
	}

	public static List<List<String>> readSpreadSheet(String file, int sheetNo) {
		InputStream inp;
		try {
			inp = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(inp);
			return readSpreadSheet(wb, sheetNo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

        private static List<List<String>> readSpreadSheet(Workbook wb, int sheetNo) {
            List<List<String>> content = new ArrayList<List<String>>();
            Sheet sheet = wb.getSheetAt(sheetNo);
            Row r = sheet.getRow(0);
            int lastCellNum = r.getLastCellNum();
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<String>();
                for(int i = 0; i < lastCellNum; i++) {
                    Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
                    String cellVal = getCellText(cell);
                    rowData.add(cellVal);
                }
                content.add(rowData);
            }
            return content;
        }

        private static String getCellText(Cell cell) {
		// return cell.getRichStringCellValue().getString();
		String text = formatter.formatCellValue(cell).trim();
		// if(text.isEmpty()) return null;
		return text;
	}

	public static int getSheetIndex(String file, String sheetName) {
		try {
			InputStream inp = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(inp);
			return wb.getSheetIndex(sheetName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static List<Map> getHeaders(String file, int sheetNo,
			int headerRowNo) {
		InputStream inp;
		try {
			inp = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(inp);
			return getHeaders(wb, sheetNo, headerRowNo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Map> getHeaders(Workbook wb, int sheetNo,
			int headerRowNo) {
		List<Map> content = new ArrayList<Map>();

		Sheet sheet = wb.getSheetAt(sheetNo);
		Row headerRow = sheet.getRow(headerRowNo);
        List<Map> headerList = new ArrayList<Map>();
        if(headerRow !=null){
            for (Cell cell : headerRow) {
                String cellVal = getCellText(cell);
                HashMap headerConfig = new LinkedHashMap();
                if (cellVal != null && !cellVal.equals("")) {
                    headerConfig.put("name", cellVal.trim().toLowerCase());
                    headerConfig.put("position", cell.getColumnIndex() + "");
                    headerList.add(headerConfig);
                }
            }
        }
        return headerList;
    }     
}
