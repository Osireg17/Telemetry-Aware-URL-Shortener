package com.urlshortener.db;

import com.urlshortener.core.Click;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterBeanMapper(Click.class)
public interface ClickDAO {

    @SqlUpdate("INSERT INTO clicks (link_id, user_agent, ip_address, referer) VALUES (:linkId, :userAgent, :ipAddress, :referer)")
    @GetGeneratedKeys
    void save(@BindBean Click click);
}