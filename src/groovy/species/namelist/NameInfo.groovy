package species.namelist

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import species.ScientificName
import species.participation.NamelistService


class NameInfo {
	
	public final static String TAXON_NAMES_SHEET = "TaxonNames"
	public final static String HIR_SHEET = "HierarchyNames"
	public final static String SYNONYMS_SHEET = "Synonyms"
	
	
	String name
	int rank = -1
	
	private int sourceIndex
	String sourceName
	
	List taxonHir
	List synonyms
	
	
	private int sourceIndexOffset = 1
	
	public NameInfo(name, rank, sourceIndex, sourceName){
		this.name = name
		this.rank = rank
		this.sourceIndex = sourceIndex + sourceIndexOffset
		this.sourceName = sourceName
	}
	
	public NameInfo(name, rank, sourceIndex){
		this(name, rank, sourceIndex, name)
	}
	
	public NameInfo(name, sourceIndex){
		this(name, -1, sourceIndex)
	}
	
	public addToHir(NameInfo h){
		if(!taxonHir)
			taxonHir = []//new ArrayList(10)
		taxonHir  << h //.add(h.rank, h)
	}
	
	public addToSyn(NameInfo s){
		if(!synonyms)
			synonyms = []
			
		synonyms << s
	}
	
	public String toString(){
		String str = '\n----------------------'
		str += "\nName :" + name
		str += "\nsourceIndex :" + sourceIndex
		str += "\nSoruceName :" + sourceName
		str += "\nRank :" + rank
		return str
	}
	
	public void printHirSyn(){
		if(taxonHir){
			println "taxonHir :" + taxonHir.size()
		}
		if(synonyms){
			println "synonyms :" + synonyms.size()
		}
	}
	
	public static File writeNamesMapperSheet(List nameInfoList, File f) {
		println "------------------------------- file -----------" + f
		if(!nameInfoList){
			return
		}
				
		List taxonList = []
		nameInfoList.each { NameInfo ni ->
			List th = ni.taxonHir
			if(th){
				th.each {NameInfo t ->
					if(t){
						taxonList << t
					}
				}
			}
		}
		
		List synList = []
		nameInfoList.each { NameInfo ni ->
			List th = ni.synonyms
			if(th){
				synList.addAll(th)
			}
		}
		
		NamelistService nmService = new NamelistService()
		
		int sIndex = 2
		Map content = nmService.nameMapper(nameInfoList)
		writeSheet(f, content, TAXON_NAMES_SHEET, sIndex++)
		
		content = nmService.nameMapper(taxonList)
		writeSheet(f, content, HIR_SHEET, sIndex++)
		
		content = nmService.nameMapper(synList)
		writeSheet(f, content, SYNONYMS_SHEET, sIndex++)
		
		return f
	}		
		
	
	private static void writeSheet(File f, Map content, String sheetName, int sheetIndex) {
		try {
			InputStream inp = new FileInputStream(f)
			
			Workbook wb = WorkbookFactory.create(inp);
			//leaving first sheet as it is and creating new match sheet at index 1
			
			Sheet sheet = wb.getSheet(sheetName);
			if(sheet != null) {
				wb.removeSheetAt(wb.getSheetIndex(sheet));
			}
			sheet = wb.createSheet(sheetName);
			
			//writing header
			Row row =  sheet.createRow(0);
			List arr = ['Name', 'Index', 'Source Name', 'Match Found', 'Matched Name' ,'Rank', 'Status', 'Group', 'Position', 'Id', 'Target Position' , 'Target Status']
			Cell cell;
			int k = 0;
			arr.each {
				cell = row.getCell(k, Row.CREATE_NULL_AS_BLANK);
				cell.setCellValue(it);
				k++;
			}
			
			//writing result
			int rowNum = 1;
			content.each { NameInfo name, List result ->
				int nameIndex = name.sourceIndex
				//println "-------------------------- name " + name.name + "  result " + result
				if(!result){
					//println "--------------- inside"
					row = sheet.createRow(rowNum++);
					cell = row.getCell(0, Row.CREATE_NULL_AS_BLANK);
					cell.setCellValue(name.name);
					cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK);
					cell.setCellValue(nameIndex);
					cell = row.getCell(2, Row.CREATE_NULL_AS_BLANK);
					cell.setCellValue(name.sourceName);
					cell = row.getCell(3, Row.CREATE_NULL_AS_BLANK);
					cell.setCellValue("");
					cell = row.getCell(4, Row.CREATE_NULL_AS_BLANK);
					cell.setCellValue("");
					if(name.rank > -1 ){
						cell = row.getCell(5, Row.CREATE_NULL_AS_BLANK);
						cell.setCellValue(ScientificName.TaxonomyRank.getTRFromInt(name.rank)?.value());
					}
				}else{
					result.each { Map r ->
						row = sheet.createRow(rowNum++);
						row.getCell(0, Row.CREATE_NULL_AS_BLANK).setCellValue(name.name);
						cell = row.getCell(1, Row.CREATE_NULL_AS_BLANK);
						cell.setCellValue(nameIndex);
						cell = row.getCell(2, Row.CREATE_NULL_AS_BLANK);
						cell.setCellValue(name.sourceName);
						//println "--------------------- row created "
						int i = 3
						r.each { k1,v1 ->
							cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
							cell.setCellValue(v1);
							i++;
						}
						
					}
				}
			}
			FileOutputStream out = new FileOutputStream(f);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
	}

	
}