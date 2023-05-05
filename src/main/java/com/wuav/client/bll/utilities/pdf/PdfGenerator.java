package com.wuav.client.bll.utilities.pdf;


import com.google.inject.Inject;
import com.wuav.client.be.Customer;
import com.wuav.client.be.Project;
import com.wuav.client.be.user.AppUser;
import javafx.scene.paint.Color;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class PdfGenerator implements IPdfGenerator {

    private static final String RESOURCE_FOLDER = "src/main/resources/com/wuav/client/images/wuav-logo.png";

    private static final String TEST_PLAN = "src/main/resources/com/wuav/client/images/drawing.png";

    private static final String OUTPUT_FOLDER = "src/main/resources";

    private static final String FILE_EXTENSION = ".pdf";

    private static final String DESCR_TEST = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Molestie at elementum eu facilisis sed odio. Et malesuada fames ac turpis egestas sed tempus. A cras semper auctor neque vitae tempus quam. Eu consequat ac felis donec et odio pellentesque diam volutpat. Adipiscing vitae proin sagittis nisl rhoncus mattis. Condimentum mattis pellentesque id nibh. Ultrices eros in cursus turpis. Egestas tellus rutrum tellus pellentesque eu tincidunt tortor. Enim nulla aliquet porttitor lacus luctus accumsan. Sed vulputate mi sit amet mauris. Molestie ac feugiat sed lectus vestibulum mattis.";


    private static void writeText(PDPageContentStream contentStream, String text, PDFont font, float leading,
                                  int size, float xPos, float yPos, RenderingMode renderModeL) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, size);
        contentStream.newLineAtOffset(xPos, yPos);
        contentStream.setRenderingMode(renderModeL);
        contentStream.setLeading(leading);
        contentStream.showText(text);
        contentStream.endText();

    }

