package dataAnonymisation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Cette classe sert à lire un fichier excel.
 */

public class ExcelReader {
	/**
	 * permet de lire et retourner un objet ExcelSheet.
	 */
	public ExcelSheet readExcel(File file) throws IOException {

		Workbook workbook = null;
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			workbook = new XSSFWorkbook(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Sheet firstSheet = workbook.getSheetAt(0);

		ExcelSheet sheet = new ExcelSheet(firstSheet.getSheetName());

		List<String> headers = getHeaders(firstSheet.getRow(0));
		sheet.addHeaders(headers);

		for (int i = 0; i < firstSheet.getLastRowNum(); i++) {
			Row nextRow = firstSheet.getRow(i+1);
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			List<Object> rowToAdd = new ArrayList<>();

			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				rowToAdd.add(cell);
			}
			sheet.appendRow(rowToAdd);
		}
		workbook.close();
		inputStream.close();
		
		sheet.removeEmptyColumns();
		sheet.removeEmpytRows();

//		Pseudonymizer pseudonymizer = new Pseudonymizer();
//		pseudonymizer.pseudonumize(sheet, 0);
		
		return sheet;
	}
	/**
	 * permet de récupérer l’entête de la feuille.
	 */
	public List<String> getHeaders(Row firstRow) {
		List<String> headers = new ArrayList<String>();
		Iterator<Cell> cellIterator = firstRow.cellIterator();

		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();

			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				headers.add(cell.getStringCellValue());
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				headers.add(String.valueOf(cell.getBooleanCellValue()));
				break;
			case Cell.CELL_TYPE_NUMERIC:
				headers.add(String.valueOf(cell.getNumericCellValue()));
				break;
				default:
					headers.add("");
			}
		}
		return headers;
	}
}
