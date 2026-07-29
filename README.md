# lisovskyi-jpa-starter

![Java](https://img.shields.io/badge/Java-21%2B-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=springboot)
![Version](https://img.shields.io/badge/version-0.1.3-blue)
![License](https://img.shields.io/badge/license-Apache%202.0-green)

A Spring Boot auto-configuration library that standardises JPA persistence across microservices. It provides a UUID v7-based identity strategy, a hierarchy of auditable base entities, and Spring Data JPA auditing wired automatically to the Spring Security context — all with zero boilerplate in consumer services.

---

## Project Overview

Every JPA-based microservice needs the same scaffolding: a consistent ID generation strategy, `createdAt`/`updatedAt` timestamps, and `createdBy`/`updatedBy` audit trails. Without a shared library this logic is either duplicated across services or implemented inconsistently.

`lisovskyi-jpa-starter` solves this by shipping:

- A custom **UUID v7 generator** (time-ordered, k-sortable) that plugs directly into Hibernate.
- A hierarchy of **`@MappedSuperclass` base entities** covering every combination of identity, timestamps, and audit metadata.
- A **`SecurityAuditorAware`** implementation that reads the current user from the Spring Security context (falls back to `"SYSTEM"` when no authentication is present).
- A **`JpaAutoConfiguration`** that enables `@EnableJpaAuditing` safely — guarded by `@ConditionalOnMissingBean(name = "jpaAuditingHandler")` to prevent double-registration conflicts.

---

## Why UUID v7?

UUID v4 is randomly generated, which means every new row gets a random position in the B-tree index. Under high insert volume this causes **index fragmentation** — the database must constantly split and rebalance index pages, degrading write performance.

**UUID v7** is time-ordered (k-sortable): each new UUID is lexicographically greater than the previous one. New rows are always appended at the tail of the index, just like an auto-increment integer — with none of the coordination overhead of sequences in distributed systems.

| Property | UUID v4 | UUID v7 |
|---|---|---|
| Globally unique | ✅ | ✅ |
| DB-independent generation | ✅ | ✅ |
| Index-friendly (k-sortable) | ❌ | ✅ |
| Embeds creation time | ❌ | ✅ |
| Sequential on the same node | ❌ | ✅ |

---

## Features

- ✅ **UUID v7 ID generation** — time-ordered UUIDs via `com.github.f4b6a3:uuid-creator`. Existing IDs are preserved on merge/programmatic insert.
- ✅ **`@UuidV7` annotation** — a simple custom annotation applied on `BaseEntity.id` to wire the generator.
- ✅ **Entity hierarchy**:
  - `BaseEntity` — UUID v7 `id`, proper `equals`/`hashCode` safe with Hibernate proxies.
  - `TimestampedEntity` — adds Hibernate `@CreationTimestamp`/`@UpdateTimestamp` (`createdAt`, `updatedAt`).
  - `CreationTimestampedEntity` — `createdAt` only.
  - `UpdateTimestampedEntity` — `updatedAt` only.
  - `AuditableEntity` — Spring Data JPA auditing columns (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`).
- ✅ **Security-aware auditing** — `SecurityAuditorAware` resolves the username from `SecurityContextHolder`. Works with both `UserDetails` principals and raw `String` principals.
- ✅ **Conditional auto-configuration** — the entire auditing setup can be disabled via `app.jpa.auditing-enabled=false`.
- ✅ **Spring Security optional dependency** — `SecurityAuditorAware` is registered only when `spring-security-core` is on the classpath.

---

## Technologies Used

| Technology | Version |
|---|---|
| Java (runtime) | 21+ |
| Java (built with) | JDK 25 |
| Spring Boot BOM | 4.1.0 |
| Spring Boot Starter Data JPA | (BOM-managed) |
| Spring Security Core | (BOM-managed, optional) |
| Hibernate | (BOM-managed) |
| uuid-creator | 6.1.1 |
| Lombok | 1.18.46 |
| Gradle | (wrapper included) |

> **Runtime requirement:** Consumer services need Java **21 or later**. The library itself is compiled with JDK 25, but the bytecode targets a level compatible with any Java 21+ JVM.

---

## Project Structure

```
lisovskyi-jpa-starter/
├── src/main/java/com/lisovskyi/jpa/autoconfigure/
│   ├── JpaAutoConfiguration.java       # Root auto-configuration; enables JPA auditing
│   ├── JpaProperties.java              # Configuration properties (prefix: app.jpa)
│   ├── audit/
│   │   └── SecurityAuditorAware.java   # Resolves current auditor from SecurityContextHolder
│   ├── entity/
│   │   ├── BaseEntity.java             # UUID v7 id; proxy-safe equals/hashCode
│   │   ├── TimestampedEntity.java      # createdAt + updatedAt (Hibernate timestamps)
│   │   ├── CreationTimestampedEntity.java  # createdAt only
│   │   ├── UpdateTimestampedEntity.java    # updatedAt only
│   │   └── AuditableEntity.java        # Full Spring Data audit (dates + who)
│   └── generator/
│       ├── UuidV7.java                 # @UuidV7 Hibernate generator annotation
│       └── UuidV7Generator.java        # Hibernate IdentifierGenerator implementation
└── src/main/resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## Prerequisites

- Java **21+** (compiled against JDK 25)
- Gradle (wrapper `gradlew` / `gradlew.bat` is bundled)
- A Spring Boot **4.1.0** consumer project with `spring-boot-starter-data-jpa` on the classpath

---

## Installation

The starter is published to the local Maven repository (`mavenLocal()`). Build and publish it first:

```bash
./gradlew publishToMavenLocal
```

### Gradle (Kotlin DSL)

```kotlin
// build.gradle.kts
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.lisovskyi:lisovskyi-jpa-starter:0.1.3")
}
```

### Maven

```xml
<!-- pom.xml -->
<repositories>
  <repository>
    <id>local</id>
    <url>file://${user.home}/.m2/repository</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.lisovskyi</groupId>
    <artifactId>lisovskyi-jpa-starter</artifactId>
    <version>0.1.3</version>
  </dependency>
</dependencies>
```

---

## Configuration

All properties are under the `app.jpa` prefix and are **optional** — defaults are production-ready.

```yaml
# application.yml (consumer service)
app:
  jpa:
    auditing-enabled: true   # default: true — set to false to skip @EnableJpaAuditing entirely
```

### Without Spring Security on the classpath

If `spring-security-core` is not on the classpath, `SecurityAuditorAware` is not registered (guarded by `@ConditionalOnClass`). JPA auditing is still enabled — this is standard Spring Data JPA behaviour: without an `AuditorAware` bean, the framework resolves an empty `Optional` and leaves `createdBy`/`updatedBy` as `null`. This is expected and harmless for services that do not need who-based audit trails.

If you do need `createdBy`/`updatedBy` values without Spring Security, provide your own `AuditorAware<String>` bean:

```java
// Resolved from a thread-local, tenant context, or any other source
@Bean
public AuditorAware<String> auditorAware() {
    return () -> Optional.ofNullable(TenantContext.getCurrentUser()).or(() -> Optional.of("SYSTEM"));
}
```

---

## Usage Examples

### 1. Identity only (`BaseEntity`)

When you only need a stable UUID v7 primary key with no timestamps:

```java
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
}
```

Inherits: `id` (UUID v7). Nothing else is added.

### 2. Entity with timestamps (`TimestampedEntity`)

```java
@Entity
@Table(name = "products")
public class Product extends TimestampedEntity {

    @Column(nullable = false)
    private String name;

    private BigDecimal price;
}
```

Inherits: `id`, `createdAt`, `updatedAt` (populated by Hibernate automatically).

### 3. Fully auditable entity (`AuditableEntity`)

```java
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    @Column(nullable = false)
    private String status;
}
```

Inherits: `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy` — populated automatically on persist/merge via Spring Data JPA auditing.

### 4. Overriding the default `AuditorAware`

Declare your own `AuditorAware<String>` bean — `@ConditionalOnMissingBean` ensures the default `SecurityAuditorAware` is skipped:

```java
@Bean
public AuditorAware<String> customAuditor() {
    return () -> Optional.of(TenantContext.getCurrentUser());
}
```

---

## Configuration Scenarios

### Disable auditing entirely

```yaml
app:
  jpa:
    auditing-enabled: false  # @EnableJpaAuditing is not applied; createdAt/updatedBy remain unmanaged
```

Useful when the consumer application already calls `@EnableJpaAuditing` itself, or auditing is not needed.

### Use with Spring Security (default)

No extra configuration is needed. When `spring-security-core` is on the classpath, `SecurityAuditorAware` is registered automatically and reads the current user from `SecurityContextHolder`. Falls back to `"SYSTEM"` for unauthenticated requests.

### Override the default `AuditorAware`

```java
@Bean
public AuditorAware<String> auditorAware() {
    // Spring Boot skips SecurityAuditorAware because this bean is already present
    return () -> Optional.ofNullable(RequestContext.getCurrentUsername())
                        .or(() -> Optional.of("SYSTEM"));
}
```

---

## Known Limitations

- **Multiple `AuditorAware` beans** — Spring Data JPA requires exactly one. The starter uses `@ConditionalOnMissingBean(AuditorAware.class)`, so declaring your own bean is sufficient to suppress the default. If another dependency also provides one, you'll get a `NoUniqueBeanDefinitionException` — resolve it by declaring a primary bean with `@Primary`.
- **Batch inserts** — `UuidV7Generator` is invoked once per entity during `saveAll`. Monotonic ordering within the same millisecond is preserved by uuid-creator's built-in sub-millisecond counter. No consumer-side action required.
- **Pre-set IDs** — if an entity already carries a non-null `id` (e.g., in tests or data migrations), the generator returns the existing value as-is. This is intentional.
- **Spring Boot version coupling** — the starter imports the Spring Boot 4.1.0 BOM. If your consumer project uses a different BOM version, pin conflicting dependency versions explicitly in your project's dependency management block.

---

## Testing

Run the test suite with:

```bash
./gradlew test
```

---

## Contributing

Contributions are welcome!

1. Fork the repository and create your feature branch from `main`.
2. Make sure the project builds and tests pass locally: `./gradlew build`.
3. Keep code style consistent with the existing conventions (Lombok annotations, `@ConditionalOnMissingBean` for all auto-configured beans).
4. Open a pull request describing what you changed and why.

---

## License

This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.

Key points of Apache 2.0:
- ✅ Free to use, modify, and distribute
- ✅ Can be used in commercial and proprietary projects
- ✅ Patent grant — contributors grant users a license to any patents covering the contribution
- ✅ Must preserve copyright and license notices
- ✅ Changes to the source must be stated
