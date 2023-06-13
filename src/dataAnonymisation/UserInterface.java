package dataAnonymisation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import javax.swing.filechooser.*;

import org.apache.poi.ss.usermodel.Cell;

class UserInterface extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// Jlabel qui montrent les fichiers que l'utilisateur selectionne
	static JLabel label;
	File file;
	static JPanel panel;

	static ExcelSheet sheet;

	//panel qui contient les pages excel
	JPanel tablePanel;
	GridBagConstraints gbcTable;

	static JFrame frame;

	//DS fichier excel généré
	ExcelSheet DS;

	public static void main(String args[]) {
		// Cadre qui contient les elements GUI
		frame = new JFrame("Anonymisation de données sensibles");

		// determiner la taille du cadre
		frame.setSize(900, 600);

		// determiner la visibilité du cadre
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// boutton qui ouvre open dialog
		JButton openButton = new JButton("Ouvrir");

		// creer un objet pour la classe filechooser
		UserInterface f1 = new UserInterface();

		openButton.addActionListener(f1);

		// creer un panel pour ajouter les bouttons et labels
		panel = new JPanel(new BorderLayout());

		JPanel panelLabelButton = new JPanel(new FlowLayout());

		// ajouter un boutton au cadre
		panelLabelButton.add(openButton);

		// remet le label à sa valeur initiale
		label = new JLabel("aucun fichier séléctionné");

		// ajoute la panel au cadre
		panelLabelButton.add(label);
		panel.add(panelLabelButton, BorderLayout.NORTH);
		frame.add(panel);

		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent evt) {

		// cree un objet de classe JFileChooser
		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

		// invoque la foncion showsOpenDialog pour montrer le save dialog
		int r = jFileChooser.showOpenDialog(null);

		// si l'utilisateur selectionne un fichier
		if (r == JFileChooser.APPROVE_OPTION) {
			file = jFileChooser.getSelectedFile();
			ExcelReader reader = new ExcelReader();
			try {
				sheet = reader.readExcel(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// met le label sur le chemin du fichier selectionné
			label.setText(jFileChooser.getSelectedFile().getAbsolutePath());

			String[][] data = generateDataToJTable(sheet);
			String[] columnNames = getStringArray(sheet.getHeaders());

			// cree la panel qui va contenir les pages generés
			tablePanel = new JPanel();
			tablePanel.setLayout(new GridBagLayout());
			gbcTable = new GridBagConstraints();
			gbcTable.gridwidth = GridBagConstraints.REMAINDER;
			gbcTable.fill = GridBagConstraints.HORIZONTAL;

			JTable table = new JTable(data, columnNames);
			table.setPreferredSize(new Dimension(300, 400));
			JScrollPane scrollpane = new JScrollPane(table);
			scrollpane.setPreferredSize(new Dimension(450, 110));
			tablePanel.add(scrollpane, gbcTable);

			panel.add(tablePanel, BorderLayout.WEST);

			// cree la panel qui contient les headers des excellsheets pour choisir les
			// identifiant, QI et données sensibles
			JPanel panelHeaders = new JPanel();
			panelHeaders.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			JLabel identifiantLabel = new JLabel("choisir les identifiants");
			panelHeaders.add(identifiantLabel, gbc);
			JList<String> identifiants = new JList<String>(columnNames);
			identifiants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			identifiants.setVisibleRowCount(5);
			panelHeaders.add(new JScrollPane(identifiants), gbc);

			JLabel quasiIdentifiantLabel = new JLabel("choisir les quasi identifiants");
			panelHeaders.add(quasiIdentifiantLabel, gbc);
			JList<String> quasiIdentifiants = new JList<String>(columnNames);
			quasiIdentifiants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			panelHeaders.add(new JScrollPane(quasiIdentifiants), gbc);

			JLabel dSensibleLabel = new JLabel("choisir les données sensibles");
			panelHeaders.add(dSensibleLabel, gbc);
			JList<String> dataSensible = new JList<String>(columnNames);
			dataSensible.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			panelHeaders.add(new JScrollPane(dataSensible), gbc);

			JLabel bucketSizeLabel = new JLabel("taille du backet");
			panelHeaders.add(bucketSizeLabel, gbc);
			JTextField bucketZise = new JTextField(6);
			panelHeaders.add(bucketZise, gbc);

			panel.add(panelHeaders, BorderLayout.CENTER);

			// crée le panneau contenant les boutons d'anonymisation
			JPanel panelAnonimizations = new JPanel();
			panelAnonimizations.setLayout(new GridBagLayout());
			GridBagConstraints gbcA = new GridBagConstraints();
			gbcA.gridwidth = GridBagConstraints.REMAINDER;
			gbcA.fill = GridBagConstraints.HORIZONTAL;

			JButton pseudonymizerButton = new JButton("Pseudonymiser");

			pseudonymizerButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					Pseudonymizer pseudonymizer = new Pseudonymizer();
					if (identifiants.getSelectedValuesList().isEmpty()) {
						JOptionPane.showMessageDialog(frame, "spécifier les identifiants!");
					} else {
						try {
							sheet = reader.readExcel(file);
							ExcelSheet anonymizedSheet = pseudonymizer.pseudonumize(sheet,
									identifiants.getSelectedValuesList());
							String[][] data = generateDataToJTable(anonymizedSheet);
							String[] columnNames = getStringArray(anonymizedSheet.getHeaders());
							refreshTable(data, columnNames);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});

			panelAnonimizations.add(pseudonymizerButton, gbcA);

			JButton bucketiserButton = new JButton("Bucketiser");

			panelAnonimizations.add(bucketiserButton, gbcA);

			JButton isLDiverserButton = new JButton("isLDiverse");
			panelAnonimizations.add(isLDiverserButton, gbcA);

			isLDiverserButton.setVisible(false);

			JTextField isLDiverserTextField = new JTextField("l value");
			panelAnonimizations.add(isLDiverserTextField, gbcA);
			isLDiverserTextField.setVisible(false);

			JLabel isLDiverserResult = new JLabel();
			panelAnonimizations.add(isLDiverserResult, gbcA);

			isLDiverserResult.setVisible(false);

			bucketiserButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (identifiants.getSelectedValuesList().isEmpty() || dataSensible.getSelectedValuesList().isEmpty()
							|| bucketZise.getText().isEmpty()) {
						JOptionPane.showMessageDialog(frame,
								"vérifier les identifiants, les données sensibles ou le bucket size!");
					} else {
						try {
							sheet = reader.readExcel(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						Backetization backetization = new Backetization();
						List<ExcelSheet> DS_QID = backetization.kAnonymize(sheet, identifiants.getSelectedValuesList(),
								dataSensible.getSelectedValuesList(), Integer.valueOf(bucketZise.getText()));
						// ajoute DS au panneau de tableau
						DS = DS_QID.get(0);
						String[][] data = generateDataToJTable(DS);
						String[] columnNames = getStringArray(DS.getHeaders());
						refreshTable(data, columnNames);

						// ajoute le QID au panneau de table
						ExcelSheet QID = DS_QID.get(1);
						data = generateDataToJTable(QID);
						columnNames = getStringArray(QID.getHeaders());
						refreshTable(data, columnNames);

						// affiche le bouton isLDiverse pour que nous puissions tester si DS est l diverse
						isLDiverserButton.setVisible(true);
						isLDiverserTextField.setVisible(true);
						isLDiverserResult.setVisible(true);
					}
				}
			});

			isLDiverserButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (dataSensible.getSelectedValuesList().isEmpty() || bucketZise.getText().isEmpty()
							|| isLDiverserTextField.getText().isEmpty()) {
						JOptionPane.showMessageDialog(frame,
								"vérifier les donnés sensibles, le bucket size ou la valeur L!");
					} else {

						Backetization backetization = new Backetization();

						boolean resutl = backetization.isLDiverse(DS, dataSensible.getSelectedValue(),
								Integer.valueOf(bucketZise.getText()), Integer.valueOf(isLDiverserTextField.getText()));
						isLDiverserResult.setText(String.valueOf(resutl));
					}
				}
			});

			JButton unidimensionnel = new JButton("algo 1 - unidim");

			unidimensionnel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (identifiants.getSelectedValuesList().isEmpty()
							|| quasiIdentifiants.getSelectedValuesList().isEmpty() || bucketZise.getText().isEmpty()) {
						JOptionPane.showMessageDialog(frame,
								"vérifier les identifiants, les quasi identifiants ou le bucket size!");
					} else {
						try {
							sheet = reader.readExcel(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						Unidimensionnel unidimensionnel = new Unidimensionnel();
						ExcelSheet anonymizedSheet = unidimensionnel.anonyme(sheet,
								quasiIdentifiants.getSelectedValue(), identifiants.getSelectedValuesList(),
								Integer.valueOf(bucketZise.getText()));
						String[][] data = generateDataToJTable(anonymizedSheet);
						String[] columnNames = getStringArray(anonymizedSheet.getHeaders());
						refreshTable(data, columnNames);
					}
				}
			});

			panelAnonimizations.add(unidimensionnel, gbcA);

			JButton multidimensionnel = new JButton("algo 1 - multidim");

			JLabel quasiIdOrderLabel = new JLabel("ordre des attributs");
			panelAnonimizations.add(quasiIdOrderLabel, gbcA);
			JTextField ordreAttribut = new JTextField(6);
			panelAnonimizations.add(ordreAttribut, gbcA);

			multidimensionnel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (identifiants.getSelectedValuesList().isEmpty() || ordreAttribut.getText().isEmpty()
							|| bucketZise.getText().isEmpty()) {
						JOptionPane.showMessageDialog(frame,
								"vérifier les identifiants, les quasi identifiants renseignés dans le textfield ou le bucket size!");
					} else {
						try {
							sheet = reader.readExcel(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						String[] ordreQuasiId = ordreAttribut.getText().split(",");

						List<String> quasiIds = new ArrayList<>();
						for (String quasiId : ordreQuasiId) {
							quasiIds.add(quasiId.trim());
						}

						Multidimensionnel multidimensionnel = new Multidimensionnel(sheet,
								identifiants.getSelectedValuesList(), quasiIds);
						ExcelSheet anonymizedSheet = multidimensionnel.anonymize(Integer.valueOf(bucketZise.getText()));
						String[][] data = generateDataToJTable(anonymizedSheet);
						String[] columnNames = getStringArray(anonymizedSheet.getHeaders());

						refreshTable(data, columnNames);
					}
				}
			});

			panelAnonimizations.add(multidimensionnel, gbcA);

			panel.add(panelAnonimizations, BorderLayout.EAST);

		}
		// si l'utilisateur annule  une opération
		else
			label.setText("the user cancelled the operation");

	}

	protected void refreshTable(String[][] data, String[] columnNames) {
		JTable tableAno = new JTable(data, columnNames);

		tableAno.setPreferredSize(new Dimension(300, 400));
		JScrollPane scrollpane = new JScrollPane(tableAno);
		scrollpane.setPreferredSize(new Dimension(450, 110));
		tablePanel.add(scrollpane, gbcTable);
		tablePanel.repaint();
		tablePanel.revalidate();
	}

	// Fonction pour convertire ArrayList<String> a String[]
	public static String[] getStringArray(List<String> arr) {

		// declaration et initialise String Array
		String str[] = new String[arr.size()];

		// Conversion de ArrayList a Array
		for (int j = 0; j < arr.size(); j++) {

			// mets chaque valeur au String array
			str[j] = arr.get(j);
		}

		return str;
	}

	public String[][] generateDataToJTable(ExcelSheet generatedSheet) {
		String[][] data = new String[generatedSheet.getNbOfRows() + 1][generatedSheet.getHeaders().size()];
		for (int i = 0; i < generatedSheet.getNbOfRows(); i++) {
			List<Object> row = generatedSheet.rows.get(i);
			for (int j = 0; j < generatedSheet.getHeaders().size(); j++) {
				if (row.get(j) instanceof Cell) {
					data[i][j] = getCellValue((Cell) row.get(j));
				} else {
					data[i][j] = (String) row.get(j);
				}
			}
		}
		return data;
	}

	private String getCellValue(Cell cell) {
		String cellValue = "";
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				cellValue = (String) cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				cellValue = String.valueOf(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_NUMERIC:
				cellValue = String.valueOf(cell.getNumericCellValue());
				break;
			default:
		}
		return cellValue;
	}

}
