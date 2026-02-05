package com.ShortLink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Original URL cannot be blank")
    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @NotBlank(message = "Short URL cannot be blank")
    @Size(max = 10, message = "Short URL must be at most 10 characters")
    @Column(name = "short_url", nullable = false, unique = true, length = 8)
    private String shortUrl;

}
