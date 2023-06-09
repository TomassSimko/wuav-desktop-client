package com.wuav.client.gui.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.wuav.client.be.Project;
import com.wuav.client.be.user.AppUser;
import com.wuav.client.bll.helpers.EventType;
import com.wuav.client.bll.utilities.pdf.DefaultPdfGenerator;
import com.wuav.client.gui.controllers.abstractController.RootController;
import com.wuav.client.gui.controllers.event.RefreshEvent;
import com.wuav.client.gui.models.user.CurrentUser;
import com.wuav.client.gui.models.user.IUserModel;
import com.wuav.client.gui.utils.FileChooserUtil;
import com.wuav.client.gui.utils.enums.CustomColor;
import com.wuav.client.gui.utils.event.CustomEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.utils.SwingFXUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The class PDFBuilderController.
 */
public class PDFBuilderController extends RootController implements Initializable {
    @FXML
    private MFXCheckbox photosCheck, technicianCheck, descriptionCheck;
    @FXML
    private MFXButton preview, export;
    @FXML
    private MFXTextField fileName;
    @FXML
    private HBox loadingBox;
    @FXML
    private Pane loadingPane;
    @FXML
    private MFXProgressSpinner progressLoader;
    @FXML
    private Pagination pagination;
    @FXML
    private Label projectName;
    @FXML
    private AnchorPane builderAnchorPane;
    private Project project;
    private final EventBus eventBus;

    private final IUserModel userModel;

    private ObjectProperty<ByteArrayOutputStream> finalPDFBytesProperty = new SimpleObjectProperty<>();

    private BooleanProperty isExport = new SimpleBooleanProperty(true);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Instantiates a new PDF builder controller.
     *
     * @param eventBus  the event bus
     * @param userModel the user model
     */
    @Inject
    public PDFBuilderController(EventBus eventBus, IUserModel userModel) {
        this.eventBus = eventBus;
        this.userModel = userModel;
    }

    /**
     * Initialize.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eventBus.register(this);
        setupExportButtonGroup();
        pagination.setPageCount(1); // set default page count to be 1

        Platform.runLater(() -> {
            handelExportThread();
            handelPreviewThread();
        });
    }

    private void handelExportThread() {
        Stage stage = (Stage) builderAnchorPane.getScene().getWindow();
        var project = (Project) stage.getProperties().get("projectToExport");
        if (project != null) {
            projectName.setText(project.getName());
            this.project = project;

            // Generate the default PDF in a background task
            Task<Image> generateDefaultPdfTask = new Task<>() {
                @Override
                protected Image call() throws Exception {
                    loadingBox.setVisible(true);
                    AppUser appUser = CurrentUser.getInstance().getLoggedUser();

                    DefaultPdfGenerator pdfGenerator = new DefaultPdfGenerator.Builder(appUser, project, "default")
                            .includeDescription(false)
                            .includeTechnicians(false)
                            .includeImages(false)
                            .build();

                    ByteArrayOutputStream defaultPdf = pdfGenerator.generatePdf();
                    return convertPdfToImage(defaultPdf);
                }
            };

            // When the task is done, update the ImageView with the default PDF
            generateDefaultPdfTask.setOnSucceeded(event -> {
                loadingBox.setVisible(false);
                System.out.println("Task succeeded");
                Image defaultPdfImage = generateDefaultPdfTask.getValue();
                pagination.setPageFactory((pageIndex) -> {
                    ImageView imageView = new ImageView(defaultPdfImage);
                    imageView.setFitWidth(300);
                    imageView.setFitHeight(300); // Set the desired height
                    imageView.setPreserveRatio(true); // This will maintain the image's aspect ratio
                    return imageView;
                });
            });

            // Start the task
            new Thread(generateDefaultPdfTask).start();
        }
    }

    private void handelPreviewThread() {
        preview.setOnAction(event -> {
            boolean includeDescription = descriptionCheck.isSelected();
            boolean includeTechnicians = technicianCheck.isSelected();
            boolean includeImages = photosCheck.isSelected();
            AppUser appUser = CurrentUser.getInstance().getLoggedUser();
            loadingBox.setVisible(true);

            DefaultPdfGenerator pdfGenerator = new DefaultPdfGenerator.Builder(appUser, project, "preview")
                    .includeDescription(includeDescription)
                    .includeTechnicians(includeTechnicians)
                    .includeImages(includeImages)
                    .build();

            Task<ByteArrayOutputStream> generatePdfTask = new Task<>() {
                @Override
                protected ByteArrayOutputStream call() throws Exception {
                    return pdfGenerator.generatePdf();
                }
            };

            generatePdfTask.setOnSucceeded(pdfEvent -> {
                loadingBox.setVisible(false);
                ByteArrayOutputStream pdfStream = generatePdfTask.getValue();
                // set it to the instance variable
                finalPDFBytesProperty.set(pdfStream);
                List<Image> pdfImages = null;
                try {
                    pdfImages = convertPdfToImages(pdfStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                List<Image> finalPdfImages = pdfImages;
                pagination.setPageFactory((pageIndex) -> {
                    ImageView imageView = new ImageView(finalPdfImages.get(pageIndex));
                    imageView.setFitWidth(300);  // Set the desired width
                    imageView.setFitHeight(300); // Set the desired height
                    imageView.setPreserveRatio(true); // This will maintain the image's aspect ratio
                    return imageView;
                });
                pagination.setPageCount(pdfImages.size());
            });

            new Thread(generatePdfTask).start();
        });
    }

    private void setupExportButtonGroup() {
        export.textProperty().bind(Bindings.when(isExport)
                .then("Export")
                .otherwise("Send Email"));

        // Disable the actionButton if we're in export mode and either the final PDF bytes are null or the filename is empty
        export.disableProperty().bind(
                isExport.and(
                        finalPDFBytesProperty.isNull()
                                .or(fileName.textProperty().isEmpty())
                )
        );
        // bind sending email if it is not export and finalPDFBytes is  null to be disabled
        export.disableProperty().bind(
                isExport.not().and(
                        finalPDFBytesProperty.isNull()
                                .or(fileName.textProperty().isEmpty())
                )
        );


        // Set the onAction of the actionButton to call either the export or email method, depending on the isExport property
        export.setOnAction(event -> {
            if (isExport.get()) {
                exportPDF();
            } else {
                sendEmail();
            }
        });

    }


    private Image convertPdfToImage(ByteArrayOutputStream pdfStream) {
        try (InputStream in = new ByteArrayInputStream(pdfStream.toByteArray())) {
            PDDocument document = PDDocument.load(in);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bImage = pdfRenderer.renderImageWithDPI(0, 300); // 0 = first page
            document.close();
            return SwingFXUtils.toFXImage(bImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Image> convertPdfToImages(ByteArrayOutputStream pdfStream) throws IOException {
        List<Image> images = new ArrayList<>();
        PDDocument document = PDDocument.load(pdfStream.toByteArray());
        PDFRenderer renderer = new PDFRenderer(document);

        for (int i = 0; i < document.getNumberOfPages(); ++i) {
            BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 300);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            images.add(fxImage);
        }

        document.close();
        return images;
    }

    /**
     * Subscribe to the RefreshEvent to handle the isExport property
     */
    @Subscribe
    public void handleIsEmail(RefreshEvent event) {
        if (event.eventType() == EventType.EXPORT_EMAIL) {
            isExport.set(false);
            System.out.println(isExport.get());
        }
    }

