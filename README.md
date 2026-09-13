# lisovskyi-jpa-starter

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=springboot)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-Apache%202.0-green)

A Spring Boot auto-configuration library that standardises JPA persistence across microservices. It provides a per-entity database-sequence identity strategy, a hierarchy of auditable base entities, and Spring Data JPA auditing wired automatically to the Spring Security context — all with zero boilerplate in consumer services.

---

## Project Overview

Every JPA-based microservice needs the same scaffolding: a consistent ID generation strategy, `createdAt`/`updatedAt` timestamps, and `createdBy`/`updatedBy` audit trails. Without a shared library this logic is either duplicated across services or implemented inconsistently.

`lisovskyi-jpa-starter` solves this by shipping:

- A custom **database-sequence ID generator** whose sequence name is derived automatically from the entity class name, and plugs directly into Hibernate.
- A hierarchy of **`@MappedSuperclass` base entities** covering every combination of identity, timestamps, and audit metadata.
- A **`SecurityAuditorAware`** implementation that reads the current user from the Spring Security context (falls back to `"SYSTEM"` when no authentication is present).
- A **`JpaAutoConfiguration`** that enables `@EnableJpaAuditing` safely — guarded by `@ConditionalOnMissingBean(name = "jpaAuditingHandler")` to prevent double-registration conflicts.

---

## Why a per-entity sequence?

A common Hibernate default is a single shared `hibernate_sequence` for every entity in the application. That works, but it means every insert across every table contends for the same sequence, and there is no way to tell — from the sequence alone — which table an ID gap came from.

`EntitySequenceGenerator` derives a dedicated sequence per entity from the class name (`UserEntity` → `user_entity_seq_gen`, `ProductOrder` → `product_order_seq_gen`), so:

| Property | Shared `hibernate_sequence` | Per-entity sequence |
|---|---|---|
| No cross-table contention | ❌ | ✅ |
| Sequence name traceable to a table | ❌ | ✅ |
| Per-entity allocation-size tuning | ❌ | ✅ (`@SequenceSize`) |
| Zero boilerplate (`@Id` + `@EntitySequence`) | ✅ | ✅ |

It still builds on Hibernate's own `SequenceStyleGenerator`, so DDL generation, allocation-size batching, and multi-database support (PostgreSQL, H2, Oracle, …) all work exactly as they would with a hand-declared `@SequenceGenerator`.

---

## Features

- ✅ **Per-entity sequence ID generation** — `EntitySequenceGenerator` derives a dedicated `{entity_class_name}_seq_gen` sequence per entity. Pre-set IDs (tests, data migrations) are preserved on merge/programmatic insert.
- ✅ **`@EntitySequence` / `@SequenceSize` annotations** — `@EntitySequence` wires the generator onto `BaseEntity.id`; `@SequenceSize` overrides the default allocation size (50) per entity class.
- ✅ **Entity hierarchy**:
  - `BaseEntity` — sequence-generated `Long id`, proper `equals`/`hashCode` safe with Hibernate proxies.
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
| Spring Boot BOM | 4.1.1 |
| Spring Boot Starter Data JPA | (BOM-managed) |
| Spring Security Core | (BOM-managed, optional) |
| Hibernate | (BOM-managed) |
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
│   │   ├── BaseEntity.java             # Sequence-generated Long id; proxy-safe equals/hashCode
│   │   ├── TimestampedEntity.java      # createdAt + updatedAt (Hibernate timestamps)
│   │   ├── CreationTimestampedEntity.java  # createdAt only
│   │   ├── UpdateTimestampedEntity.java    # updatedAt only
│   │   └── AuditableEntity.java        # Full Spring Data audit (dates + who)
│   └── generator/
│       ├── EntitySequence.java         # @EntitySequence — wires the generator onto @Id
│       ├── EntitySequenceGenerator.java    # Hibernate SequenceStyleGenerator subclass
│       └── SequenceSize.java           # @SequenceSize — per-entity allocation-size override
└── src/main/resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## Prerequisites

- Java **21+** (compiled against JDK 25)
- Gradle (wrapper `gradlew` / `gradlew.bat` is bundled)
- A Spring Boot **4.1.1** consumer project with `spring-boot-starter-data-jpa` on the classpath

---

## Installation

Published to Maven Central via the [`com.vanniktech.maven.publish`](https://github.com/vanniktech/gradle-maven-publish-plugin) plugin — no extra repository declaration is needed beyond `mavenCentral()`.

### Gradle (Kotlin DSL)

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.lisovskyi-arsenii:lisovskyi-jpa-starter:1.0.0")
}
```

### Maven

```xml
<!-- pom.xml -->
<dependencies>
  <dependency>
    <groupId>io.github.lisovskyi-arsenii</groupId>
    <artifactId>lisovskyi-jpa-starter</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

To build and test a change locally without waiting on a Central release, publish to your local Maven repository instead:

```bash
./gradlew publishToMavenLocal
```

then add `mavenLocal()` to `repositories { }` in the consumer project.

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

When you only need a stable sequence-generated primary key with no timestamps:

```java
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
}
```

Inherits: `id` (`Long`, backed by the `tag_seq_gen` sequence). Nothing else is added.

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

### 5. Overriding the sequence allocation size

By default, `EntitySequenceGenerator` pre-fetches 50 IDs per round-trip (Hibernate's own default). For a low-traffic entity where gaps from unused pre-fetched values matter more than round-trip cost, override it with `@SequenceSize`:

```java
@SequenceSize(size = 10)
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity extends BaseEntity {

    private String action;
}
```

Backed by the `audit_log_entity_seq_gen` sequence, allocating 10 IDs per round-trip instead of the default 50.

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
- **Batch inserts** — `EntitySequenceGenerator` extends Hibernate's `SequenceStyleGenerator`, so `saveAll` benefits from the same allocation-size pre-fetch batching as a hand-declared `@SequenceGenerator`. No consumer-side action required.
- **Pre-set IDs** — if an entity already carries a non-null `id` (e.g., in tests or data migrations), the generator returns the existing value as-is. This is intentional.
- **Spring Boot version coupling** — the starter imports the Spring Boot 4.1.1 BOM. If your consumer project uses a different BOM version, pin conflicting dependency versions explicitly in your project's dependency management block.

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
3. Keep code style consistent with the existing conventions (plain Java getters/setters, `@ConditionalOnMissingBean` for all auto-configured beans).
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
