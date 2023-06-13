package dataAnonymisation;

import java.util.ArrayList;
import java.util.List;
/**
 * Cette classe sert à définir les outils nécessaires pour différents traitements.
 */
public class Utilities {
	/**
	 * permet de copier l’objet ExcelSheet reçu en paramètre dans un autre
	 *
	 * @see original la feuille original à copier
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
