package com.dulcepack.backend.catalog.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_images")
public class ProductImageEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private ProductEntity product;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductImageEntity() {
    }

    public ProductImageEntity(UUID id, ProductEntity product, String fileName, String contentType,
            byte[] content, Instant now) {
        this.id = id;
        this.product = product;
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content.clone();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public byte[] getContent() { return content.clone(); }

    public void replace(String fileName, String contentType, byte[] content, Instant now) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content.clone();
        this.updatedAt = now;
    }
}
