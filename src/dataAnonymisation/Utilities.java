package dataAnonymisation;

import java.util.ArrayList;
import java.util.List;
/**
 * Cette classe sert � d�finir les outils n�cessaires pour diff�rents traitements.
 */
public class Utilities {
	/**
	 * permet de copier l�objet ExcelSheet re�u en param�tre dans un autre
	 *
	 * @see original la feuille original � copier
	 */
	public static ExcelSheet copySheet(ExcelSheet sheet) {

		ExcelSheet copiedSheet = new ExcelSheet(sheet.getName());
		copiedSheet.addHeaders(sheet.getHeaders());

		for (List<Object>entry : sheet.rows) {
			List<Object> list = new ArrayList<Object>();
			list.addAll(entry);
			copiedSheet.appendRow(list);
		}
		return copiedSheet;
	}
}