    private void exportPDF() {
        FileChooserUtil fileChooserUtil = new FileChooserUtil(
                "Save PDF", "*.pdf",
                fileName.getText() + ".pdf"
        );
        File file = fileChooserUtil.showDialog(builderAnchorPane.getScene().getWindow());

        // If the user has selected a file
        if (file != null) {
            try {
                // Write the ByteArrayOutputStream to the selected file
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    finalPDFBytesProperty.getValue().writeTo(fileOutputStream);
                    handleEvent(true, "PDF exported successfully");
                    getStage().close();
                }
            } catch (IOException e) {
                handleEvent(false, e.getMessage());
            }
        }
    }


    private void sendEmail() {
        AppUser appUser = CurrentUser.getInstance().getLoggedUser();
        progressLoader.setVisible(true);
        loadingPane.setVisible(true);
        loadingPane.setStyle(CustomColor.DIMMED.getStyle());

        executorService.submit(() -> {
            try {
                boolean result = userModel.sendEmailWithAttachement(appUser, project, finalPDFBytesProperty.getValue(), fileName.getText());

                Platform.runLater(() -> {
                    try {
                        if (!result) {
                            handleEvent(false, "Error sending email");
                            hideLoadingPane();
                        } else {
                            getStage().close(); // Close the login stage
                            handleEvent(true, "Email sent successfully");
                        }
                        executorService.shutdown(); // Shutdown the executor service
                    } catch (Exception e) {
                        handleEvent(false, e.getMessage());
                    }
                });
            } catch (GeneralSecurityException | IOException e) {
                // Update the UI on the JavaFX application thread
                Platform.runLater(() -> {
                    handleEvent(false, e.getMessage());
                    hideLoadingPane();
                });
            }
        });
    }

    private void hideLoadingPane() {
        progressLoader.setVisible(false);
        loadingPane.setVisible(false);
        loadingPane.setStyle(CustomColor.DIMMED.getStyle());
    }

    private void handleEvent(boolean isSuccess, String message) {
        EventType eventType = EventType.SHOW_NOTIFICATION;
        CustomEvent event = new CustomEvent(eventType, isSuccess, message);
        eventBus.post(event);
    }
}
