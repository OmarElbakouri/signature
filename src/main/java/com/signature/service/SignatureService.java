package com.signature.service;

import com.signature.entity.Document;
import com.signature.entity.User;
import com.signature.repository.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
// Visual signature imports removed temporarily
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
// import org.apache.pdfbox.io.IOUtils; // Unused
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
// import org.bouncycastle.jce.provider.BouncyCastleProvider; // Unused
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class SignatureService {

    @Autowired
    private DocumentRepository documentRepository;

    @Value("${signature.storage.path}")
    private String storagePath;

    @Value("${signature.certificate.path}")
    private String certificatePath;

    @Value("${signature.certificate.private-key}")
    private String privateKeyFile;

    @Value("${signature.certificate.certificate}")
    private String certificateFile;

    private PrivateKey privateKey;
    private X509Certificate certificate;
    
    /**
     * Récupère l'utilisateur actuellement connecté
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    public Document uploadDocument(MultipartFile file) throws Exception {
        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = storageDir.resolve(fileName);

        // Save file
        Files.copy(file.getInputStream(), filePath);

        // Create document entity
        Document document = new Document(
            file.getOriginalFilename(),
            fileName,
            filePath.toString(),
            file.getSize(),
            file.getContentType()
        );
        
        // Associate document with current user
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            document.setUser(currentUser);
        }

        return documentRepository.save(document);
    }

    public Document signDocument(Long documentId, String signerName) throws Exception {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        if (document.getIsSigned()) {
            throw new RuntimeException("Document is already signed");
        }

        // Load certificate and private key
        loadCertificateAndKey();

        // Sign the document
        String signedFilePath = signPdfDocument(document.getFilePath(), signerName);

        // Update document entity
        document.setIsSigned(true);
        document.setSignedFilePath(signedFilePath);
        document.setSignedAt(LocalDateTime.now());
        document.setSignerName(signerName);
        document.setCertificateSubject(certificate.getSubjectDN().getName());
        document.setSignatureAlgorithm("SHA256withRSA");

        return documentRepository.save(document);
    }

    private String signPdfDocument(String inputPath, String signerName) throws Exception {
        String outputPath = inputPath.replace(".pdf", "_signed.pdf");
        System.out.println("Starting PDF signing process for: " + inputPath);
        System.out.println("Output path will be: " + outputPath);

        try (FileInputStream fis = new FileInputStream(inputPath);
             FileOutputStream fos = new FileOutputStream(outputPath)) {
             
            System.out.println("Loading PDF document...");
            PDDocument doc = Loader.loadPDF(new File(inputPath));
            System.out.println("PDF loaded successfully. Pages: " + doc.getNumberOfPages());

            System.out.println("Creating signature object...");
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(signerName);
            signature.setLocation("Electronic Signature Service");
            signature.setReason("Document electronically signed");
            signature.setSignDate(Calendar.getInstance());

            SignatureOptions signatureOptions = new SignatureOptions();
            signatureOptions.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
            
            // Add visual signature to the first page
            System.out.println("Adding visual signature to document...");
            addVisualSignature(doc, signerName);

            System.out.println("Adding digital signature to document...");
            doc.addSignature(signature, new SignatureInterface() {
                @Override
                public byte[] sign(InputStream content) throws IOException {
                    try {
                        System.out.println("Reading content bytes for signing...");
                        byte[] contentBytes = content.readAllBytes();
                        System.out.println("Creating PKCS7 signature...");
                        return createPKCS7Signature(contentBytes);
                    } catch (Exception e) {
                        System.err.println("Error in signature creation: " + e.getMessage());
                        e.printStackTrace();
                        throw new IOException("Error creating signature", e);
                    }
                }
            }, signatureOptions);

            System.out.println("Saving document with signature...");
            doc.saveIncremental(fos);
            System.out.println("Document saved successfully with signature.");
        } catch (Exception e) {
            System.err.println("Error in PDF signing process: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return outputPath;
    }

    private byte[] createPKCS7Signature(byte[] content) throws Exception {
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        generator.addSignerInfoGenerator(
            new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                .build(contentSigner, certificate)
        );
        generator.addCertificates(new JcaCertStore(Arrays.asList(certificate)));

        CMSProcessableByteArray processableContent = new CMSProcessableByteArray(content);
        CMSSignedData signedData = generator.generate(processableContent, false);

        return signedData.getEncoded();
    }

    private void loadCertificateAndKey() throws Exception {
        if (privateKey == null || certificate == null) {
            try {
                System.out.println("Loading certificate and private key...");
                System.out.println("Certificate path: " + certificatePath);
                System.out.println("Private key file: " + privateKeyFile);
                System.out.println("Certificate file: " + certificateFile);
                
                // Load private key
                ClassPathResource keyResource = new ClassPathResource(certificatePath + privateKeyFile);
                System.out.println("Private key resource exists: " + keyResource.exists());
                
                String keyContent = new String(keyResource.getInputStream().readAllBytes());
                System.out.println("Private key content length: " + keyContent.length());
                keyContent = keyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                                     .replace("-----END PRIVATE KEY-----", "")
                                     .replaceAll("\\s", "");
                
                byte[] keyBytes = Base64.getDecoder().decode(keyContent);
                System.out.println("Decoded key bytes length: " + keyBytes.length);
                
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privateKey = keyFactory.generatePrivate(keySpec);
                System.out.println("Private key loaded successfully: " + privateKey.getAlgorithm());

                // Load certificate
                ClassPathResource certResource = new ClassPathResource(certificatePath + certificateFile);
                System.out.println("Certificate resource exists: " + certResource.exists());
                
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                certificate = (X509Certificate) certFactory.generateCertificate(certResource.getInputStream());
                System.out.println("Certificate loaded successfully: " + certificate.getSubjectDN());
                System.out.println("Certificate valid from: " + certificate.getNotBefore() + " to " + certificate.getNotAfter());
            } catch (Exception e) {
                System.err.println("Error loading certificate or private key: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }

    public byte[] getDocumentContent(Long documentId, boolean signed) throws Exception {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        String filePath = signed ? document.getSignedFilePath() : document.getFilePath();
        if (filePath == null) {
            throw new RuntimeException("File not found");
        }

        return Files.readAllBytes(Paths.get(filePath));
    }
    
    public String testCertificateLoading() throws Exception {
        try {
            loadCertificateAndKey();
            return "Certificate and private key loaded successfully. Certificate subject: " + certificate.getSubjectDN();
        } catch (Exception e) {
            throw new Exception("Failed to load certificate or private key: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds a visual signature to the first page of the PDF document
     * Compatible with PDFBox 3.x API
     */
    private void addVisualSignature(PDDocument document, String signerName) throws Exception {
        try {
            // Get the first page
            PDPage firstPage = document.getPage(0);
            
            // Create a content stream for drawing
            PDPageContentStream contentStream = new PDPageContentStream(document, firstPage, 
                PDPageContentStream.AppendMode.APPEND, true, true);
            
            // Load a standard font (PDFBox 3.x way)
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            
            // Define signature box dimensions and position
            float boxWidth = 200;
            float boxHeight = 80;
            float boxX = 50; // Left margin
            float boxY = 50; // Bottom margin
            
            // Draw signature box border
            contentStream.setStrokingColor(Color.BLACK);
            contentStream.setLineWidth(1);
            contentStream.addRect(boxX, boxY, boxWidth, boxHeight);
            contentStream.stroke();
            
            // Fill background with light gray
            contentStream.setNonStrokingColor(new Color(245, 245, 245));
            contentStream.addRect(boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2);
            contentStream.fill();
            
            // Add signature text
            contentStream.setNonStrokingColor(Color.BLACK);
            
            // Title
            contentStream.beginText();
            contentStream.setFont(boldFont, 10);
            contentStream.newLineAtOffset(boxX + 10, boxY + boxHeight - 20);
            contentStream.showText("Document signé numériquement");
            contentStream.endText();
            
            // Signer name
            contentStream.beginText();
            contentStream.setFont(font, 9);
            contentStream.newLineAtOffset(boxX + 10, boxY + boxHeight - 35);
            contentStream.showText("Signataire: " + signerName);
            contentStream.endText();
            
            // Date and time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String dateTime = now.format(formatter);
            
            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.newLineAtOffset(boxX + 10, boxY + boxHeight - 50);
            contentStream.showText("Date: " + dateTime);
            contentStream.endText();
            
            // Location/reason
            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.newLineAtOffset(boxX + 10, boxY + boxHeight - 65);
            contentStream.showText("Signature électronique certifiée");
            contentStream.endText();
            
            // Close the content stream
            contentStream.close();
            
            System.out.println("Visual signature added successfully for: " + signerName);
            
        } catch (Exception e) {
            System.err.println("Error adding visual signature: " + e.getMessage());
            throw new Exception("Failed to add visual signature: " + e.getMessage(), e);
        }
    }
}
