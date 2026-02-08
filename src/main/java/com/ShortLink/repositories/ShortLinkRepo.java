package com.ShortLink.repositories;


import com.ShortLink.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortLinkRepo extends JpaRepository<Link,Long> {
    Optional<Link> findLinkByOriginalUrl(String originalUrl);
    Optional<Link> findLinkByShortUrl(String shortUrl);
}
