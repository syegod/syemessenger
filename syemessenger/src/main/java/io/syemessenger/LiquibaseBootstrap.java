package io.syemessenger;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import java.sql.Connection;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@Named
public class LiquibaseBootstrap {

  private final DataSource dataSource;

  public LiquibaseBootstrap(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @PostConstruct
  public void init() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      final var database =
          DatabaseFactory.getInstance()
              .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      final var liquibase =
          new Liquibase("dbchangelog/dbchangelog.xml", new ClassLoaderResourceAccessor(), database);

      liquibase.update();
    }
  }
}
