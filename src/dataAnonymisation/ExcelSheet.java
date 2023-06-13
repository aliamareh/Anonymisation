package dataAnonymisation;

import java.util.*;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Cette classe sert à représenter une feuille Excel dans le programme.
 */
public class ExcelSheet {
	private String name;

	private int numRows = 0;

	List<String> headers = new ArrayList<>();

	List< List<Object>> rows = new ArrayList<>();

	/**
	 * 
	 * Créer une feuille vide avec un nom.
	 *
	 * @param name Le nom de la feuille Excel
	 */
	public ExcelSheet(String name) {
		this(name, 1, 1);
	}

	/**
	 * Permet d’initialiser une feuille vide avec le nom donné, ou le nombre des Lignes/colonnes !
	 *
	 * @param name    Le nom de la feuille Excel
	 * @param rows    Nombre de ligne dans la feuille 
	 * @param columns Nombre de colonne dans la feuille.
	 *
	 * @throws IllegalArgumentException Si le nombre de ligne/colonne sont négative
	 */
	public ExcelSheet(String name, int rows, int columns) {
		if (rows < 0 || columns < 0)
			throw new IllegalArgumentException("Rows/Columns can't be negative");

		this.name = name;
	}

	/**
	 * ajoute les entêtes de la feuille excel 
	 *
	 * @param headers la liste d'entête lu à partir du fichier excel
	 */
	public void addHeaders(List<String> headersToAdd) {
		for (String header : headersToAdd) {
			headers.add(header);
		}
	}

	/**
	 * Les entêtes de la feuille
	 *
	 * @return la liste des entêtes de cette feuille
	 */
	public List<String> getHeaders() {
		return headers;
	}

	/**
	 * permet d’ajouter une ligne à la feuille ! 
	 *
	 * @param row   ligne a ajouter
	 */
	public void appendRow(List<Object> row) {
		rows.add(row);
		numRows++;
	}

	/**
	 * permet de récupérer une ligne de la feuille
	 *
	 * @param index l'index de la ligne a retourner
	 * @return  la ligne composée d'une liste de cellules
	 */
	public List<Object> getRow(int index) {
		return rows.get(index);
	}

	/**
	 * permet de récupérer le nombre de ligne de la feuille 
	 *
	 * @return Un entier qui répresente le nombre de ligne de cette feuille
	 */
	public int getNbOfRows() {
		return numRows;
	}

	/**
	 * permet de récuperer le nom de la feuille
	 *
	 * @return Le nom de la feuille
	 */
	public String getName() {
		return name;
	}

	/**
	 * Permet de modifier le nom de la feuille
	 *
	 * @param name Le nom de la feuille
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * permet de supprimer une colonne avec le titre donné de la feuille
	 *
	 * @param header L'index de la colonne a supprimer
	 */
	public void removeColumn(String header) {
		int index = headers.indexOf(header);
		this.headers.remove(header);

		for (int i = 0; i < numRows; i++) {
			List<Object> row = getRow(i);
			if(row != null)
			row.remove(index);
		}
	}

	/**
	 * permet de supprimer les colonnes vides de la feuille
	 */
	public void removeEmptyColumns() {
		int index = headers.indexOf("");
		headers.remove(index);
		while (index != -1) {
			for (int i = 0; i < numRows; i++) {
				List<Object> row = getRow(i);
				row.remove(index);
			}
			index = headers.indexOf("");
		}

	}

	/**
	 * permet de supprimer les lignes vides de la feuille
	 */
	public void removeEmpytRows() {
		for (int i = 0; i < numRows; i++) {
			List<Object> row = getRow(i);
			boolean isRowEmpty = true;
			for (Object cellObject : row) {
				Cell cell = (Cell) cellObject;
				if (!cell.toString().trim().equals(""))
					isRowEmpty = false;
				break;
			}
			if (isRowEmpty) {
				removeRow(i);
				i--;
			}
		}
	}

	/**
	 * permet de supprimer la ligne précisée de la feuille
	 * 
	 * @param index l'index de la ligne a supprimé
	 */
	private void removeRow(int index) {
		rows.remove(index);
		numRows--;
	}

	/**
	 * ajoute une entête à la liste d'entête de la feuille
	 * 
	 * @param header L'entête a ajouter
	 */
	public void addHeader(String header) {
		this.headers.add(header);
	}
}