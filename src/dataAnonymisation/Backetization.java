package dataAnonymisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Implemente la technique de Bucketisation pour anonymiser
 *
 */
public class Backetization {

	public ExcelSheet QID = null;
	public ExcelSheet DS = null;
	/**
	 * Anonymiser la feuille donn�e selon le principe de Backetization en utilisant un groupement de donn�es par k (pass� en param�tre
	 * 
	 * @param sheetTokAnonymize la feuille excel a anonymiser
	 * @param identifiants      les entetes identifiants a cacher
	 * @param sData             les entetes des donn�es sensibles
	 * @param k                 taille du groupe de donn�es
	 * @return 
	 */
	public List<ExcelSheet> kAnonymize(ExcelSheet sheetTokAnonymize, List<String> identifiants, List<String> sData, int k) {

		QID = generateQID(sheetTokAnonymize, identifiants, sData, k);
		DS = generateDS(sheetTokAnonymize, QID, sData);
		List<ExcelSheet> list = new ArrayList<>();
		list.add(DS);
		list.add(QID);
		return list;

	}

	/**
	 * permet de g�n�rer la feuille QID depuis la proc�dure d�anonymisation
	 
	 * @param sheetTokAnonymize la feuille excel a anonymiser
	 * @param identifiants      les entetes identifiants a cacher
	 * @param sData             les entetes des donn�es sensibles
	 * @param k                 taille du groupe de donn�es
	 */
	private ExcelSheet generateQID(ExcelSheet sheetTokAnonymize, List<String> identifiants, List<String> sData, int k) {
		ExcelSheet qIDSheet = Utilities.copySheet(sheetTokAnonymize);
		qIDSheet.setName("QID_" + k + "_anonymiser");

		for (String header : identifiants) {
			qIDSheet.removeColumn(header);
		}

		for (String header : sData) {
			qIDSheet.removeColumn(header);
		}

		// ajoute la colonne groupe � la feuille excel
		qIDSheet.getHeaders().add("Groupe");

		generateQIDTable(qIDSheet, k);

		ExcelWriter writer = new ExcelWriter();
		try {
			writer.writeExcelSheet(qIDSheet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return qIDSheet;
	}

	/**
	 * permet de g�n�rer la feuille DS depuis la proc�dure d�anonymisation
	 * 
	 * @param sheetTokAnonymize la feuille excel a anonymiser
	 * @param identifiants      les entetes identifiants a cacher
	 * @param sData             les entetes des donn�es sensibles
	 * @param k                 taille du groupe de donn�es
	 */
	private ExcelSheet generateDS(ExcelSheet sheetTokAnonymize, ExcelSheet qID, List<String> sData) {
		ExcelSheet dS = new ExcelSheet("DS");
		
		ExcelSheet copySheet = Utilities.copySheet(sheetTokAnonymize);

		dS.addHeader("Groupe");
		dS.addHeaders(sData);

		for (int i = 0; i < copySheet.getNbOfRows(); i++) {
			List<Object> row = new ArrayList<>();
			Object groupCellToAdd = qID.getRow(i).get(qID.getHeaders().indexOf("Groupe"));
			row.add(groupCellToAdd);
			dS.appendRow(row);
			for (String sdHeader : sData) {
				Object cellToAdd = copySheet.getRow(i).get(copySheet.getHeaders().indexOf(sdHeader));
				row.add(cellToAdd);
			}
		}
		ExcelWriter writer = new ExcelWriter();
		try {
			writer.writeExcelSheet(dS);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dS;
	}

	/**
	 * ajoute la colonne du groupe � la feuille excel
	 * 
	 * @param copiedSheet la feuille excel � laquelle on rajoute la colonne
	 * @param k           taille du groupe de donn�es
	 */
	private void generateQIDTable(ExcelSheet copiedSheet, int k) {
		int group = 0;
		for (int i = 0; i < copiedSheet.getNbOfRows(); i++) {
			if (i % k == 0) {
				group++;
			}
			List<Object> row = copiedSheet.getRow(i);
			row.add("G" + group);
		}
	}

	/**
	 * permet de v�rifier si les DS sont L diverse
	 * 
	 * @param k taille du groupe de donn�es
	 * @param l nombre de differentes valeur dans le groupe
	 */
	public boolean isLDiverse(ExcelSheet dS, String sData, int k, int l) {
		boolean isLDiverse = true;
		List<Map<String, Integer>> frequencies = new ArrayList<>();
		int index = dS.getHeaders().indexOf(sData);
		Map<String, Integer> bucketFrequencies = null;
		
		for (int i = 0; i < dS.getNbOfRows(); i++) {
			if (i % k == 0) {
				bucketFrequencies = new HashMap<>();
				frequencies.add(bucketFrequencies);
			}
			List<Object> row = dS.getRow(i);
			Cell keyCell = (Cell) row.get(index);
			String key = getCellValue(keyCell);
			if (bucketFrequencies.keySet().contains(key)) {
				int frequency = bucketFrequencies.get(key).intValue();
				frequency++;
				bucketFrequencies.put(key, frequency);
			}else {
				bucketFrequencies.put(key, 0);
			}
		}
		
		for(Map<String, Integer> bucketFrequency : frequencies) {
			if(bucketFrequency.keySet().size() < l) {
				return false;
			}
		}
		
		return isLDiverse;
	}

	/**
	 * permet de r�cup�rer la valeur de la cellule en tant que texte
	 * 
	 * @param keyCell la cellule � laquelle nous prenons la valeur
	 */
	private String getCellValue(Cell keyCell) {
		String cellValue ="";
		switch (keyCell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			cellValue = keyCell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			cellValue = String.valueOf(keyCell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			cellValue = String.valueOf(keyCell.getNumericCellValue());
			break;
		}
		return cellValue;
	}
}