//    public static void main(String[] args) {
//        PdfGenerator pdfGenerator = new PdfGenerator();
//
//        Customer customer = new Customer(1, "Tomasko", "eail@yahoo.com", "dsafsdfsdf", "Private");
//
//
//        Project project = new Project();
//        project.setName("tomas");
//        project.setDescription(DESCR_TEST);
//        project.setCustomer(customer);
//
//
//        pdfGenerator.generatePdf(null, project, "test");
//    }


    @Override
    public ByteArrayOutputStream generatePdf(AppUser appUser, Project project, String fileName) {
        var outputStream = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);


            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set font
            PDType1Font font = PDType1Font.HELVETICA;
            contentStream.setFont(font, 12);

            // Set colors
            PDColor blueColor = new PDColor(new float[]{0.25f, 0.55f, 0.79f}, PDDeviceRGB.INSTANCE);
            PDColor grayColor = new PDColor(new float[]{0.62f, 0.62f, 0.62f}, PDDeviceRGB.INSTANCE);
            PDColor blackColor = new PDColor(new float[]{0f, 0f, 0f}, PDDeviceRGB.INSTANCE);
            PDColor whiteColor = new PDColor(new float[]{1f, 1f, 1f}, PDDeviceRGB.INSTANCE);

            // Logo Image area
            PDRectangle logoRect = new PDRectangle(50, PDRectangle.A4.getHeight() - 150, PDRectangle.A4.getWidth() / 4, 75);

            // Full Width Image area (with padding around it)
            PDRectangle imageRect = new PDRectangle(50, PDRectangle.A4.getHeight() - 350, PDRectangle.A4.getWidth() - 100, 160);

            // Customer Info List area (right under the full-width image)
            PDRectangle infoRect = new PDRectangle(50, PDRectangle.A4.getHeight() - 800, PDRectangle.A4.getWidth() - 100, 400);

            // Colored Link Text area
            PDRectangle linkRect = new PDRectangle(50, PDRectangle.A4.getHeight() - 800, PDRectangle.A4.getWidth() - 100, 7);

            // Description Text area
            PDRectangle descriptionRect = new PDRectangle(50, PDRectangle.A4.getHeight() - 800, PDRectangle.A4.getWidth(), 200);
            // Logo Image
            PDImageXObject logoImage = PDImageXObject.createFromFile(RESOURCE_FOLDER, document);
            contentStream.drawImage(logoImage, logoRect.getLowerLeftX(), logoRect.getLowerLeftY(), logoRect.getWidth(), logoRect.getHeight());

            // Full Width Image
            PDImageXObject image = PDImageXObject.createFromFile(TEST_PLAN, document);

            // Calculate the scaling factor based on the original image size and the desired display size
            float originalWidth = image.getWidth();
            float originalHeight = image.getHeight();
            float displayWidth = imageRect.getWidth() - 40;
            float displayHeight = imageRect.getHeight();

            float scaleX = displayWidth / originalWidth;
            float scaleY = displayHeight / originalHeight;
            float scale = Math.min(scaleX, scaleY);

            // Draw the resized image within the desired area
            float imageWidth = originalWidth * scale;
            float imageHeight = originalHeight * scale;
            float imageX = imageRect.getLowerLeftX() + (displayWidth - imageWidth) / 2;
            float imageY = imageRect.getLowerLeftY() + (displayHeight - imageHeight) / 2;

            contentStream.drawImage(image, imageX, imageY, imageWidth, imageHeight);

            // Draw a border around the full-width image area
            contentStream.addRect(imageRect.getLowerLeftX(), imageRect.getLowerLeftY(), imageRect.getWidth(), imageRect.getHeight());
            contentStream.setLineWidth(1);
            contentStream.setStrokingColor(java.awt.Color.BLACK);
            contentStream.stroke();

            // Customer Info List
            String customerInfo = "Customer Info";
            contentStream.beginText();
            // make the font bold
            PDType1Font fontHeader = PDType1Font.HELVETICA_BOLD;
            contentStream.setFont(fontHeader, 18);
            contentStream.newLineAtOffset(infoRect.getLowerLeftX(), infoRect.getUpperRightY() + 20);
            contentStream.showText(customerInfo);
            // add margin on the bottom
            contentStream.newLineAtOffset(0, -20);
            contentStream.endText();

            List<String> customerInfoList = List.of("Customer Name: " + project.getCustomer().getName(),
                    "Customer Email: " + project.getCustomer().getEmail(),
                    "Customer Phone: " + project.getCustomer().getPhoneNumber());

            contentStream.beginText();
            // set padding to the top
            contentStream.newLineAtOffset(0, -5);
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(infoRect.getLowerLeftX(), infoRect.getUpperRightY());

            for (String line : customerInfoList) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -20);
            }
            contentStream.endText();

            // Colored Link Text
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.setNonStrokingColor(blueColor); // Use the blue color defined earlier
            contentStream.newLineAtOffset(linkRect.getLowerLeftX(), linkRect.getLowerLeftY());
            contentStream.showText("https://wuav.dk/installation/image/3");
            contentStream.endText();


            // descirption

            // Description Text
            contentStream.beginText();
            contentStream.setFont(fontHeader, 18);
            contentStream.setNonStrokingColor(blackColor);
            contentStream.newLineAtOffset(descriptionRect.getLowerLeftX(), descriptionRect.getLowerLeftY() + 300);
            contentStream.showText("Description");
            contentStream.endText();


            // Lorem Ipsum Text
            String loremIpsum = DESCR_TEST;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.setNonStrokingColor(blackColor);
            contentStream.newLineAtOffset(descriptionRect.getLowerLeftX(), descriptionRect.getLowerLeftY() + 250);

            // Break lorem ipsum text into lines and show them
            List<String> loremLines = breakTextIntoLines(loremIpsum, 500, font, 12);
            for (String line : loremLines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -20);
            }
            contentStream.endText();

            // close the stream
            contentStream.close();


            Path resourceFolder = Paths.get(OUTPUT_FOLDER);
            Path filePath = resourceFolder.resolve(fileName + FILE_EXTENSION);
            System.out.println("Saving file");



            try {
                document.save(outputStream);
                contentStream.close();
                document.close();

                return outputStream; // Use the return keyword to return the outputStream
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return outputStream;
    }

    public static List<String> breakTextIntoLines(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        int lastSpace = -1;

        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0) {
                spaceIndex = text.length();
            }
            String subString = text.substring(0, spaceIndex);
            float size = fontSize * font.getStringWidth(subString) / 1000;

            if (size > maxWidth) {
                if (lastSpace < 0) {
                    lastSpace = spaceIndex;
                }
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }


}




