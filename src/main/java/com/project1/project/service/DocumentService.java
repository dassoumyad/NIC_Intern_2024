package com.project1.project.service;

import com.project1.project.model.ArchiveDocument;
import com.project1.project.model.ClientDocument;

import com.project1.project.model.PdfPasswordRequest;
import com.project1.project.model.Review;
import com.project1.project.repository.ArchiveRepository;
import com.project1.project.repository.DocumentRepository;
import com.project1.project.repository.ReviewRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Service
public class DocumentService {

    @Autowired
    private final DocumentRepository documentRepository;
    private final MongoTemplate mongoTemplate;
    @Autowired
    private ReviewRepository reviewRepository;
    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());
    @Autowired
    public DocumentService(DocumentRepository documentRepository, MongoTemplate mongoTemplate) {
        this.documentRepository = documentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    public ArchiveRepository archiveRepository;
    //saving the document
    public UUID saveDocument(ClientDocument document) {
        document.setDocument_id(UUID.randomUUID());

        Date date = new Date();
        document.setCreated_on(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 1);
        Date expiryDate = calendar.getTime();
        document.setExpiry_on(expiryDate);

        documentRepository.save(document);
        return document.getDocument_id();
    }
    // getting a document using documentID
    public ResponseEntity<ClientDocument> getDocumentById(UUID documentId) {
        System.out.println("searching for document id: " + documentId);
        ClientDocument document = documentRepository.findById(documentId).orElse(null);

        System.out.println("Document found : " + document);
        return ResponseEntity.ok(document);

    }

    //getting a docuemnt using personID
    public List<ClientDocument> getDocumentsByPersonId(int personId) {
        System.out.println("searching for document with personId: " + personId);
        return documentRepository.findByPersonId(personId);

    }

    //save review of a document
    public Review saveOrUpdateReview(Review review) {
        Optional<Review> existingReview = reviewRepository.findByApplicationTransactionId(review.getApplication_transaction_id());

        if (existingReview.isPresent()) {
            Review existing = existingReview.get();
            existing.setReview(review.getReview());

            return reviewRepository.save(existing);
        }

        return reviewRepository.save(review);
    }


    // archive/delete a document
    public ArchiveDocument archiveDocument(ArchiveDocument archiveDocument) {

        Optional<ArchiveDocument> existingArchive = archiveRepository.findByApplicationTransactionId(archiveDocument.getApplication_transaction_id());
        Optional<ClientDocument> archivedDocument = documentRepository.findByApplicationTransactionId(archiveDocument.getApplication_transaction_id());

        if (existingArchive.isPresent()) {
            ArchiveDocument archivedoc = existingArchive.get();
            archivedoc.setArchival_comments(archiveDocument.getArchival_comments());
            return archiveRepository.save(archivedoc);
        }

        archivedDocument.ifPresent(document -> documentRepository.deleteById(document.getDocument_id()));

        return archiveRepository.save(archiveDocument);
    }




    public ClientDocument addWatermarkToDocument(long applicationTransactionId, String watermark) throws IOException {
        Optional<ClientDocument> existingDocument = documentRepository.findByApplicationTransactionId(applicationTransactionId);

        if (!existingDocument.isPresent()) {
            throw new IOException("Document not found");
        }

        ClientDocument clientDocument = existingDocument.get();

        byte[] pdfBytes = Base64.getDecoder().decode(clientDocument.getDocument().getActual_document_base_64());

        PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));

        //loop to add watermark to each page
        for(PDPage page : document.getPages()){
            PDRectangle pageSize = page.getMediaBox();
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 70);
            contentStream.setNonStrokingColor(200, 200, 200);    //Light Grey colour


            float stringWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(watermark) / 1000 * 50;
            float stringHeight = PDType1Font.HELVETICA_BOLD.getFontDescriptor().getCapHeight() / 1000 * 50;

            //calculating middle coordinates
            float centerX = (pageWidth - stringWidth) / 2;
            float centerY = (pageHeight - stringHeight) / 2;

            contentStream.beginText();
            contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45), centerX, centerY));  // adjust the position and angle as required
            contentStream.showText(watermark);
            contentStream.endText();
            contentStream.close();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        String base64WatermarkedPdf = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        clientDocument.getDocument().setActual_document_base_64(base64WatermarkedPdf);

        documentRepository.deleteById(existingDocument.get().getDocument_id());
        documentRepository.save(clientDocument);

        return clientDocument;

    }



    public Optional<Review> viewReviewLog(long applicationTransactionId) {

        return reviewRepository.findByApplicationTransactionId(applicationTransactionId);
    }


   //Viewing the edit logs
    public Optional<ArchiveDocument> viewEditLog(long applicationTransactionId) {
        return archiveRepository.findByApplicationTransactionId(applicationTransactionId);
    }


    //Update a Document
    public void deleteDocumentById(UUID documentId) {
        documentRepository.deleteById(documentId);
    }

    public ClientDocument updateDocument(ClientDocument document) {
        Date date = new Date();
        document.setCreated_on(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 1);
        Date expiryDate = calendar.getTime();
        document.setExpiry_on(expiryDate);

        return documentRepository.save(document);
    }


    public String addPasswordToPdf(PdfPasswordRequest request) throws IOException {
        Optional<ClientDocument> existingDocument = documentRepository.findByApplicationTransactionId(request.getApplication_transaction_id());

        if (!existingDocument.isPresent()) {
            throw new IOException("Document not found");
        }

        ClientDocument clientDocument = existingDocument.get();

        byte[] pdfBytes = Base64.getDecoder().decode(clientDocument.getDocument().getActual_document_base_64());

        PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));

        // Set the password protection
        AccessPermission accessPermission = new AccessPermission();
        StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(
                request.getPassword(), request.getPassword(), accessPermission);

        // Customize the protection policy if necessary
        protectionPolicy.setEncryptionKeyLength(128);  // 128-bit key length
        protectionPolicy.setPermissions(accessPermission);
        document.protect(protectionPolicy);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        String base64PdfWithPassword = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        // Update the ClientDocument with the new Base64 content
        clientDocument.getDocument().setActual_document_base_64(base64PdfWithPassword);

        // Save the updated ClientDocument
        documentRepository.save(clientDocument);

        return base64PdfWithPassword;
    }



}
