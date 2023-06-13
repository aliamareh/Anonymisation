package dataAnonymisation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Cette classe sert à écrire dans un fichier Excel.
 */
public class ExcelWriter {
	/**
	 * Permet d’écrire l’objet ExcelSheet reçu en paramètre dans un fichier.
	 */
	public void writeExcelSheet(ExcelSheet sheetToWrite) throws IOException {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet(sheetToWrite.getName());

		// creer entete
		// creer une ligne et y mettre des cellules dedans
		XSSFRow headers = sheet.createRow(0);
		for (int i = 0; i < sheetToWrite.getHeaders().size(); i++) {
			headers.createCell(i).setCellValue(sheetToWrite.getHeaders().get(i));
		}

		// creer des lignes par rapport à leurs types
		for (int i = 0; i < sheetToWrite.getNbOfRows(); i++) {
			XSSFRow row = sheet.createRow(i + 1);
			List<Object> rowCells = sheetToWrite.getRow(i);
			for (int j = 0; j < rowCells.size(); j++) {
				Object cellObject = rowCells.get(j);
				if (cellObject instanceof Cell) {
					Cell cellToWrite = (Cell) cellObject;

					switch (cellToWrite.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						row.createCell(j).setCellValue(cellToWrite.getStringCellValue());
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						row.createCell(j).setCellValue(cellToWrite.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						row.createCell(j).setCellValue(cellToWrite.getNumericCellValue());
						break;
					}
				}else {
					row.createCell(j).setCellValue(String.valueOf(cellObject));
				}
			}
		}

		// ecrit le resultat dans un fichier
		FileOutputStream fileOut = new FileOutputStream(sheetToWrite.getName() + ".xlsx");
		wb.write(fileOut);
		wb.close();
		fileOut.close();
		System.out.println("Successfully Created workbook");
	}
}