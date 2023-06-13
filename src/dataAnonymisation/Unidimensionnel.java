package dataAnonymisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;

/**
 * L'implementation de l'algorithme avec un seul quasi identifiant
 */
public class Unidimensionnel {

	List<Double> medians = new ArrayList<>();

	List<Double> quasiIdentifiantData = new ArrayList<>();

	/**
	 * Anonymise la feuille donnée selon le principe unidimensionnel en utilisant un tas de données k (passé en paramètre)
	 * 
	 * @param sheet la feuille contenant tous les données
	 * @param k     la taille du bucket
	 * @return 
	 */
	public ExcelSheet anonyme(ExcelSheet sheet, String quasiIdentifiant, List<String> identifiants, int k) {

		ExcelSheet anonymizedSheet = null;
		ExcelSheet copySheet = Utilities.copySheet(sheet);

		// get l'index du premier quasi identifiant
		int index = copySheet.getHeaders().indexOf(quasiIdentifiant);

		// liste des données correspondant au quasi identifiant de l'entête
		quasiIdentifiantData = new ArrayList<Double>();

		// get les données de la colonne des quasi identfiants
		for (int i = 0; i < copySheet.getNbOfRows(); i++) {
			List<Object> row = copySheet.getRow(i);
			Cell keyCell = (Cell) row.get(index);
			double key = (double) keyCell.getNumericCellValue();
			quasiIdentifiantData.add(key);
		}

		// appliquer l'algorithme 1 sur le quasiIdentifiantData
		anonyme(quasiIdentifiantData, k);
		
		Collections.sort(medians);

		anonymizedSheet = generateSheet(copySheet, quasiIdentifiant, identifiants);

		ExcelWriter writer = new ExcelWriter();
		try {
			writer.writeExcelSheet(anonymizedSheet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return anonymizedSheet;
	}
	/**
	 * permet de générer la feuille excel depuis la procédure d’anonymisation
	 */
	private ExcelSheet generateSheet(ExcelSheet orginalSheet, String quasiIdentifiant, List<String> identifiants) {

		ExcelSheet anonymizedSheet = Utilities.copySheet(orginalSheet);

		anonymizedSheet.setName(anonymizedSheet.getName() + "_uni-unonymized");

		// get l'index de l'entête
		int index = orginalSheet.getHeaders().indexOf(quasiIdentifiant);

		// remplace la valeur du quasi identifiant par une intervalle basé sur la mediane
		for (int i = 0; i < anonymizedSheet.getNbOfRows(); i++) {
			List<Object> row = anonymizedSheet.getRow(i);
			updateQuasiIdentifiantValue(row, index);
		}
		for (String identifiant : identifiants) {
			anonymizedSheet.removeColumn(identifiant);
		}
		return anonymizedSheet;
	}

	/**
	 * remplace la valeur de la cellule à l'index spécifié d'une ligne
	 * avec la valeur anonymisé generalisé basé sur les valeurs médianes
	 * 
	 * @param row   la ligne dans une feuille
	 * @param index l'index de la cellule qui va etre anonymiser
	 */
	private void updateQuasiIdentifiantValue(List<Object> row, int index) {

		Double median = 0.0;

		for (int i = 0; i < medians.size(); i++) {
			if (((Cell) row.get(index)).getNumericCellValue() >= medians.get(i)) {
				median = medians.get(i);
			}
		}

		// if the value of the
		if (median == 0.0) {
			((Cell) row.get(index)).setCellValue("[" + quasiIdentifiantData.get(0) + "-" + medians.get(0) + "]");
		}
		// verifie si la mediane est le dernier element de la liste des medianes
		else if (medians.indexOf(median) == medians.size() - 1) {
			((Cell) row.get(index))
					.setCellValue("[" + median + "-" + quasiIdentifiantData.get(quasiIdentifiantData.size() - 1) + "]");
		} else {
			((Cell) row.get(index)).setCellValue("[" + median + "-" + medians.get(medians.indexOf(median) + 1) + "]");
		}

	}

	private void anonyme(List<Double> quasiIdentifiantData, int k) {
		// get la mediane des données
		double median = getMedian(quasiIdentifiantData);

		List<Double> lhs = new ArrayList<>();
		List<Double> rhs = new ArrayList<>();

		for (double data : quasiIdentifiantData) {
			if (data <= median) {
				lhs.add(data);
			} else {
				rhs.add(data);
			}
		}

		// prends la mediane seulement si la taille des données est lhs et rhs > k
		if (lhs.size() >= k && rhs.size() >= k) {
			medians.add(median);
			anonyme(lhs, k);
			anonyme(rhs, k);
		}
	}

	/**
	 * permet de calculer la médiane des données de la colonne QID
	 * 
	 * @param quasiIdentifiantData les données pour calculer la médiane
	 */
	private double getMedian(List<Double> quasiIdentifiantData) {
		Collections.sort(quasiIdentifiantData);

		int n = quasiIdentifiantData.size();

		if (n % 2 != 0)
			return quasiIdentifiantData.get(n / 2);

		return (quasiIdentifiantData.get((n - 1) / 2) + quasiIdentifiantData.get(n / 2)) / 2.0;
	}

	/**
	 * permet d’ordonner la liste des fréquences pour cette procédure
	 */
	public TreeMap<Double, Integer> sortBykey(Map<Double, Integer> dataFrequencies) {
		
		TreeMap<Double, Integer> sorted = new TreeMap<>();

		
		sorted.putAll(dataFrequencies);

		return sorted;
	}
}
