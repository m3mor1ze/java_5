import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private final JCheckBoxMenuItem showAxisMenuItem;
    private final JCheckBoxMenuItem showMarkersMenuItem;
    private final JCheckBoxMenuItem rotateMenuItem;
    private final JCheckBoxMenuItem showAxisMenuItem2;
    private final JCheckBoxMenuItem showMarkersMenuItem2;
    private final JCheckBoxMenuItem rotateMenuItem2;
    private final GraphicsDisplay display = new GraphicsDisplay();
    private JFileChooser fileChooser = null;
    private boolean file1Loaded = false;
    private boolean file2Loaded = false;

    public MainFrame() {
        super("Построение графиков функций на основе заранее подготовленных файлов");
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH) / 2, (kit.getScreenSize().height - HEIGHT) / 2);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком 1") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile(), false);


            }
        };
        fileMenu.add(openGraphicsAction);
        Action openSecondGraphicsAction = new AbstractAction("Открыть файл с графиком 2") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile(), true);
            }
        };
        fileMenu.add(openSecondGraphicsAction);

        JMenu graphicsMenu = new JMenu("График 1");
        menuBar.add(graphicsMenu);
        Action showAxisAction = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent event) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);
        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);
        Action rotateAction = new AbstractAction("Повернуть график на 90") {
            public void actionPerformed(ActionEvent event) {
                display.setRotation(rotateMenuItem.isSelected());
            }
        };
        rotateMenuItem = new JCheckBoxMenuItem(rotateAction);
        graphicsMenu.add(rotateMenuItem);
        graphicsMenu.addMenuListener(new GraphicsMenuListener());
        Action saveToGraphicsAction = new AbstractAction("Сохранить график") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    display.saveToGraphicsFile(fileChooser.getSelectedFile(), false);
            }
        };
        graphicsMenu.add(saveToGraphicsAction);

        JMenu graphicsMenu2 = new JMenu("График 2");
        menuBar.add(graphicsMenu2);
        Action showAxisAction2 = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent event) {
                display.setShowAxis2(showAxisMenuItem2.isSelected());
            }
        };
        showAxisMenuItem2 = new JCheckBoxMenuItem(showAxisAction2);
        graphicsMenu2.add(showAxisMenuItem2);
        showAxisMenuItem2.setSelected(true);
        Action showMarkersAction2 = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent event) {
                display.setShowMarkers2(showMarkersMenuItem2.isSelected());
            }
        };
        showMarkersMenuItem2 = new JCheckBoxMenuItem(showMarkersAction2);
        graphicsMenu2.add(showMarkersMenuItem2);
        showMarkersMenuItem2.setSelected(true);
        Action rotateAction2 = new AbstractAction("Повернуть график на 90") {
            public void actionPerformed(ActionEvent event) {
                display.setRotation2(rotateMenuItem2.isSelected());
            }
        };
        rotateMenuItem2 = new JCheckBoxMenuItem(rotateAction2);
        graphicsMenu2.add(rotateMenuItem2);
        JCheckBoxMenuItem showSecondGraphMenuItem = new JCheckBoxMenuItem("Отобразить второй график");
        graphicsMenu2.add(showSecondGraphMenuItem);
        showSecondGraphMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                display.setDisplaySecond(showSecondGraphMenuItem.isSelected());
            }
        });
        graphicsMenu2.addMenuListener(new GraphicsMenuListener());
        Action saveToGraphicsAction2 = new AbstractAction("Сохранить график") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    display.saveToGraphicsFile(fileChooser.getSelectedFile(), true);
            }
        };
        graphicsMenu2.add(saveToGraphicsAction2);

        getContentPane().add(display, BorderLayout.CENTER);
    }

    protected void openGraphics(File selectedFile, boolean whichOne)  {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            Double[][] graphicsData = new Double[in.available() / (Double.SIZE / 8) / 2][];
            int i = 0;
            while (in.available() > 0) {
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData[i++] = new Double[]{x, y};
            }

            if (graphicsData != null && graphicsData.length > 0) {
                if (!whichOne) {
                    file1Loaded = true;
                    display.showGraphics(graphicsData, null);
                }
                if (whichOne) {
                    file2Loaded = true;
                    display.showGraphics(null, graphicsData);
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        }
        repaint();
    }

    private class GraphicsMenuListener implements MenuListener {
        public void menuSelected(MenuEvent e) {
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
    }
}