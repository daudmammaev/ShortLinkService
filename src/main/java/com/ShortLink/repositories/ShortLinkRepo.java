package com.ShortLink.repositories;


import com.ShortLink.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortLinkRepo extends JpaRepository<Link,Long> {
    Link findLinkByOriginalUrl(String originalUrl);
    Link findLinkByShortUrl(String shortUrl);
}
