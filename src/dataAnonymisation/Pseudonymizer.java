package dataAnonymisation;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
/**
 * Cette classe permet de lancer l’action de pseudonymisation sur une feuille Excel 
 * sur un ensemble de colonnes [identifiants].
 */
public class Pseudonymizer {
	/**
	 * Créer un fichier Excel résultat de la pseudonymisation des différents identifiant.
	 */
	public ExcelSheet pseudonumize(ExcelSheet sheetToPseudonymize, List<String> identifiants) throws IOException {

		ExcelSheet copiedSheet = Utilities.copySheet(sheetToPseudonymize);
		copiedSheet.setName(copiedSheet.getName() + "_pseudonymiser");

		for (String identifiant : identifiants) {
			pseudonumizeIdentifiant(copiedSheet, identifiant);
		}

		ExcelWriter writer = new ExcelWriter();
		writer.writeExcelSheet(copiedSheet);
		return copiedSheet;

	}
	/**
	 * Permet de pseudonymiser une colonne dans le fichier excel reçu en paramètre.
	 */
	private void pseudonumizeIdentifiant(ExcelSheet copiedSheet, String identifiant) {

		int headerIndex = copiedSheet.getHeaders().indexOf(identifiant);
		for (int i = 0; i < copiedSheet.getNbOfRows(); i++) {
			List<Object> row = copiedSheet.getRow(i);

			Cell cellAtIndex = (Cell) row.get(headerIndex);
			String valueToHash = "";

			switch (cellAtIndex.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				valueToHash = cellAtIndex.getStringCellValue();
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				valueToHash = String.valueOf(cellAtIndex.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_NUMERIC:
				valueToHash = String.valueOf(cellAtIndex.getNumericCellValue());
				break;
			}
			String hashedCellValue = hashCellValue(valueToHash);

			List<Object> copiedRow = copiedSheet.getRow(i);
			Cell cellToHash = (Cell) copiedRow.get(headerIndex);
			cellToHash.setCellValue(hashedCellValue);
		}
	}

	/**
	 * permet de générer une valeur hachée pour la cellule donnée
	 *
	 * @param cellValue la valeur de la cellule a hachée
	 * @return returns la valeur hashée de la cellule
	 */
	public String hashCellValue(String cellValue) {
		String generatedHash = "";
		if (!"".equals(cellValue)) {
			try {
				
				MessageDigest md = MessageDigest.getInstance("MD5");
				
				md.update(cellValue.getBytes());
				
				byte[] bytes = md.digest();
				
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < bytes.length; i++) {
					sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
				}
				
				generatedHash = sb.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return generatedHash;
	}
}
