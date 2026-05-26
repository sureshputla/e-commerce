# Apache Maven

## What is Maven?

**Maven** is a **build tool** and **dependency manager** for Java projects. It automates:
- Compiling code
- Managing dependencies (libraries)
- Running tests
- Packaging the application
- Deploying artifacts

```
Without Maven (Manual hell):
├─ Download JUnit jar
├─ Download Mockito jar
├─ Download Spring jar
├─ Download Hibernate jar
├─ ... 50 more jars
└─ Manually add to classpath
   ← Version conflicts!
   ← Dependency conflicts!

With Maven (Automated):
pom.xml
  ├─ <dependency>junit</dependency>
  ├─ <dependency>mockito</dependency>
  └─ <dependency>spring</dependency>

$ mvn install
  ↓
Maven downloads all dependencies
INCLUDING their dependencies (transitive)
Handles conflicts intelligently
```

---

## Key Concepts

### pom.xml (Project Object Model)

**Single source of truth** for your project configuration.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.sureshputla</groupId>      <!-- Organization identifier -->
    <artifactId>order-service</artifactId>  <!-- Project name -->
    <version>1.0-SNAPSHOT</version>         <!-- Current version -->
    
    <name>order-service</name>
    <description>Order management service</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.3.5</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Dependency Coordinates

Every dependency has unique address:

```
groupId (company/org):       org.springframework.boot
artifactId (project name):   spring-boot-starter-web
version:                     3.3.5

Like: company/project/version
```

---

## Maven Lifecycle

```bash
$ mvn clean install
```

Executes entire lifecycle:

```
clean
  ├─ Delete target/ directory (cleanup)
  │
compile
  ├─ Compile src/main/java → target/classes
  ├─ Process annotations
  └─ Generate code (Lombok, MapStruct)
  
test
  ├─ Compile src/test/java
  ├─ Run test classes
  └─ Generate test reports
  
package
  ├─ Create JAR/WAR
  ├─ Embed Spring Boot classloader
  └─ target/order-service-1.0-SNAPSHOT.jar
  
install
  ├─ Copy JAR to local repository (~/.m2/)
  └─ Available for other projects to depend on
```

### Common Commands

```bash
mvn clean              # Delete target/
mvn compile            # Compile code
mvn test               # Run tests
mvn package            # Create JAR
mvn install            # Install to local repo
mvn deploy             # Deploy to remote repo
mvn clean package      # Clean + package (common)
mvn clean install      # Clean + install (all steps)
mvn spring-boot:run    # Run Spring Boot app directly
```

---

## Dependency Management

### Transitive Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.3.5</version>
</dependency>
```

This single dependency brings in:
- spring-boot-starter (base framework)
- spring-boot-autoconfigure
- spring-webmvc
- spring-web
- jackson (JSON processing)
- tomcat (embedded server)
- ... and many more!

Maven resolves all **transitive dependencies** automatically. 🎉

### Dependency Scope

```xml
<!-- Compile: included in JAR, needed at runtime -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <scope>compile</scope>  <!-- Default -->
</dependency>

<!-- Test: only used during testing, NOT in JAR -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <scope>test</scope>
</dependency>

<!-- Provided: available at runtime (e.g., servlet container provides it) -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>

<!-- Runtime: not needed for compilation, only at runtime -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## In Our Project

### Parent POM

```xml
<!-- pom.xml (root) -->
<project>
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.sureshputla</groupId>
    <artifactId>ecommerce-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>common</module>
        <module>service-registry</module>
        <module>api-gateway</module>
        <module>product-service</module>
        <module>cart-service</module>
        <!-- ... more modules -->
    </modules>
</project>
```

### Child Module (e.g., order-service)

```xml
<!-- order-service/pom.xml -->
<project>
    <parent>
        <groupId>com.sureshputla</groupId>
        <artifactId>ecommerce-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>order-service</artifactId>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <!-- version inherited from parent -->
        </dependency>
        
        <dependency>
            <groupId>com.sureshputla</groupId>
            <artifactId>common</artifactId>  <!-- Our shared module -->
        </dependency>
    </dependencies>
</project>
```

### Build

```bash
# Build all modules
cd /path/to/e-commerce
mvn clean install

# Build specific module
mvn -pl order-service clean install

# Build and skip tests
mvn clean install -DskipTests
```

---

## Plugins

Plugins add custom behavior to Maven lifecycle.

