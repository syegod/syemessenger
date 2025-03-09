package io.syemessenger;

import java.util.StringJoiner;

public class ServiceConfig {

  private int port = 8080;
  private String dbUrl;
  private String dbUser;
  private String dbPassword;
  private String kafkaBootstrapServers;
  private String kafkaConsumerGroup;
  private boolean shouldRunRoomOutboxProcessor;
  private int roomOutboxProcessorRunDelay = 300;

  public ServiceConfig() {}

  public static ServiceConfig fromSystemProperties() {
    final var port = getProperty("port");
    final var dbUrl = getProperty("dbUrl");
    final var dbUser = getProperty("dbUser");
    final var dbPassword = getProperty("dbPassword");
    final var kafkaBootstrapServers = getProperty("kafkaBootstrapServers");
    final var kafkaConsumerGroup = getProperty("kafkaConsumerGroup");
    final var shouldRunRoomOutboxProcessor = getOptionalProperty("shouldRunRoomOutboxProcessor");
    final var roomOutboxProcessorRunDelay = getOptionalProperty("roomOutboxProcessorRunDelay");

    return new ServiceConfig()
        .port(Integer.parseInt(port))
        .dbUrl(dbUrl)
        .dbUser(dbUser)
        .dbPassword(dbPassword)
        .kafkaBootstrapServers(kafkaBootstrapServers)
        .kafkaConsumerGroup(kafkaConsumerGroup)
        .shouldRunRoomOutboxProcessor(Boolean.parseBoolean(shouldRunRoomOutboxProcessor))
        .roomOutboxProcessorRunDelay(
            roomOutboxProcessorRunDelay != null
                ? Integer.parseInt(roomOutboxProcessorRunDelay)
                : 300);
  }

  public ServiceConfig port(int port) {
    this.port = port;
    return this;
  }

  public int port() {
    return port;
  }

  public String dbUrl() {
    return dbUrl;
  }

  public ServiceConfig dbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
    return this;
  }

  public String dbUser() {
    return dbUser;
  }

  public ServiceConfig dbUser(String dbUser) {
    this.dbUser = dbUser;
    return this;
  }

  public String dbPassword() {
    return dbPassword;
  }

  public ServiceConfig dbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
    return this;
  }

  public String kafkaBootstrapServers() {
    return kafkaBootstrapServers;
  }

  public ServiceConfig kafkaBootstrapServers(String kafkaBootstrapServers) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    return this;
  }

  public String kafkaConsumerGroup() {
    return kafkaConsumerGroup;
  }

  public ServiceConfig kafkaConsumerGroup(String kafkaConsumerGroup) {
    this.kafkaConsumerGroup = kafkaConsumerGroup;
    return this;
  }

  public boolean shouldRunRoomOutboxProcessor() {
    return shouldRunRoomOutboxProcessor;
  }

  public ServiceConfig shouldRunRoomOutboxProcessor(boolean shouldRunRoomOutboxProcessor) {
    this.shouldRunRoomOutboxProcessor = shouldRunRoomOutboxProcessor;
    return this;
  }

  public int roomOutboxProcessorRunDelay() {
    return roomOutboxProcessorRunDelay;
  }

  public ServiceConfig roomOutboxProcessorRunDelay(int roomOutboxProcessorRunDelay) {
    this.roomOutboxProcessorRunDelay = roomOutboxProcessorRunDelay;
    return this;
  }

  private static String getProperty(String property) {
    return getProperty(property, false);
  }

  private static String getOptionalProperty(String property) {
    return getProperty(property, true);
  }

  private static String getProperty(String property, boolean isOptional) {
    final var value = System.getProperty("syemessenger." + property);
    if (!isOptional && value == null) {
      throw new RuntimeException("Wrong config: missing " + property);
    }
    return value;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ServiceConfig.class.getSimpleName() + "[", "]")
        .add("port=" + port)
        .add("dbUrl='" + dbUrl + "'")
        .add("dbUser='" + dbUser + "'")
        .add("dbPassword='" + dbPassword + "'")
        .add("kafkaBootstrapServers='" + kafkaBootstrapServers + "'")
        .add("kafkaConsumerGroup='" + kafkaConsumerGroup + "'")
        .add("shouldRunRoomOutboxProcessor=" + shouldRunRoomOutboxProcessor)
        .add("roomOutboxProcessorRunDelay=" + roomOutboxProcessorRunDelay)
        .toString();
  }
}
