import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
* @author Iancovici Teodor
*/

public class MovieApp {
    private static Document document;
    private static JFrame frame;
    private static Container contentPane;
    private static CardLayout cardLayout;

    /** Cream 2 fonturi*/
    static Font mainFont = new Font("", Font.BOLD, 20);
    static Font Font2 = new Font("", Font.BOLD, 16);
    
    public static void main(String[] args) throws Exception {
        /** Analizam fisierul XML*/
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse("movies.xml");

        /** Login Window*/
        JFrame loginFrame = new JFrame("Autentificare");
        loginFrame.setSize(300, 200);
        loginFrame.setLayout(new GridLayout(3, 2));
        loginFrame.setLocationRelativeTo(null);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        loginFrame.add(new JLabel("Username:"){{setHorizontalAlignment(JLabel.CENTER);}});
        loginFrame.add(usernameField);
        loginFrame.add(new JLabel("Parola:"){{setHorizontalAlignment(JLabel.CENTER);}});
        loginFrame.add(passwordField);

        JButton loginButton = new JButton("Autentificare");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            /** Accesam Username-ul si Parola din fisierul XML*/
            /** Username-ul ales este "Admin", iar Parola este "12345".*/
            String correctUsername = document.getElementsByTagName("username").item(0).getTextContent().trim();
            String correctPassword = document.getElementsByTagName("password").item(0).getTextContent().trim();

            /** Verificam daca Username-ul si Parola sunt corecte.*/
            if (username.equals(correctUsername) && password.equals(correctPassword)) {
                JOptionPane.showMessageDialog(loginFrame, "Autentificare reușită!");
                loginFrame.dispose();
                /** Cream o Interfata Grafica.*/
                frame = new JFrame("Movie App");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                /** Cream o colectie cu aspectul de CardLayout.*/
                contentPane = frame.getContentPane();
                cardLayout = new CardLayout();
                contentPane.setLayout(cardLayout);

                /** Initial GUI Setup*/
                refreshGUI();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Username sau Parola incorectă.");
            }
        });
        loginFrame.add(loginButton);

        loginFrame.setVisible(true);
    }

    public static void refreshGUI() {
        /** Scoatem toate componentele din colectia "contentPane".*/
        contentPane.removeAll();
        /** Cautam toate elementele din fisierul XML care au eticheta "film".*/
        NodeList nodeList = document.getElementsByTagName("film");
        /** Cream o pagina pentru fiecare film din colectie.*/
        for (int i = 0; i < nodeList.getLength(); i++) {
            final int finalI = i;
            Node node = nodeList.item(finalI);
            JPanel card = new JPanel(new BorderLayout());
            /** Cream un Panel care va contine mai multe butoane si va fi pozitionat in partea dreapta a paginii.*/
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); /** Setarea layout-ului la FlowLayout*/
            /** Cream un Label dupa numele filmului respectiv.*/
            JLabel titlu = new JLabel(((Element) node).getElementsByTagName("nume").item(0).getTextContent().trim());
            titlu.setHorizontalAlignment(JLabel.CENTER);
            titlu.setFont(mainFont);

            /** Adaugam o lista cu actori.*/
            NodeList actorNodes = ((Element) node).getElementsByTagName("actor");
            DefaultListModel<String> listModel = new DefaultListModel<>();
            for (int j = 0; j < actorNodes.getLength(); j++) {
                final int finalJ = j;
                listModel.addElement(actorNodes.item(finalJ).getTextContent().trim());
            }
            JList<String> actorList = new JList<>(listModel);
            actorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            actorList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selectedActor = actorList.getSelectedValue();
                    actorList.clearSelection();
                    /** Cream o pagina noua, unde apar toate filmele in care a aparut actorul ales.*/
                    JPanel actorCard = new JPanel(new BorderLayout());
                    JLabel actorLabel = (new JLabel("Filme cu " + selectedActor));
                    actorLabel.setFont(mainFont);
                    actorLabel.setHorizontalAlignment(JLabel.CENTER);
                    actorCard.add(actorLabel, BorderLayout.NORTH);
                    JPanel moviePanel = new JPanel();
                    NodeList allFilmNodes = document.getElementsByTagName("film");
                    for (int k = 0; k < allFilmNodes.getLength(); k++) {
                        Node filmNode = allFilmNodes.item(k);
                        NodeList filmActorNodes = ((Element) filmNode).getElementsByTagName("actor");
                        for (int l = 0; l < filmActorNodes.getLength(); l++) {
                            if (filmActorNodes.item(l).getTextContent().trim().equals(selectedActor)) {
                                JButton movieButton = new JButton(((Element) filmNode).getElementsByTagName("nume").item(0).getTextContent().trim());
                                final int finalK = k;
                                movieButton.addActionListener(ev -> cardLayout.show(contentPane, "Film " + (finalK + 1)));
                                moviePanel.add(movieButton);
                                break;
                            }
                        }
                    }
                    actorCard.add(moviePanel, BorderLayout.CENTER);
                    /** Adaugam un buton "Home", care ne va redirectiona la Pagina Principala.*/
                    JButton homeButton = new JButton("Home");
                    homeButton.addActionListener(ev -> cardLayout.show(contentPane, "Home"));
                    actorCard.add(homeButton, BorderLayout.SOUTH);
                    contentPane.add(actorCard, "Actor " + (finalI + 1) + " " + (actorList.getSelectedIndex() + 1));
                    cardLayout.show(contentPane, "Actor " + (finalI + 1) + " " + (actorList.getSelectedIndex() + 1));
                }
            });

            /** Adaugam o lista cu regizori.*/
            NodeList directorNodes = ((Element) node).getElementsByTagName("regizor");
            DefaultListModel<String> listModel2 = new DefaultListModel<>();
            for (int j = 0; j < directorNodes.getLength(); j++) {
                final int finalJ = j;
                listModel2.addElement(directorNodes.item(finalJ).getTextContent().trim());
            }
            JList<String> directorList = new JList<>(listModel2);
            directorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            directorList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selectedDirector = directorList.getSelectedValue();
                    directorList.clearSelection();
                    /** Cream o pagina noua, unde apar toate filmele regizate de regizorul ales.*/
                    JPanel directorCard = new JPanel(new BorderLayout());
                    JLabel directorLabel = (new JLabel("Filme regizate de " + selectedDirector));
                    directorLabel.setFont(mainFont);
                    directorLabel.setHorizontalAlignment(JLabel.CENTER);
                    directorCard.add(directorLabel, BorderLayout.NORTH);
                    JPanel moviePanel = new JPanel();
                    NodeList allFilmNodes = document.getElementsByTagName("film");
                    for (int k = 0; k < allFilmNodes.getLength(); k++) {
                        Node filmNode = allFilmNodes.item(k);
                        NodeList filmDirectorNodes = ((Element) filmNode).getElementsByTagName("regizor");
                        for (int l = 0; l < filmDirectorNodes.getLength(); l++) {
                            if (filmDirectorNodes.item(l).getTextContent().trim().equals(selectedDirector)) {
                                JButton movieButton = new JButton(((Element) filmNode).getElementsByTagName("nume").item(0).getTextContent().trim());
                                final int finalK = k;
                                movieButton.addActionListener(ev -> cardLayout.show(contentPane, "Film " + (finalK + 1)));
                                moviePanel.add(movieButton);
                                break;
                            }
                        }
                    }
                    directorCard.add(moviePanel, BorderLayout.CENTER);
                    /** Adaugam un buton "Home", care ne va redirectiona la Pagina Principala.*/
                    JButton homeButton = new JButton("Home");
                    homeButton.addActionListener(ev -> cardLayout.show(contentPane, "Home"));
                    directorCard.add(homeButton, BorderLayout.SOUTH);
                    contentPane.add(directorCard, "Regizor " + (finalI + 1) + " " + (actorList.getSelectedIndex() + 1));
                    cardLayout.show(contentPane, "Regizor " + (finalI + 1) + " " + (actorList.getSelectedIndex() + 1));
                }
            });

            /** Selectam fisierul filmului respectiv.*/
            String FilmName = ((Element) node).getElementsByTagName("file").item(0).getTextContent().trim();
            /** Adaugam un buton pentru redarea filmului.*/
            JButton playButton = new JButton("Play Movie");
            playButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFrame videoFrame = new JFrame("Video");
                    JFXPanel jfxPanel = new JFXPanel();
                    videoFrame.add(jfxPanel);
                    videoFrame.setSize(1280, 720);
                    videoFrame.setVisible(true);

                    Platform.runLater(() -> {
                        /** Deschidem filmul.*/
                        File videoFile = new File(FilmName);
                        Media media = new Media(videoFile.toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(media);
                        MediaView mediaView = new MediaView(mediaPlayer);
                        /** Redimensionam videoclipul pentru a se potrivi cu dimensiunea ferestrei.*/
                        mediaView.setFitWidth(videoFrame.getWidth());
                        mediaView.setFitHeight(videoFrame.getHeight());
                        /** Adaugăm un ChangeListener la proprietatile widthProperty si heightProperty ale ferestrei
                        pentru a adjusta dimensiunile filmului automat atunci cand modificam dimensiunea ferestrei.*/
                        videoFrame.addComponentListener(new java.awt.event.ComponentAdapter() {
                            public void componentResized(java.awt.event.ComponentEvent e) {
                                Platform.runLater(() -> {
                                    mediaView.setFitWidth(videoFrame.getWidth());
                                    mediaView.setFitHeight(videoFrame.getHeight());
                                });
                            }
                        });

                        /** Adăugăm un Buton de Pauza.*/
                        Button pauseButton = new Button("Pauza");
                        pauseButton.setOnAction(event -> {
                            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                                mediaPlayer.pause();
                            } else {
                                mediaPlayer.play();
                            }
                        });

                        /** Adăugăm un Slider, pentru a selecta un anumit moment din film.*/
                        Slider timeSlider = new Slider();
                        timeSlider.setMin(0);
                        timeSlider.setMax(1);
                        timeSlider.setValue(0);
                        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                            timeSlider.setValue(newValue.toSeconds() / media.getDuration().toSeconds());
                        });
                        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                            if (timeSlider.isValueChanging()) {
                                mediaPlayer.seek(Duration.seconds(newValue.doubleValue() * media.getDuration().toSeconds()));
                            }
                        });

                        /** Adăugăm toate elementele in VBox.*/
                        VBox vbox = new VBox(pauseButton, timeSlider, mediaView);
                        Scene scene = new Scene(vbox);
                        jfxPanel.setScene(scene);
                        mediaPlayer.play();

                        videoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                                mediaPlayer.stop();
                            }
                        });
                    });
                }
            });
            /** Adaugam Butonul la "buttonPanel".*/
            buttonPanel.add(playButton);

            /** Adaugam un camp de cautare, pentru a gasi mai usor filmul dorit.*/
            JTextField searchField = new JTextField(10);
            searchField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String searchText = searchField.getText();
                    boolean found = false;
                    for (int j = 0; j < nodeList.getLength(); j++) {
                        Node filmNode = nodeList.item(j);
                        if (filmNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element filmElement = (Element) filmNode;
                            NodeList nameList = filmElement.getElementsByTagName("nume");
                            if (nameList.getLength() > 0) {
                                Node nameNode = nameList.item(0);
                                if (nameNode.getTextContent().equals(searchText)) {
                                    cardLayout.show(contentPane, "Film " + (j + 1));
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (found) {
                        searchField.setText("");
                    }
                }
            });
            /** Adaugam campul de cautare la "buttonPanel".*/
            JLabel searchLabel = new JLabel("Caută un film:");
            buttonPanel.add(searchLabel);
            buttonPanel.add(searchField);

            /** Adaugam un buton care permite modificarea filmului selectat.*/
            JButton modifyButton = new JButton("Modifică film");
            modifyButton.addActionListener(e -> {
                /** Deschidem o fereastra noua.*/
                JFrame modifyFrame = new JFrame("Modifică film");
                modifyFrame.setSize(700, 400);
                /** Aranjam noua fereastra pe 10 randuri si 2 coloane.*/
                modifyFrame.setLayout(new GridLayout(10, 2));

                /** Cautam informatiile actuale ale filmului selectat in fisierul XML.*/
                String currentName = ((Element) node).getElementsByTagName("nume").item(0).getTextContent().trim();
                String currentCategory = ((Element) node).getElementsByTagName("categorie").item(0).getTextContent().trim();
                String currentData = ((Element) node).getElementsByTagName("data").item(0).getTextContent().trim();
                String currentRating = ((Element) node).getElementsByTagName("rating").item(0).getTextContent().trim();
                String currentID = ((Element) node).getElementsByTagName("id").item(0).getTextContent().trim();
                String currentDirector = ((Element) node).getElementsByTagName("regizor").item(0).getTextContent().trim();
                NodeList currentActorsNodes = ((Element) node).getElementsByTagName("actor");
                /** Actorii sunt separati prin ", " si au fiecare cate o eticheta <actor>, pentru a le putea atribui fiecaruia cate o pagina. */
                StringBuilder currentActors = new StringBuilder();
                for (int j = 0; j < currentActorsNodes.getLength(); j++) {
                    currentActors.append(currentActorsNodes.item(j).getTextContent().trim());
                    if (j < currentActorsNodes.getLength() - 1) {
                        currentActors.append(", ");
                    }
                }
                String currentDescription = ((Element) node).getElementsByTagName("descriere").item(0).getTextContent().trim();
                String currentFile = ((Element) node).getElementsByTagName("file").item(0).getTextContent().trim();

                /** Completam campurile de text cu informatiile actuale ale filmului.*/
                JTextField nameField = new JTextField(currentName);
                JTextField categoryField = new JTextField(currentCategory);
                JTextField dataField = new JTextField(currentData);
                JTextField ratingField = new JTextField(currentRating);
                JTextField idField = new JTextField(currentID);
                JTextField directorField = new JTextField(currentDirector);
                JTextField actorsField = new JTextField(currentActors.toString());
                JTextField descriptionField = new JTextField(currentDescription.toString());
                JTextField fileField = new JTextField(currentFile.toString());

                /** Adaugam elementele in noua fereastra.*/
                modifyFrame.add(new JLabel("Nume:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(nameField);
                modifyFrame.add(new JLabel("Categorie:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(categoryField);
                modifyFrame.add(new JLabel("Data:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(dataField);
                modifyFrame.add(new JLabel("Rating:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(ratingField);
                modifyFrame.add(new JLabel("Id IMDB:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(idField);
                modifyFrame.add(new JLabel("Regizor:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(directorField);
                modifyFrame.add(new JLabel("Actori:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(actorsField);
                modifyFrame.add(new JLabel("Descriere:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(descriptionField);
                modifyFrame.add(new JLabel("Fisier:"){{setHorizontalAlignment(JLabel.CENTER);}});
                modifyFrame.add(fileField);

                /** Adaugam un buton de trimitere.*/
                JButton submitButton = new JButton("Trimite");
                submitButton.addActionListener(ev -> {
                    /** Modificam datele filmului in colectie.*/
                    ((Element) node).getElementsByTagName("nume").item(0).setTextContent(nameField.getText());
                    ((Element) node).getElementsByTagName("categorie").item(0).setTextContent(categoryField.getText());
                    ((Element) node).getElementsByTagName("data").item(0).setTextContent(dataField.getText());
                    ((Element) node).getElementsByTagName("rating").item(0).setTextContent(ratingField.getText());
                    ((Element) node).getElementsByTagName("id").item(0).setTextContent(idField.getText());
                    ((Element) node).getElementsByTagName("regizor").item(0).setTextContent(directorField.getText());
                    Element actoriElement = document.createElement("actori");
                    String[] actors = actorsField.getText().split(", ");
                    for (String actor : actors) {
                        Element actorElement = document.createElement("actor");
                        actorElement.appendChild(document.createTextNode(actor));
                        actoriElement.appendChild(actorElement);
                    }
                    node.replaceChild(actoriElement, ((Element) node).getElementsByTagName("actori").item(0));
                    ((Element) node).getElementsByTagName("descriere").item(0).setTextContent(descriptionField.getText());
                    ((Element) node).getElementsByTagName("file").item(0).setTextContent(fileField.getText());

                    /** Salvam modificarile in fisierul XML.*/
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = null;
                    try {
                        transformer = transformerFactory.newTransformer();
                    } catch (TransformerConfigurationException transformerConfigurationException) {
                        transformerConfigurationException.printStackTrace();
                    }
                    DOMSource source = new DOMSource(document);
                    StreamResult result = new StreamResult("movies.xml");
                    try {
                        transformer.transform(source, result);
                    } catch (TransformerException transformerException) {
                        transformerException.printStackTrace();
                    }

                    /** Dam refresh la GUI si revenim la pagina principala (Home).*/
                    refreshGUI();

                    /** Inchidem fereastra de modificare.*/
                    modifyFrame.dispose();
                });
                modifyFrame.add(submitButton);

                modifyFrame.setVisible(true);
            });
            /** Adaugam Butonul la "buttonPanel".*/
            buttonPanel.add(modifyButton);

            /** Adaugam un buton pentru stergerea filmului selectat.*/
            JButton deleteButton = new JButton("Șterge film");
            deleteButton.addActionListener(e -> {
                /** Sterge filmul din colectie.*/
                document.getDocumentElement().removeChild(node);

                /** Salvam modificarile in fisierul XML.*/
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = null;
                try {
                    transformer = transformerFactory.newTransformer();
                } catch (TransformerConfigurationException transformerConfigurationException) {
                    transformerConfigurationException.printStackTrace();
                }
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult("movies.xml");
                try {
                    transformer.transform(source, result);
                } catch (TransformerException transformerException) {
                    transformerException.printStackTrace();
                }

                /** Dam refresh la GUI si revenim la pagina principala (Home).*/
                refreshGUI();
            });
            /** Adaugam Butonul la "buttonPanel".*/
            buttonPanel.add(deleteButton);

            /** Adaugam in pagina detaliile filmului.*/
            /** Cream un nou Panel, care are aspectul "GridBagLayout".*/
            JPanel filmPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;

            JLabel categoryField = new JLabel(((Element) node).getElementsByTagName("categorie").item(0).getTextContent().trim());
            categoryField.setFont(Font2);
            JLabel dataField = new JLabel(((Element) node).getElementsByTagName("data").item(0).getTextContent().trim());
            dataField.setFont(Font2);
            JLabel ratingField = new JLabel(((Element) node).getElementsByTagName("rating").item(0).getTextContent().trim());
            ratingField.setFont(Font2);
            JLabel idField = new JLabel(((Element) node).getElementsByTagName("id").item(0).getTextContent().trim());
            idField.setFont(Font2);
            JTextArea descriptionField = new JTextArea(((Element) node).getElementsByTagName("descriere").item(0).getTextContent().trim());
            descriptionField.setLineWrap(true);
            descriptionField.setWrapStyleWord(true);
            descriptionField.setFont(Font2);
            descriptionField.setEditable(false);

            gbc.gridheight = 1;
            gbc.gridy = 0;
            gbc.weightx = 0.2;
            filmPanel.add(new JLabel("Categorie:"){{setFont(mainFont); setHorizontalAlignment(JLabel.CENTER);}}, gbc);
            gbc.weightx = 0.8;
            filmPanel.add(categoryField, gbc);
            gbc.gridy = 1;
            gbc.weightx = 0.2;
            filmPanel.add(new JLabel("Data:"){{setFont(mainFont); setHorizontalAlignment(JLabel.CENTER);}}, gbc);
            gbc.weightx = 0.8;
            filmPanel.add(dataField, gbc);
            gbc.gridy = 2;
            gbc.weightx = 0.2;
            filmPanel.add(new JLabel("Rating:"){{setFont(mainFont); setHorizontalAlignment(JLabel.CENTER);}}, gbc);
            gbc.weightx = 0.8;
            filmPanel.add(ratingField, gbc);
            gbc.gridy = 3;
            gbc.weightx = 0.2;
            filmPanel.add(new JLabel("ID-ul IMDB:"){{setFont(mainFont); setHorizontalAlignment(JLabel.CENTER);}}, gbc);
            gbc.weightx = 0.8;
            filmPanel.add(idField, gbc);
            gbc.gridy = 4;
            gbc.weightx = 0.2;
            filmPanel.add(new JLabel("Actori:"){{setFont(mainFont); setHorizontalAlignment(JLabel.CENTER);}}, gbc);
            gbc.weightx = 0.8;
            JScrollPane actorScrollPane = new JScrollPane(actorList);
            actorScrollPane.setPreferredSize(new Dimension(100, 150));
            filmPanel.add(actorScrollPane, gbc);
            gbc.gridy = 5;
            gbc.gridx = 0;
            gbc.weightx = 0.2;
            filmPanel.add(new JLabel("Regizor:"){{setFont(mainFont); setHorizontalAlignment(JLabel.CENTER);}}, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            JScrollPane directorScrollPane = new JScrollPane(directorList);
            directorScrollPane.setPreferredSize(new Dimension(100, 10));
            filmPanel.add(directorScrollPane, gbc);
            gbc.gridy = 6;
            gbc.gridx = 0;
            gbc.weightx = 0.2;
            filmPanel.add(new JLabel("Descriere:"){{setFont(mainFont); setHorizontalAlignment(JLabel.CENTER);}}, gbc);
            gbc.weightx = 0.8;
            gbc.gridheight = 3;
            gbc.gridx = 1;
            descriptionField.setPreferredSize(new Dimension(300, 50));
            filmPanel.add(descriptionField, gbc);

            /** Cream un nou Panel pentru a adăuga titlul și buttonPanel.*/
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(titlu, BorderLayout.NORTH);
            topPanel.add(buttonPanel, BorderLayout.SOUTH);

            /** Adaugam componentele la card.*/
            card.add(topPanel, BorderLayout.PAGE_START);
            card.add(filmPanel, BorderLayout.CENTER);

            /** Adaugam un buton "Home", care ne va redirectiona la Pagina Principala.*/
            JButton homeButton = new JButton("Home");
            homeButton.addActionListener(ev -> cardLayout.show(contentPane, "Home"));
            card.add(homeButton, BorderLayout.SOUTH);
            /** Adaugam pagina filmului respectiv in colectie.*/
            contentPane.add(card, "Film " + (finalI + 1));
        }

        /** Adaugam o Pagina Principala (Home), care contine butoane pentru fiecare categorie.*/
        JPanel homeCard = new JPanel(new BorderLayout());
        JLabel HomeLabel = new JLabel("Home");
        HomeLabel.setFont(mainFont);
        HomeLabel.setHorizontalAlignment(JLabel.CENTER);
        homeCard.add(HomeLabel, BorderLayout.NORTH);
        JPanel categoryPanel1 = new JPanel();
        JPanel categoryPanel2 = new JPanel();
        /** Cream o pagina pentru fiecare categorie.*/
        String[] categories = {"Actiune", "Animatie", "Aventura", "Comedie", "Drama", "Horror", "Fantezie", "Istorie"};
        for (int i = 0; i < categories.length; i++) {
            final int finalI = i;
            JButton categoryButton = new JButton(categories[finalI]);
            categoryButton.setPreferredSize(new Dimension(140, 70));
            categoryButton.setFont(mainFont);
            categoryButton.addActionListener(e -> {
                /** Afisam o pagina cu toate filmele din categoria respectiva.*/
                JPanel categoryCard = new JPanel(new BorderLayout());
                JLabel label = new JLabel("Filme de " + categories[finalI]);
                label.setFont(mainFont);
                label.setHorizontalAlignment(JLabel.CENTER);
                categoryCard.add(label, BorderLayout.PAGE_START);
                JPanel moviePanel = new JPanel();
                NodeList allFilmNodes = document.getElementsByTagName("film");
                for (int j = 0; j < allFilmNodes.getLength(); j++) {
                    Node filmNode = allFilmNodes.item(j);
                    if (((Element) filmNode).getElementsByTagName("categorie").item(0).getTextContent().trim().equals(categories[finalI])) {
                        JButton movieButton = new JButton(((Element) filmNode).getElementsByTagName("nume").item(0).getTextContent().trim());
                        final int finalJ = j;
                        movieButton.addActionListener(ev -> cardLayout.show(contentPane, "Film " + (finalJ + 1)));
                        moviePanel.add(movieButton);
                    }
                }
                categoryCard.add(moviePanel, BorderLayout.CENTER);
                /** Adaugam un buton "Home", care ne va redirectiona la Pagina Principala.*/
                JButton homeButton = new JButton("Home");
                homeButton.addActionListener(ev -> cardLayout.show(contentPane, "Home"));
                categoryCard.add(homeButton, BorderLayout.SOUTH);
                contentPane.add(categoryCard, "Categorie " + (finalI + 1));
                cardLayout.show(contentPane, "Categorie " + (finalI + 1));
            });
            categoryPanel1.add(categoryButton);
        }

        /** Adaugam un camp de cautare, pentru a gasi mai usor filmul dorit.*/
        JTextField searchField = new JTextField(10);
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                boolean found = false;
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node filmNode = nodeList.item(j);
                    if (filmNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element filmElement = (Element) filmNode;
                        NodeList nameList = filmElement.getElementsByTagName("nume");
                        if (nameList.getLength() > 0) {
                            Node nameNode = nameList.item(0);
                            if (nameNode.getTextContent().equals(searchText)) {
                                cardLayout.show(contentPane, "Film " + (j + 1));
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (found) {
                    searchField.setText("");
                }
            }
        });
        categoryPanel2.add(new JLabel("Caută un film:"));
        categoryPanel2.add(searchField);

        /** Cream un buton care permite adaugarea unui nou film la colectie.*/
        JButton addButton = new JButton("Adaugă film");
        addButton.addActionListener(e -> {
            /**  Deschidem o noua fereastra.*/
            JFrame addFrame = new JFrame("Adaugă film");
            addFrame.setSize(700, 400);
            /** Aranjam noua fereastra pe 10 randuri si 2 coloane.*/
            addFrame.setLayout(new GridLayout(10, 2));

            JTextField nameField = new JTextField();
            JTextField categoryField = new JTextField();
            JTextField dataField = new JTextField();
            JTextField ratingField = new JTextField();
            JTextField idField = new JTextField();
            JTextField directorField = new JTextField();
            JTextField actorsField = new JTextField();
            JTextField descriptionField = new JTextField();
            JTextField fileField = new JTextField();

            addFrame.add(new JLabel("Nume:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(nameField);
            addFrame.add(new JLabel("Categorie:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(categoryField);
            addFrame.add(new JLabel("Data:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(dataField);
            addFrame.add(new JLabel("Rating:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(ratingField);
            addFrame.add(new JLabel("Id IMDB:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(idField);
            addFrame.add(new JLabel("Regizor:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(directorField);
            addFrame.add(new JLabel("Actori:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(actorsField);
            addFrame.add(new JLabel("Descriere:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(descriptionField);
            addFrame.add(new JLabel("Fisier:"){{setHorizontalAlignment(JLabel.CENTER);}});
            addFrame.add(fileField);

            /** Adaugam un buton de trimitere.*/
            JButton submitButton = new JButton("Trimite");
            submitButton.addActionListener(ev -> {
                /** Adaugam filmul la colectie si ii cream o pagina.*/
                Element film = document.createElement("film");
                Element nume = document.createElement("nume");
                nume.appendChild(document.createTextNode(nameField.getText()));
                film.appendChild(nume);
                Element categorie = document.createElement("categorie");
                categorie.appendChild(document.createTextNode(categoryField.getText()));
                film.appendChild(categorie);
                Element data = document.createElement("data");
                data.appendChild(document.createTextNode(dataField.getText()));
                film.appendChild(data);
                Element rating = document.createElement("rating");
                rating.appendChild(document.createTextNode(ratingField.getText()));
                film.appendChild(rating);
                Element id = document.createElement("id");
                id.appendChild(document.createTextNode(idField.getText()));
                film.appendChild(id);
                Element regizor = document.createElement("regizor");
                regizor.appendChild(document.createTextNode(directorField.getText()));
                film.appendChild(regizor);
                Element actori = document.createElement("actori");
                /** Actorii sunt separati prin ", " si au fiecare cate o eticheta <actor>, pentru a le putea atribui fiecaruia cate o pagina. */
                String[] actors = actorsField.getText().split(", ");
                for (String actor : actors) {
                    Element actorElement = document.createElement("actor");
                    actorElement.appendChild(document.createTextNode(actor));
                    actori.appendChild(actorElement);
                }
                film.appendChild(actori);
                Element descriere = document.createElement("descriere");
                descriere.appendChild(document.createTextNode(descriptionField.getText()));
                film.appendChild(descriere);
                Element file = document.createElement("file");
                descriere.appendChild(document.createTextNode(descriptionField.getText()));
                film.appendChild(file);
                document.getDocumentElement().appendChild(film);

                /** Salvam modificarile in fisiereul XML.*/
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = null;
                try {
                    transformer = transformerFactory.newTransformer();
                } catch (TransformerConfigurationException transformerConfigurationException) {
                    transformerConfigurationException.printStackTrace();
                }
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult("movies.xml");
                try {
                    transformer.transform(source, result);
                } catch (TransformerException transformerException) {
                    transformerException.printStackTrace();
                }

                /** Refresh the GUI*/
                refreshGUI();

                /** Inchidem fereastra pentru adaugarea filmului.*/
                addFrame.dispose();
            });
            addFrame.add(submitButton);

            addFrame.setVisible(true);
        });
        categoryPanel2.add(addButton);

        /** Adaugăm un buton pentru a vizualiza Raportul cu lista de filme din colectie.*/
        JButton viewButton = new JButton("Colectia de Filme");
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File inputFile = new File("movies.xml");
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(inputFile);
                    doc.getDocumentElement().normalize();

                    Map<String, List<String>> movieMap = new TreeMap<>();
                    NodeList nList = doc.getElementsByTagName("film");
                    for (int i = 0; i < nList.getLength(); i++) {
                        Node nNode = nList.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;
                            String category = eElement.getElementsByTagName("categorie").item(0).getTextContent();
                            String name = eElement.getElementsByTagName("nume").item(0).getTextContent();
                            movieMap.putIfAbsent(category, new ArrayList<>());
                            movieMap.get(category).add(name);
                        }
                    }

                    JTextArea textArea = new JTextArea();
                    textArea.setEditable(false);
                    for (Map.Entry<String, List<String>> entry : movieMap.entrySet()) {
                        textArea.append(entry.getKey() + ":\n");
                        Collections.sort(entry.getValue());
                        for (String movie : entry.getValue()) {
                            textArea.append("\t" + movie + "\n");
                        }
                    }

                    JFrame reportFrame = new JFrame("Colectia de Filme");
                    reportFrame.add(new JScrollPane(textArea));
                    reportFrame.setSize(800, 600);
                    reportFrame.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        categoryPanel2.add(viewButton);

        /** Adaugam un buton pentru a creea un fisier ce contine Raportul cu lista de filme din colectie.*/
        JButton saveButton = new JButton("Salvează Raportul");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File inputFile = new File("movies.xml");
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(inputFile);
                    doc.getDocumentElement().normalize();

                    Map<String, List<String>> movieMap = new TreeMap<>();
                    NodeList nList = doc.getElementsByTagName("film");
                    for (int i = 0; i < nList.getLength(); i++) {
                        Node nNode = nList.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;
                            String category = eElement.getElementsByTagName("categorie").item(0).getTextContent();
                            String name = eElement.getElementsByTagName("nume").item(0).getTextContent();
                            movieMap.putIfAbsent(category, new ArrayList<>());
                            movieMap.get(category).add(name);
                        }
                    }

                    PrintWriter writer = new PrintWriter("Raport.txt", "UTF-8");
                    for (Map.Entry<String, List<String>> entry : movieMap.entrySet()) {
                        writer.println(entry.getKey() + ":");
                        Collections.sort(entry.getValue());
                        for (String movie : entry.getValue()) {
                            writer.println("\t" + movie);
                        }
                    }
                    writer.close();

                    JOptionPane.showMessageDialog(frame, "Raportul a fost creat cu succes!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        categoryPanel2.add(saveButton);

        /** Cream o componenta de tip split.*/
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, categoryPanel1, categoryPanel2);
        splitPane.setResizeWeight(0.25);
        /** Adaugam componenta in Pagina Principala.*/
        homeCard.add(splitPane);
        contentPane.add(homeCard, "Home");
        cardLayout.show(contentPane, "Home");

        frame.pack();
        frame.setSize(1200, 700);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
