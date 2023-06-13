package dataAnonymisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;

/**
 * L'implementation de l'algorithme avec plusieur quasi identifiant
 */
public class Multidimensionnel {

	ExcelSheet sheet;
	// la feuille contenant tous les donn�es
	List<String> identifiants = new ArrayList<String>();
	List<String> quasiIdentifiants = new ArrayList<String>();

	// m�diane correspondant � chaque quasi-identifiants si il y'en a
	Map<String, Double> medians = new HashMap<>();

	Map<Double, List<List<List<Object>>>> rhsLhsOfMedians = new HashMap<>();

	/**
	 * constructeur de la classe Multidimensionnel
	 * 
	 * @param sheet             la feuille excel � traiter
	 * @param identifiants      la liste des identifiants � cacher
	 * @param quasiIdentifiants la liste des quasi identifiants � anonymiser
	 */
	public Multidimensionnel(ExcelSheet sheet, List<String> identifiants, List<String> quasiIdentifiants) {
		this.sheet = sheet;
		this.identifiants.addAll(identifiants);
		this.quasiIdentifiants.addAll(quasiIdentifiants);
	}

	/**
	 * anonymise les donn�es de la colonne QID
	 * 
	 * @param k taille du bucket
	 * @return 
	 */
	public ExcelSheet anonymize(int k) {
		
		ExcelSheet anonymizedSheet  = null;
		ExcelSheet copySheet = Utilities.copySheet(sheet);

		if (!quasiIdentifiants.isEmpty()) {
			// get le premier quasi-identfiant
			String currentQuasiIdentifiant = quasiIdentifiants.get(0);
			// get l'index de l'ent�te
			currentQuasiIdentifiant.trim();
			anonymize(copySheet, currentQuasiIdentifiant, k);

			anonymizedSheet = generateSheet(copySheet);

			ExcelWriter writer = new ExcelWriter();
			try {
				writer.writeExcelSheet(anonymizedSheet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return anonymizedSheet;
	}
	/**
	 * Anonymiser la feuille de donn�es selon le principe de multidimensionnel  en utilisant tas de donn�es k (pass� en param�tre)
	 */
	private void anonymize(ExcelSheet copySheet, String currentQuasiIdentifiant, int k) {

		// liste des donn�es correspondant au premier quasi-identifiant
		List<Double> quasiIdentifiantData = getColumnData(copySheet,currentQuasiIdentifiant);

		// applique l'algorithme 1 sur le quasiIdentifianData

		// get la mediane des donn�es
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

		List<List<Object>> rowsInLHS = new ArrayList<>();
		List<List<Object>> rowsInRHS = new ArrayList<>();

		quasiIdentifiants.remove(0);
		int index = copySheet.getHeaders().indexOf(currentQuasiIdentifiant);

		// prends la mediane seulement si la taille des donn�es est lhs et rhs > k
		if (lhs.size() >= k && rhs.size() >= k) {
			medians.put(currentQuasiIdentifiant, median);
			rowsInLHS = getLHSRows(copySheet.rows, median, index);
			rowsInRHS = getRHSRows(copySheet.rows, median, index);
			List<List<List<Object>>> lhsRhs = new ArrayList<>();
			lhsRhs.add(rowsInLHS);
			lhsRhs.add(rowsInRHS);
			rhsLhsOfMedians.put(median, lhsRhs);

			if (!quasiIdentifiants.isEmpty()) {
				String currentQuasiId = quasiIdentifiants.get(0);
				anonyme(copySheet, currentQuasiId, rowsInLHS, k);
				anonyme(copySheet, currentQuasiId, rowsInRHS, k);
			}
		} else if (!quasiIdentifiants.isEmpty()) {
			anonymize(copySheet, quasiIdentifiants.get(0), k);
		} else {
			return;
		}
	}
	/**
	 * permet de r�cup�rer les ligne � gauche (inferieures) de la m�diane de l�indice donn�
	 */
	private List<List<Object>> getLHSRows(List<List<Object>> rows, double median, int index) {
		List<List<Object>> rowsInLHS = new ArrayList<>();
		for (int i = 0; i < rows.size(); i++) {
			List<Object> row = rows.get(i);
			Cell keyCell = (Cell) row.get(index);
			double key = (double) keyCell.getNumericCellValue();
			if (key <= median) {
				rowsInLHS.add(row);
			}
		}
		return rowsInLHS;
	}
	/**
	 * permet de r�cup�rer les ligne � droite (sup�rieures) de la m�diane de l'indice donn�
	 */
	private List<List<Object>> getRHSRows(List<List<Object>> rows, double median, int index) {
		List<List<Object>> rowsInRHS = new ArrayList<>();
		for (int i = 0; i < rows.size(); i++) {
			List<Object> row = rows.get(i);
			Cell keyCell = (Cell) row.get(index);
			double key = (double) keyCell.getNumericCellValue();
			if (key > median) {
				rowsInRHS.add(row);
			}
		}
		return rowsInRHS;
	}
	/**
	 * permet de g�n�rer la feuille r�sultante ! 
	 */
	private ExcelSheet generateSheet(ExcelSheet anonymizedSheet) {

		anonymizedSheet.setName(anonymizedSheet.getName() + "_multi_unonymized");

		for (Entry<String, Double> entry : medians.entrySet()) {
			String quasiIdentifiant = entry.getKey();
			int index = anonymizedSheet.getHeaders().indexOf(quasiIdentifiant);
			List<List<List<Object>>> lhsRhsofMedian = rhsLhsOfMedians.get(entry.getValue());

			// mettre a jour les valeurs quasi identifiant de lhs
			List<List<Object>> lhsRows = lhsRhsofMedian.get(0);
			for (List<Object> row : lhsRows) {
				((Cell) row.get(index)).setCellValue("<=" + entry.getValue());
			}

			// mettre a jour les valeurs quasi identifiants de rhs
			List<List<Object>> rhsRows = lhsRhsofMedian.get(1);
			for (List<Object> row : rhsRows) {
				((Cell) row.get(index)).setCellValue(">" + entry.getValue());
			}
		}

		for (String identifiant : identifiants) {
			anonymizedSheet.removeColumn(identifiant);
		}
		return anonymizedSheet;
	}
	/**
	 * permet de r�cup�rer les donn�es depuis la colonne
	 */ 
	 
	public List<Double> getColumnData(ExcelSheet copySheet, String header) {
		int index = copySheet.getHeaders().indexOf(header);

		// liste des donn�es correspondant au premier quasi identfiant
		List<Double> quasiIdentifiantData = new ArrayList<Double>();

		// get les donn�es dans la colonne des quasi identfiants
		for (int i = 0; i < copySheet.getNbOfRows(); i++) {
			List<Object> row = copySheet.getRow(i);
			Cell keyCell = (Cell) row.get(index);
			double key = (double) keyCell.getNumericCellValue();
			quasiIdentifiantData.add(key);
		}
		return quasiIdentifiantData;
	}
    
	private void anonyme(ExcelSheet copySheet, String currentQuasiIdentifiant, List<List<Object>> rows, int k) {

		// liste des donn�es correspondant au premier quasi identifiant
		List<Double> quasiIdentifiantData = new ArrayList<Double>();
		int index = copySheet.getHeaders().indexOf(currentQuasiIdentifiant.trim());

		// get les donn�es de la colonne des quasi identifiants
		for (int i = 0; i < rows.size(); i++) {
			List<Object> row = rows.get(i);
			Cell keyCell = (Cell) row.get(index);
			double key = (double) keyCell.getNumericCellValue();
			quasiIdentifiantData.add(key);
		}
		// get la mediane des donn�es
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

		List<List<Object>> rowsInLHS = new ArrayList<>();
		List<List<Object>> rowsInRHS = new ArrayList<>();

		if (!quasiIdentifiants.isEmpty()) {
			quasiIdentifiants.remove(0);
		}

		// prends la mediane si seulement la taille des donn�es est lhs et rhs > K
		if (lhs.size() >= k && rhs.size() >= k) {
			medians.put(currentQuasiIdentifiant, median);
			rowsInLHS = getLHSRows(rows, median, index);
			rowsInRHS = getRHSRows(rows, median, index);
			List<List<List<Object>>> lhsRhs = new ArrayList<>();
			lhsRhs.add(rowsInLHS);
			lhsRhs.add(rowsInRHS);
			rhsLhsOfMedians.put(median, lhsRhs);
			if (!quasiIdentifiants.isEmpty()) {
				anonyme(copySheet, quasiIdentifiants.get(0).trim(), rowsInLHS, k);
				anonyme(copySheet, quasiIdentifiants.get(0).trim(), rowsInRHS, k);
			}
		} else if (!quasiIdentifiants.isEmpty()) {
			anonymize(copySheet, quasiIdentifiants.get(0).trim(), k);
		} else {
			return;
		}
	}

	/**
	 * permet de calculer la m�diane des donn�es de la colonne QID
	 * 
	 * @param quasiIdentifiant les donn�es pour calculer sa m�diane
	 */
	private double getMedian(List<Double> quasiIdentifiantData) {
		Collections.sort(quasiIdentifiantData);

		int n = quasiIdentifiantData.size();

		if (n % 2 != 0)
			return quasiIdentifiantData.get(n / 2);

		return (quasiIdentifiantData.get((n - 1) / 2) + quasiIdentifiantData.get(n / 2)) / 2.0;
	}

	/**
	 * permet d�ordonner la liste des fr�quences pour cette proc�dure
	 */
	public TreeMap<Double, Integer> sortBykey(Map<Double, Integer> dataFrequencies) {
		
		TreeMap<Double, Integer> sorted = new TreeMap<>();

		
		sorted.putAll(dataFrequencies);

		return sorted;
	}
}
