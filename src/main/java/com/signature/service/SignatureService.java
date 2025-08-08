package com.signature.service;

import com.signature.entity.Document;
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

// import java.awt.Color; // Unused
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
// import java.security.Security; // Unused
// import java.security.cert.Certificate; // Unused
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
// import java.text.SimpleDateFormat; // Unused
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
// import java.util.Date; // Unused
import java.util.UUID;
import org.apache.pdfbox.Loader;

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
            
            // Add visual signature to the first page (temporarily disabled for compilation)
            System.out.println("Visual signature temporarily disabled - focusing on digital signature");
            // addVisualSignature(doc, signerName);

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
    
    // Visual signature method temporarily removed due to PDFBox 3.x font API compatibility issues
    // TODO: Re-implement visual signature with correct PDFBox 3.x font API
    /*
    private void addVisualSignature(PDDocument document, String signerName) throws Exception {
        // Implementation will be added once PDFBox font API issues are resolved
        System.out.println("Visual signature feature temporarily disabled");
    }
    */
}
