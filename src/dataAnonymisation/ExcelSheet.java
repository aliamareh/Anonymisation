package dataAnonymisation;

import java.util.*;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Cette classe sert � repr�senter une feuille Excel dans le programme.
 */
public class ExcelSheet {
	private String name;

	private int numRows = 0;

	List<String> headers = new ArrayList<>();

	List< List<Object>> rows = new ArrayList<>();

	/**
	 * 
	 * Cr�er une feuille vide avec un nom.
	 *
	 * @param name Le nom de la feuille Excel
	 */
	public ExcelSheet(String name) {
		this(name, 1, 1);
	}

	/**
	 * Permet d�initialiser une feuille vide avec le nom donn�, ou le nombre des Lignes/colonnes !
	 *
	 * @param name    Le nom de la feuille Excel
	 * @param rows    Nombre de ligne dans la feuille 
	 * @param columns Nombre de colonne dans la feuille.
	 *
	 * @throws IllegalArgumentException Si le nombre de ligne/colonne sont n�gative
	 */
	public ExcelSheet(String name, int rows, int columns) {
		if (rows < 0 || columns < 0)
			throw new IllegalArgumentException("Rows/Columns can't be negative");

		this.name = name;
	}

	/**
	 * ajoute les ent�tes de la feuille excel 
	 *
	 * @param headers la liste d'ent�te lu � partir du fichier excel
	 */
	public void addHeaders(List<String> headersToAdd) {
		for (String header : headersToAdd) {
			headers.add(header);
		}
	}

	/**
	 * Les ent�tes de la feuille
	 *
	 * @return la liste des ent�tes de cette feuille
	 */
	public List<String> getHeaders() {
		return headers;
	}

	/**
	 * permet d�ajouter une ligne � la feuille ! 
	 *
	 * @param row   ligne a ajouter
	 */
	public void appendRow(List<Object> row) {
		rows.add(row);
		numRows++;
	}

	/**
	 * permet de r�cup�rer une ligne de la feuille
	 *
	 * @param index l'index de la ligne a retourner
	 * @return  la ligne compos�e d'une liste de cellules
	 */
	public List<Object> getRow(int index) {
		return rows.get(index);
	}

	/**
	 * permet de r�cup�rer le nombre de ligne de la feuille 
	 *
	 * @return Un entier qui r�presente le nombre de ligne de cette feuille
	 */
	public int getNbOfRows() {
		return numRows;
	}

	/**
	 * permet de r�cuperer le nom de la feuille
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
	 * permet de supprimer une colonne avec le titre donn� de la feuille
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
	 * permet de supprimer la ligne pr�cis�e de la feuille
	 * 
	 * @param index l'index de la ligne a supprim�
	 */
	private void removeRow(int index) {
		rows.remove(index);
		numRows--;
	}

	/**
	 * ajoute une ent�te � la liste d'ent�te de la feuille
	 * 
	 * @param header L'ent�te a ajouter
	 */
	public void addHeader(String header) {
		this.headers.add(header);
	}
}