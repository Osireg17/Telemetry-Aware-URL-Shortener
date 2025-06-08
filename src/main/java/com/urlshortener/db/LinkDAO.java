package com.urlshortener.db;

import com.urlshortener.core.Link;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

@RegisterBeanMapper(Link.class)
public interface LinkDAO {

    @SqlUpdate("INSERT INTO links (long_url, short_code, created_at) VALUES (:longUrl, :shortCode, :createdAt)")
    @GetGeneratedKeys
    long save(@BindBean Link link);

    @SqlQuery("SELECT * FROM links WHERE short_code = :shortCode")
    Optional<Link> findByShortCode(@Bind("shortCode") String shortCode);
}