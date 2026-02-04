package com.shortlink.repositories;



import com.shortlink.models.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortLinkRepo extends JpaRepository<Link,Long> {
    Link findLinkByOriginalUrl(String originalUrl);
    Link findLinkByShortUrl(String shortUrl);
}
