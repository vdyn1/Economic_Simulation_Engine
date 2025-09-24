import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Menu extends JFrame {

    String modelpack = "/Users/v.d.o_/IntellijIdeaProjects/prefinal/src/model";
    String datapack = "/Users/v.d.o_/IntellijIdeaProjects/prefinal/src/data";
    String scriptpack = "/Users/v.d.o_/IntellijIdeaProjects/prefinal/src/script";

    private JList<String> modelList;
    private JList<String> dataList;

    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private UniversalController controller;
    private Object currentModel;

    public Menu() {
        setTitle("modelling");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        controller = new UniversalController();

        modelList = new JList<>(getModelNames(modelpack));
        JScrollPane modelScrollPane = new JScrollPane(modelList);

        dataList = new JList<>(getDataFiles(datapack));
        JScrollPane dataScrollPane = new JScrollPane(dataList);

        JButton runButton = new JButton("Run model");
        runButton.addActionListener(new RunModelActionListener());

        JPanel selectionPanel = new JPanel(new GridLayout(1, 3));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select model and data"));
        selectionPanel.add(modelScrollPane);
        selectionPanel.add(dataScrollPane);
        selectionPanel.add(runButton);
        add(selectionPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel();
        resultsTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        add(tableScrollPane, BorderLayout.CENTER);


        JPanel scriptPanel = new JPanel(new FlowLayout());
        scriptPanel.setBorder(BorderFactory.createTitledBorder("Scripts"));

        JButton runScriptFileButton = new JButton("Run script from file");
        scriptPanel.add(runScriptFileButton);
        runScriptFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentModel == null) {
                    return;
                }
                JFileChooser chooser = new JFileChooser(scriptpack);
                if (chooser.showOpenDialog(Menu.this) == JFileChooser.APPROVE_OPTION) {
                    File scriptFile = chooser.getSelectedFile();
                    controller.filescript(scriptFile.getAbsolutePath());
                    String results = controller.resulttable(currentModel);
                    updateTable(results);
                }
            }
        });

        JButton runAdHocScriptButton = new JButton("Create and run ad-hoc script");
        scriptPanel.add(runAdHocScriptButton);
        runAdHocScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentModel == null) {
                    return;
                }
                String scriptCode = showScriptEditor();
                if (scriptCode != null && !scriptCode.trim().isEmpty()) {
                        controller.runScript(scriptCode);
                        String results = controller.resulttable(currentModel);
                        updateTable(results);
                }
            }
        });
        add(scriptPanel, BorderLayout.NORTH);
    }

    private String showScriptEditor() {
        JDialog dialog = new JDialog(this, "Script Writer ",true);
        dialog.setSize(300, 600);
        dialog.setLayout(new BorderLayout());

        JTextArea scriptTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(scriptTextArea);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton runButton = new JButton("Run");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(runButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        String[] scriptCode = new String[1];

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scriptCode[0] = scriptTextArea.getText();
                dialog.dispose();
                System.out.println("run entered ");
                System.out.println();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scriptCode[0] = null;
                dialog.dispose();
                System.out.println("cancel entered ");
            }
        });
        dialog.setVisible(true);
        return scriptCode[0];
    }

    private String[] getModelNames(String modelPath) {
        File modelFolder = new File(modelPath);
        File[] files = modelFolder.listFiles();


        java.util.List<String> modelNames = new ArrayList<>();
        for (File f : files) {
            String fname = f.getName();
            if (fname.toLowerCase().endsWith(".java")) {
                String base = fname.substring(0, fname.length() - 5);
                modelNames.add(base);
            }
        }
        return modelNames.toArray(new String[0]);
    }


    private String[] getDataFiles(String dataDirPath) {
        File dataFolder = new File(dataDirPath);
        File[] files = dataFolder.listFiles();
        if (files == null) return new String[0];

        java.util.List<String> datanames = new ArrayList<>();
        for (File f : files) {
            String fname = f.getName();
            if (fname.toLowerCase().endsWith(".txt")) {
                String base = fname.substring(0, fname.length() - 4);
                datanames.add(base);
            }
        }
        return datanames.toArray(new String[0]);
    }

    private void updateTable(String tsv) {
        String[] rows = tsv.split("\n");
        if (rows.length == 0) {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            return;
        }
        String[] columns = rows[0].split("\t");

        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);

        for (int i = 1; i < rows.length; i++) {
            String[] rowData = rows[i].split("\t");
            tableModel.addRow(rowData);
        }
    }

    private class RunModelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String selectedModelName = modelList.getSelectedValue();
            String selectedDataFile = dataList.getSelectedValue();

            if (selectedModelName == null || selectedDataFile == null) {
                return;
            }


            String fullModelName = "model." + selectedModelName;
            String fullPath = datapack + "/" + selectedDataFile + ".txt";

            try {
                currentModel = Class.forName(fullModelName)
                        .getDeclaredConstructor().newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }

            controller.setdata(fullPath);
            controller.setmodel(currentModel);
            controller.runModel(currentModel);
            String results = controller.resulttable(currentModel);
            updateTable(results);


        }
    }

    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.setVisible(true);
    }
}