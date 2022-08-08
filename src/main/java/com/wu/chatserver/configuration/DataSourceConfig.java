package com.wu.chatserver.configuration;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.tool.schema.Action;
import org.jboss.weld.util.collections.ImmutableMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hibernate.cfg.AvailableSettings.*;

@Slf4j
@ApplicationScoped
public class DataSourceConfig {

    @Dependent
    @Produces
    private PersistenceUnitInfo archiverPersistenceUnitInfo() {
        return new PersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return "chat_persistence_unit";
            }

            @Override
            public String getPersistenceProviderClassName() {
                return "org.hibernate.jpa.HibernatePersistenceProvider";
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                String dbUrl = System.getenv("JDBC_DATABASE_URL");
                String dbUser = "chatdb";
                String dbPassword = "chatdb";
                String driverName = "org.postgresql.Driver";
                if (dbUrl == null || dbUrl.isEmpty()) {
                    throw new IllegalStateException("Database URL is required");
                } else {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(dbUrl);
                    config.setDriverClassName(driverName);
                    config.setUsername(dbUser);
                    config.setPassword(dbPassword);
                    config.setMaximumPoolSize(20);
                    return new HikariDataSource(config);
                }
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return null;
            }

            @Override
            public List<String> getMappingFileNames() {
                return Collections.emptyList();
            }

            @Override
            public List<java.net.URL> getJarFileUrls() {
                try {
                    return Collections.list(this.getClass()
                            .getClassLoader()
                            .getResources(""));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public List<String> getManagedClassNames() {
                return Collections.emptyList();
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return false;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return null;
            }

            @Override
            public ValidationMode getValidationMode() {
                return null;
            }

            @Override
            public Properties getProperties() {
                return new Properties();
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public void addTransformer(ClassTransformer transformer) {

            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }
        };
    }

    @Produces
    @Default
    @ApplicationScoped
    public EntityManagerFactory entityManagerFactory() {
        String jdbc_database_url = System.getenv("JDBC_DATABASE_URL");
        log.info("EntityManagerFactory bean creation with params: " + jdbc_database_url);
        return new HibernatePersistenceProvider().createContainerEntityManagerFactory(
                archiverPersistenceUnitInfo(),
                ImmutableMap.<String, Object>builder()
                        .put(DIALECT, PostgreSQL82Dialect.class)
                        .put(HBM2DDL_AUTO, Action.CREATE_DROP)
                        .put(SHOW_SQL, false)
                        .build());
        //return Persistence.createEntityManagerFactory("chat_persistence_unit");
    }

    public void closeEntityManagerFactory(@Disposes EntityManagerFactory entityManagerFactory) {
        entityManagerFactory.close();
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager(EntityManagerFactory entityManagerFactory) {
        log.debug("EntityManager bean creation");
        return entityManagerFactory.createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager em) {
        log.debug("EntityManager closing");
        em.close();
    }
}
