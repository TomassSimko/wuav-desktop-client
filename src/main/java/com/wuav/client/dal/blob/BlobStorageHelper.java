package com.wuav.client.dal.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.wuav.client.be.CustomImage;
import com.wuav.client.bll.utilities.UniqueIdGenerator;
import javafx.scene.image.Image;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class BlobStorageHelper {
    private final BlobContainerClient containerClient;

    public BlobStorageHelper(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    public CustomImage uploadImageToBlobStorage(File imageFile) {
        String extension = getFileExtension(imageFile.getName());
        String uniqueName = UUID.randomUUID().toString() + "." + extension;
        BlobClient blobClient = containerClient.getBlobClient(uniqueName);

        try (InputStream inputStream = new FileInputStream(imageFile)) {
            blobClient.upload(inputStream, imageFile.length(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var imageId = UniqueIdGenerator.generateUniqueId();

        return new CustomImage(imageId,extension,blobClient.getBlobUrl());

    }

    public Image downloadImageFromBlobStorage(BlobContainerClient containerClient, String blobUrl) {
        URL url;
        try {
            url = new URL(blobUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        String blobName = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.download(outputStream);

        byte[] imageBytes = outputStream.toByteArray();
        return new Image(new ByteArrayInputStream(imageBytes));
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }
}
