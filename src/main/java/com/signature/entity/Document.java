package com.signature.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String originalName;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Boolean isSigned = false;
    
    @Column
    private String signedFilePath;
    
    @Column
    private LocalDateTime signedAt;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    @Column
    private String signerName;
    
    @Column
    private String certificateSubject;
    
    @Column
    private String signatureAlgorithm;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // Pour éviter les cycles de sérialisation JSON
    private User user;

    // Constructors
    public Document() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Document(String originalName, String fileName, String filePath, Long fileSize, String contentType) {
        this();
        this.originalName = originalName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Boolean getIsSigned() { return isSigned; }
    public void setIsSigned(Boolean isSigned) { this.isSigned = isSigned; }

    public String getSignedFilePath() { return signedFilePath; }
    public void setSignedFilePath(String signedFilePath) { this.signedFilePath = signedFilePath; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }

    public String getCertificateSubject() { return certificateSubject; }
    public void setCertificateSubject(String certificateSubject) { this.certificateSubject = certificateSubject; }
    
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