```xml
<plugins>
    <!-- Spring Boot Maven Plugin -->
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <excludes>
                <exclude>org.projectlombok:lombok</exclude>
            </excludes>
        </configuration>
    </plugin>
    
    <!-- Compiler Plugin (configure Java version) -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
            <source>17</source>
            <target>17</target>
            <annotationProcessorPaths>
                <!-- Annotation processors: Lombok, MapStruct -->
            </annotationProcessorPaths>
        </configuration>
    </plugin>
    
    <!-- Surefire Plugin (run tests) -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
    </plugin>
</plugins>
```

---

## Repository

Where Maven stores downloaded dependencies.

### Local Repository

```
~/.m2/repository/
├── org/
│   └── springframework/
│       └── boot/
│           └── spring-boot-starter-web/
│               └── 3.3.5/
│                   └── spring-boot-starter-web-3.3.5.jar
├── junit/
│   └── junit/
│       └── 4.13.2/
│           └── junit-4.13.2.jar
└── ... (all your dependencies)
```

Downloaded once, reused by all projects!

### Remote Repository

```
Central Repository (default):
  https://repo.maven.apache.org/maven2/
  
Custom Repository (private):
  https://myrep.company.com/repository/
```

Configure in `pom.xml`:

```xml
<repositories>
    <repository>
        <id>company-repo</id>
        <url>https://myrep.company.com/repository/</url>
    </repository>
</repositories>
```

---

## Version Numbering

```
1.0-SNAPSHOT   (development version – changes)
1.0-BETA       (beta version)
1.0-RC1        (release candidate 1)
1.0            (stable release)
1.1-SNAPSHOT   (next development version)
```

**SNAPSHOT** versions are for active development:
```
$ mvn deploy  (publishes 1.0-SNAPSHOT)
$ mvn deploy  (overwrites 1.0-SNAPSHOT again)
```

Release versions are immutable:
```
$ mvn deploy  (publishes 1.0)
$ mvn deploy  (ERROR! 1.0 already published!)
→ Must bump version to 1.1-SNAPSHOT
```

---

## Multi-Module Projects

Perfect for microservices!

```
ecommerce/
├── pom.xml (parent)
├── common/
│   ├── pom.xml (child)
│   └── src/
├── order-service/
│   ├── pom.xml (child)
│   └── src/
├── payment-service/
│   ├── pom.xml (child)
│   └── src/
└── ... (more services)

$ mvn clean install

Builds ALL modules
Respects inter-module dependencies
Handles versions centrally
```

---

## CLI vs IDE

### Command Line

```bash
mvn clean install
mvn spring-boot:run
mvn test
```

### IDE (IntelliJ / Eclipse)

Right-click project → Run Maven → ...
Or built-in IDE integration!

Maps to same Maven commands under the hood.

---

## Advantages

✅ **Standardization** – All Java projects follow same structure  
✅ **Dependency management** – Automatic version resolution  
✅ **Reproducible builds** – Same `pom.xml` = same JAR  
✅ **Convention over configuration** – Minimal config needed  
✅ **Large ecosystem** – Millions of libraries available  

---

## Disadvantages

❌ **Verbose** – `pom.xml` can get long  
❌ **Setup time** – Slow on first run (downloads dependencies)  
❌ **Version conflicts** – Can be complex to debug  
❌ **Learning curve** – Lifecycle, plugins, scopes to understand  

---

## Alternatives

| Tool | Type | Language | Learning |
|------|------|----------|----------|
| **Maven** | Dependency + Build | Java | Moderate |
| **Gradle** | Modern build | Java | Moderate |
| **Ant** | Build | Java | Steep |
| **npm** | Dependency | JavaScript | Easy |
| **pip** | Dependency | Python | Easy |

---

## Real-World Analogy

Maven is like a **grocery delivery service**:

- **You write list**: "I need Spring Boot, JUnit, Mockito"
- **Service understands**: "Spring Boot needs X, Y, Z"
- **Service delivers**: All dependencies, including sub-dependencies
- **Cached locally**: Don't download twice
- **Different recipes**: Test dependencies vs compile dependencies

---

## Further Reading

- **Maven Official**: https://maven.apache.org/
- **Maven POM Reference**: https://maven.apache.org/pom.html

---

**You're now equipped with comprehensive knowledge of the technologies powering this microservices platform!** 🚀

