# Java 17 & Java Virtual Machine (JVM)

## What is Java?

Java is a **general-purpose, object-oriented programming language** created by Sun Microsystems in 1995. It's designed to be:
- **Platform-independent** – "Write once, run anywhere"
- **Secure** – Sandboxed execution model
- **Robust** – Strong type system, automatic memory management
- **High-performance** – Just-in-time (JIT) compilation

## What is the Java Virtual Machine (JVM)?

The **JVM** is an **abstract computing machine** that enables your compiled Java code to run on any device or operating system that has a JVM installed.

```
Source Code (.java)
    ↓ javac (compiler)
Bytecode (.class)
    ↓ JVM
Machine Code (CPU-specific)
    ↓
Execution on any OS (Windows, macOS, Linux)
```

### Key Process

1. **Compilation**: Java source code → Bytecode (JVM instructions)
2. **Loading**: Bytecode loaded into memory via ClassLoader
3. **Verification**: Bytecode verified for security & integrity
4. **Execution**: Bytecode executed by JVM interpreter or JIT compiler

---

## Why Java 17?

**Java 17** is a Long-Term Support (LTS) release (released September 2021), meaning:

✅ **5 years of commercial support** (until September 2026)  
✅ **Stable, production-ready** – enterprise-grade reliability  
✅ **Modern features** – sealed classes, pattern matching, records  
✅ **Performance improvements** – better GC, JIT compiler enhancements  
✅ **Security patches** – regular security updates  

### Key Features in Java 17

| Feature | What It Is | Example |
|---------|-----------|---------|
| **Records** | Immutable data carrier class | `record Product(int id, String name) {}` |
| **Sealed Classes** | Restrict inheritance | `sealed class Shape permits Circle, Square {}` |
| **Pattern Matching** | Simplified instanceof | `if (obj instanceof String s) { use s; }` |
| **Text Blocks** | Multi-line strings | `"""..."""` |

---

## JVM Memory Model

```
┌─────────────────────────────────────────┐
│         JAVA VIRTUAL MACHINE            │
├─────────────────────────────────────────┤
│                                         │
│  HEAP (Shared across all threads)      │
│  ├── Objects                            │
│  ├── Arrays                             │
│  └── [Garbage Collected]                │
│                                         │
│  STACK (Per thread)                    │
│  ├── Local variables                   │
│  ├── Method calls                      │
│  └── References to heap objects        │
│                                         │
│  CLASS AREA (Shared, not GC'd)         │
│  ├── Class definitions                 │
│  ├── Method code                       │
│  └── Runtime constants                 │
│                                         │
│  GARBAGE COLLECTOR                     │
│  └── Automatically frees unused objects│
│                                         │
└─────────────────────────────────────────┘
```

## Bytecode Example

Your Java code:
```java
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

Compiles to bytecode (disassembled):
```
public int add(int, int);
  Code:
     0: iload_1          // load first parameter (a)
     1: iload_2          // load second parameter (b)
     2: iadd             // add them
     3: ireturn          // return result
```

The JVM then interprets or JIT-compiles these bytecode instructions into native machine code.

---

## Garbage Collection (GC)

The JVM **automatically frees memory** when objects are no longer referenced. This prevents memory leaks.

```java
// Without GC (C/C++):
Person p = new Person("Alice");
// manually: delete p;  ← ERROR-PRONE!

// With GC (Java):
Person p = new Person("Alice");
// when p goes out of scope, GC automatically frees it
```

### GC Generations

Java uses **generational GC** – most objects die young:

```
Young Generation          Old Generation
(frequently GC'd)         (rarely GC'd)
├── Eden Space            └── Tenured Space
└── Survivor Spaces          (long-lived objects)
```

---

## In Our Project

### Where We Use Java 17

```xml
<!-- pom.xml -->
<java.version>17</java.version>
```

### Java 17 Features We Use

1. **Records** – Data carriers:
   ```java
   public record FilterResponse(List<String> categories, List<String> brands) {}
   ```

2. **Pattern Matching**:
   ```java
   if (product instanceof ProductDto dto) {
       log.debug("Product: {}", dto.name());
   }
   ```

3. **Sealed Classes** (could use for entities):
   ```java
   sealed class OrderStatus permits PENDING, CONFIRMED, CANCELLED {}
   ```

---

## JVM Tuning in Production

Our services use default JVM settings, but production typically tweaks:

```bash
# Heap size
java -Xms512M -Xmx2G application.jar

# Garbage collector choice
java -XX:+UseG1GC application.jar

# Enable performance monitoring
java -XX:+UnlockDiagnosticVMOptions -XX:+ShowMessageBoxOnError
```

---

## Real-World Analogy

Think of the JVM like a **universal translator**:

- **You speak Java** (source code)
- **Compiler translates to bytecode** (neutral language)
- **JVM translates bytecode to your computer's native language** (machine code)
- **Works the same on Windows, Mac, Linux** – the universal translator handles it!

---

## Comparison with Other Languages

| Aspect | Java | C++ | Python |
|--------|------|-----|--------|
| Compilation | Bytecode (JVM) | Native | Interpreted |
| Platform | Windows, Mac, Linux | Must recompile | Windows, Mac, Linux |
| Memory Mgmt | Automatic (GC) | Manual | Automatic |
| Performance | Very fast (JIT) | Fastest (native) | Slowest |
| Learning Curve | Moderate | Steep | Easiest |

---

## Further Reading

- **Oracle Java Documentation**: https://docs.oracle.com/javase/17/
- **JVM Bytecode**: https://en.wikipedia.org/wiki/Java_bytecode
- **Garbage Collection**: https://www.baeldung.com/jvm-garbage-collectors

---

**Next**: Learn how Spring Boot runs on top of Java → [Spring Boot 3](./02-spring-boot-3.md)

