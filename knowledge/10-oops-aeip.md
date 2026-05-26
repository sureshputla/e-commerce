# OOP Concepts – AEIP

## The Four Pillars of Object-Oriented Programming

| Pillar | One-Line Definition |
|--------|-------------------|
| **A**bstraction | Show only what is necessary, hide complexity |
| **E**ncapsulation | Bundle data + behaviour, control access |
| **I**nheritance | Child class reuses and extends parent class |
| **P**olymorphism | Same name, different behaviour depending on context |

---

## Real-World Analogy First

Think of a **Car**:

| Pillar | Real-World Car Example |
|--------|----------------------|
| Abstraction | You press the accelerator — you don't know how fuel injection works |
| Encapsulation | The engine is closed inside a hood — you can't directly touch the pistons |
| Inheritance | A `SportsCar` is a `Car` — it reuses Car's steering + adds turbo |
| Polymorphism | `car.start()` works for Sedan, SUV, Truck — each starts differently internally |

---

&nbsp;

---

## A – Abstraction

### What Is It?

**Abstraction** means hiding **complex internal details** and exposing only what the user needs to know.

> "Show the *what*, not the *how*."

### Real-World Analogy

When you use an **ATM machine**:
- You see: Insert card, enter PIN, withdraw money
- You don't see: Network communication, bank server, encryption algorithms

The complexity is **abstracted away** from you.

### How to Achieve Abstraction in Java

Two ways:
1. **Abstract class** – partially abstract (can have concrete methods too)
2. **Interface** – fully abstract (only method signatures, no body)

---

### Example 1 – Abstract Class

```java
// Abstract class defines WHAT must exist, not HOW it works
abstract class Animal {

    String name;

    Animal(String name) {
        this.name = name;
    }

    // Abstract method – no body, subclass MUST implement
    abstract void makeSound();

    // Concrete method – shared for all animals
    void breathe() {
        System.out.println(name + " is breathing.");
    }
}

// Subclass provides the HOW
class Dog extends Animal {

    Dog(String name) {
        super(name);
    }

    @Override
    void makeSound() {
        System.out.println(name + " says: Woof! Woof!");
    }
}

class Cat extends Animal {

    Cat(String name) {
        super(name);
    }

    @Override
    void makeSound() {
        System.out.println(name + " says: Meow!");
    }
}

public class Main {
    public static void main(String[] args) {
        Animal dog = new Dog("Bruno");
        Animal cat = new Cat("Whiskers");

        dog.makeSound();   // Bruno says: Woof! Woof!
        cat.makeSound();   // Whiskers says: Meow!

        dog.breathe();     // Bruno is breathing.
        cat.breathe();     // Whiskers is breathing.

        // Animal animal = new Animal("X");  ← ERROR! Can't instantiate abstract class
    }
}
```

---

### Example 2 – Interface

```java
// Interface = 100% abstract contract
interface Shape {
    double area();       // Every shape MUST provide area
    double perimeter();  // Every shape MUST provide perimeter
    void display();      // Every shape MUST provide display
}

class Circle implements Shape {
    double radius;

    Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }

    @Override
    public double perimeter() {
        return 2 * Math.PI * radius;
    }

    @Override
    public void display() {
        System.out.printf("Circle | radius=%.1f | area=%.2f | perimeter=%.2f%n",
                radius, area(), perimeter());
    }
}

class Rectangle implements Shape {
    double length, width;

    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public double area() {
        return length * width;
    }

    @Override
    public double perimeter() {
        return 2 * (length + width);
    }

    @Override
    public void display() {
        System.out.printf("Rectangle | %sx%s | area=%.2f | perimeter=%.2f%n",
                length, width, area(), perimeter());
    }
}

public class Main {
    public static void main(String[] args) {
        Shape circle    = new Circle(5);
        Shape rectangle = new Rectangle(4, 6);

        circle.display();       // Circle | radius=5.0 | area=78.54 | perimeter=31.42
        rectangle.display();    // Rectangle | 4.0x6.0 | area=24.00 | perimeter=20.00

        // User only knows: area(), perimeter(), display()
        // Internal maths is hidden (abstracted)!
    }
}
```

---

### Key Rules

```
✓ Abstract class CAN have:
    - Abstract methods (no body)
    - Concrete methods (with body)
    - Constructors
    - Instance variables

✓ Interface CAN have (Java 8+):
    - Abstract methods (default)
    - Default methods (with body)
    - Static methods
    - Constants (public static final)

✗ You CANNOT:
    - Instantiate an abstract class
    - Instantiate an interface
    - Leave abstract methods unimplemented (compile error)
```

---

&nbsp;

---

## E – Encapsulation

### What Is It?

**Encapsulation** means **bundling data (fields) and methods** that operate on the data into a single unit (class), and **restricting direct access** to the data using access modifiers.

> "Data + Behaviour in one capsule. Access controlled from outside."

### Real-World Analogy

A **medicine capsule**:
- The medicine (data) is inside
- The outer shell (class) controls what gets in/out
- You can't directly touch the powder; you take it through the defined method (swallowing)

### Access Modifiers

```
private   → only inside same class             (most restrictive)
default   → only within same package
protected → same package + subclasses
public    → accessible from everywhere         (least restrictive)
```

---

### Example – Bank Account

```java
class BankAccount {

    // private = data is HIDDEN from outside world
    private String accountHolder;
    private String accountNumber;
    private double balance;

    // Constructor
    public BankAccount(String accountHolder, String accountNumber, double initialBalance) {
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    // Getter – read-only access
    public String getAccountHolder() {
        return accountHolder;
    }

    public String getAccountNumber() {
        return "****" + accountNumber.substring(accountNumber.length() - 4); // Mask account
    }

    public double getBalance() {
        return balance;
    }

    // Controlled write – validation enforced!
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance += amount;
        System.out.printf("Deposited ₹%.2f | New Balance: ₹%.2f%n", amount, balance);
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds.");
        }
        balance -= amount;
        System.out.printf("Withdrawn ₹%.2f | New Balance: ₹%.2f%n", amount, balance);
    }

    @Override
    public String toString() {
        return String.format("Account[holder=%s, number=%s, balance=₹%.2f]",
                accountHolder, getAccountNumber(), balance);
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount account = new BankAccount("Suresh", "123456789012", 10000);

        System.out.println(account);  // Account[holder=Suresh, number=****9012, balance=₹10000.00]

        account.deposit(5000);   // Deposited ₹5000.00 | New Balance: ₹15000.00
        account.withdraw(3000);  // Withdrawn ₹3000.00 | New Balance: ₹12000.00

        // Controlled access
        System.out.println(account.getBalance());  // 12000.0

        // Direct access NOT allowed – compile error!
        // account.balance = -999999;  ← ERROR! private field
        // account.balance += 100000;  ← ERROR! private field

        // Business logic enforced:
        // account.withdraw(999999);  ← Exception: Insufficient funds
        // account.deposit(-100);     ← Exception: Deposit amount must be positive
    }
}
```

---

### Why Encapsulation Matters

```
WITHOUT encapsulation:
    account.balance = -10000;   ← Corrupted data! No validation!
    account.balance = null;     ← Nulls anywhere! Crashes later!

WITH encapsulation:
    account.withdraw(10000);    ← Validates: enough balance?
    account.deposit(-50);       ← Validated: rejects negative
    ← Data always stays valid!
```

---

### Example – Student Grade

```java
class Student {

    private String name;
    private int age;
    private double gpa;

    public Student(String name, int age) {
        setName(name);
        setAge(age);
        this.gpa = 0.0;
    }

    // Getter
    public String getName() { return name; }
    public int getAge()     { return age;  }
    public double getGpa()  { return gpa;  }

    // Setter with VALIDATION
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public void setAge(int age) {
        if (age < 5 || age > 100) {
            throw new IllegalArgumentException("Age must be between 5 and 100.");
        }
        this.age = age;
    }

    public void setGpa(double gpa) {
        if (gpa < 0.0 || gpa > 10.0) {
            throw new IllegalArgumentException("GPA must be between 0.0 and 10.0.");
        }
        this.gpa = gpa;
    }

    @Override
    public String toString() {
        return String.format("Student[name=%s, age=%d, gpa=%.1f]", name, age, gpa);
    }
}
```

---

&nbsp;

---

## I – Inheritance

### What Is It?

**Inheritance** allows a **child class** to acquire properties and methods of a **parent class**, promoting code reuse.

> "A child inherits from a parent. Child can add more, or change what it inherited."

### Real-World Analogy

```
Person
├── name, age, speak(), eat()
│
├── Employee (inherits Person)
│   ├── employeeId, salary, work()
│   │
│   ├── Manager (inherits Employee)
│   │   └── team, conductMeeting()
│   │
│   └── Developer (inherits Employee)
│       └── language, writeCode()
│
└── Student (inherits Person)
    ├── studentId, marks, study()
    └── (also has: name, age, speak(), eat() from Person!)
```

---

### Example – Vehicle Hierarchy

```java
// PARENT CLASS (Base/Superclass)
class Vehicle {

    String brand;
    String model;
    int year;
    int speed = 0;

    Vehicle(String brand, String model, int year) {
        this.brand = brand;
        this.model = model;
        this.year = year;
    }

    void start() {
        System.out.println(brand + " " + model + " started.");
    }

    void stop() {
        speed = 0;
        System.out.println(brand + " " + model + " stopped.");
    }

    void accelerate(int amount) {
        speed += amount;
        System.out.println("Speed: " + speed + " km/h");
    }

    @Override
    public String toString() {
        return brand + " " + model + " (" + year + ")";
    }
}

// CHILD CLASS 1 – inherits Vehicle
class Car extends Vehicle {

    int numberOfDoors;

    Car(String brand, String model, int year, int numberOfDoors) {
        super(brand, model, year);  // Call parent constructor
        this.numberOfDoors = numberOfDoors;
    }

    void openTrunk() {
        System.out.println("Trunk opened for " + brand + " " + model);
    }

    @Override
    public String toString() {
        return super.toString() + " [Car, " + numberOfDoors + " doors]";
    }
}

// CHILD CLASS 2 – inherits Vehicle
class Motorcycle extends Vehicle {

    boolean hasSidecar;

    Motorcycle(String brand, String model, int year, boolean hasSidecar) {
        super(brand, model, year);
        this.hasSidecar = hasSidecar;
    }

    void wheelie() {
        System.out.println(brand + " doing a wheelie!");
    }

    @Override
    public String toString() {
        return super.toString() + " [Motorcycle, sidecar=" + hasSidecar + "]";
    }
}

// GRANDCHILD CLASS – inherits Car
class ElectricCar extends Car {

    int batteryCapacity; // kWh

    ElectricCar(String brand, String model, int year, int batteryCapacity) {
        super(brand, model, year, 4);
        this.batteryCapacity = batteryCapacity;
    }

    void charge() {
        System.out.println(brand + " " + model + " is charging... battery=" + batteryCapacity + "kWh");
    }

    @Override
    void start() {
        System.out.println(brand + " " + model + " silently started (electric).");
    }
}

public class Main {
    public static void main(String[] args) {
        Car car = new Car("Toyota", "Camry", 2024, 4);
        Motorcycle moto = new Motorcycle("Harley", "Sportster", 2023, false);
        ElectricCar tesla = new ElectricCar("Tesla", "Model 3", 2024, 75);

        System.out.println(car);    // Toyota Camry (2024) [Car, 4 doors]
        System.out.println(moto);   // Harley Sportster (2023) [Motorcycle, sidecar=false]
        System.out.println(tesla);  // Tesla Model 3 (2024) [Car, 4 doors]

        car.start();             // Toyota Camry started.
        car.accelerate(60);      // Speed: 60 km/h
        car.openTrunk();         // Trunk opened for Toyota Camry

        moto.start();            // Harley Sportster started.
        moto.wheelie();          // Harley doing a wheelie!

        tesla.start();           // Tesla Model 3 silently started (electric). ← Overridden!
        tesla.accelerate(100);   // Speed: 100 km/h    ← Inherited from Vehicle
        tesla.openTrunk();       // Trunk opened for Tesla Model 3  ← Inherited from Car
        tesla.charge();          // Tesla Model 3 is charging...    ← Own method
    }
}
```

---

### Types of Inheritance

```
✓ Single Inheritance          (Java supports)
    A → B

✓ Multilevel Inheritance      (Java supports)
    A → B → C

✓ Hierarchical Inheritance    (Java supports)
    A → B
    A → C
    A → D

✗ Multiple Inheritance        (Java does NOT support with classes)
    A → C ← B  (forbidden with classes, causes "diamond problem")

✓ Multiple via Interfaces     (Java DOES support)
    class C implements InterfaceA, InterfaceB { }  ← OK!
```

---

### super Keyword

```java
class Animal {
    String name = "Animal";

    void sound() {
        System.out.println("Some sound.");
    }
}

class Dog extends Animal {
    String name = "Dog";

    void printNames() {
        System.out.println(name);        // Dog   (own field)
        System.out.println(super.name);  // Animal (parent's field)
    }

    @Override
    void sound() {
        super.sound();               // Calls parent: "Some sound."
        System.out.println("Woof!"); // Then adds own: "Woof!"
    }
}
```

---

&nbsp;

---

## P – Polymorphism

### What Is It?

**Polymorphism** means "many forms". The same method name behaves **differently** based on the object it acts upon, or the arguments passed.

> "One interface, multiple implementations."

Two types:
1. **Compile-time Polymorphism** – Method Overloading (decided at compile time)
2. **Runtime Polymorphism** – Method Overriding (decided at runtime via dynamic dispatch)

---

### Type 1 – Method Overloading (Compile-Time)

**Same method name**, **different parameter lists** in the **same class**.

```java
class Calculator {

    // Overloaded add() methods – same name, different parameters!
    int add(int a, int b) {
        System.out.print("int add: ");
        return a + b;
    }

    double add(double a, double b) {
        System.out.print("double add: ");
        return a + b;
    }

    int add(int a, int b, int c) {
        System.out.print("3-arg add: ");
        return a + b + c;
    }

    String add(String a, String b) {
        System.out.print("String concat: ");
        return a + b;
    }
}

public class Main {
    public static void main(String[] args) {
        Calculator calc = new Calculator();

        System.out.println(calc.add(5, 3));          // int add: 8
        System.out.println(calc.add(5.5, 3.2));      // double add: 8.7
        System.out.println(calc.add(1, 2, 3));       // 3-arg add: 6
        System.out.println(calc.add("Hello", " World")); // String concat: Hello World
    }
}
```

Java decides **which version to call** at **compile time** based on argument types.

---

### Type 2 – Method Overriding (Runtime)

Child class **redefines** a method from the parent class.

```java
class Employee {
    String name;
    double baseSalary;

    Employee(String name, double baseSalary) {
        this.name = name;
        this.baseSalary = baseSalary;
    }

    // This method will be OVERRIDDEN differently by each subclass
    double calculateBonus() {
        return baseSalary * 0.10;  // Default: 10% bonus
    }

    void printSalaryDetails() {
        double bonus = calculateBonus(); // ← Which version runs? RUNTIME decides!
        System.out.printf("%s | Base: ₹%.0f | Bonus: ₹%.0f | Total: ₹%.0f%n",
                name, baseSalary, bonus, baseSalary + bonus);
    }
}

class Manager extends Employee {
    int teamSize;

    Manager(String name, double baseSalary, int teamSize) {
        super(name, baseSalary);
        this.teamSize = teamSize;
    }

    @Override
    double calculateBonus() {
        return baseSalary * 0.25 + teamSize * 500;  // 25% + ₹500 per team member
    }
}

class SalesExecutive extends Employee {
    double salesTarget;
    double salesAchieved;

    SalesExecutive(String name, double baseSalary, double salesTarget, double salesAchieved) {
        super(name, baseSalary);
        this.salesTarget = salesTarget;
        this.salesAchieved = salesAchieved;
    }

    @Override
    double calculateBonus() {
        double achievement = salesAchieved / salesTarget;
        return baseSalary * achievement * 0.30;  // 30% bonus scaled by achievement
    }
}

class Intern extends Employee {

    Intern(String name, double baseSalary) {
        super(name, baseSalary);
    }

    @Override
    double calculateBonus() {
        return 0;  // No bonus for interns
    }
}

public class Main {
    public static void main(String[] args) {

        // Polymorphism: same type (Employee), different objects
        Employee[] employees = {
                new Employee("Alice", 50000),
                new Manager("Bob", 80000, 10),
                new SalesExecutive("Carol", 40000, 100000, 120000),
                new Intern("Dave", 15000)
        };

        // Same method call: calculateBonus()
        // Different behaviour for each type!
        for (Employee emp : employees) {
            emp.printSalaryDetails();  // Calls the correct OVERRIDDEN version!
        }
    }
}

/*  Output:
    Alice | Base: ₹50000 | Bonus: ₹5000   | Total: ₹55000
    Bob   | Base: ₹80000 | Bonus: ₹25000  | Total: ₹105000
    Carol | Base: ₹40000 | Bonus: ₹14400  | Total: ₹54400
    Dave  | Base: ₹15000 | Bonus: ₹0      | Total: ₹15000
*/
```

At runtime, Java looks at the **actual object type** (not the reference type) and calls the **correct overridden method**. This is called **dynamic dispatch**.

---

### Full Power: Polymorphism + Abstraction Together

```java
interface PaymentGateway {
    boolean processPayment(double amount);
    String getGatewayName();
}

class CreditCardPayment implements PaymentGateway {
    String cardNumber;

    CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("[Credit Card] Charging ₹" + amount + " to card ending " + cardNumber.substring(cardNumber.length() - 4));
        return true;
    }

    @Override
    public String getGatewayName() { return "Credit Card"; }
}

class UpiPayment implements PaymentGateway {
    String upiId;

    UpiPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("[UPI] Sending ₹" + amount + " via " + upiId);
        return true;
    }

    @Override
    public String getGatewayName() { return "UPI"; }
}

class CashOnDelivery implements PaymentGateway {

    @Override
    public boolean processPayment(double amount) {
        System.out.println("[COD] Order placed. Collect ₹" + amount + " at delivery.");
        return true;
    }

    @Override
    public String getGatewayName() { return "Cash on Delivery"; }
}

class OrderCheckout {

    // Polymorphism: accepts ANY payment gateway!
    static void checkout(PaymentGateway gateway, double amount) {
        System.out.print("Processing via " + gateway.getGatewayName() + " → ");
        boolean success = gateway.processPayment(amount);  // Dynamic dispatch!
        System.out.println("Result: " + (success ? "SUCCESS ✓" : "FAILED ✗"));
        System.out.println();
    }
}

public class Main {
    public static void main(String[] args) {
        OrderCheckout.checkout(new CreditCardPayment("4111111111111234"), 1499.00);
        OrderCheckout.checkout(new UpiPayment("suresh@okhdfc"),            749.00);
        OrderCheckout.checkout(new CashOnDelivery(),                       2999.00);
    }
}

/*  Output:
    Processing via Credit Card → [Credit Card] Charging ₹1499.0 to card ending 1234
    Result: SUCCESS ✓

    Processing via UPI → [UPI] Sending ₹749.0 via suresh@okhdfc
    Result: SUCCESS ✓

    Processing via Cash on Delivery → [COD] Order placed. Collect ₹2999.0 at delivery.
    Result: SUCCESS ✓
*/
```

---

&nbsp;

---

## Putting It All Together – Complete Example

All four pillars in one program: a **Library Management System**.

```java
// ===================================
// ABSTRACTION – Interface (contract)
// ===================================
interface LibraryItem {
    String getTitle();
    String getItemType();
    void displayDetails();
    boolean isAvailable();
}

// ===================================
// ENCAPSULATION – Base class with private fields
// ===================================
abstract class MediaItem implements LibraryItem {

    // private = encapsulated
    private final String itemId;
    private final String title;
    private final String author;
    private boolean available;
    private int borrowCount;

    // Controlled constructor
    protected MediaItem(String itemId, String title, String author) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title required");
        this.itemId      = itemId;
        this.title       = title;
        this.author      = author;
        this.available   = true;
        this.borrowCount = 0;
    }

    // Getters (read-only access)
    public String getItemId()   { return itemId;      }
    public String getTitle()    { return title;       }
    public String getAuthor()   { return author;      }
    public boolean isAvailable(){ return available;   }
    public int getBorrowCount() { return borrowCount; }

    // Controlled state change (business rules enforced)
    public void borrow() {
        if (!available) throw new IllegalStateException(title + " is already borrowed.");
        available = false;
        borrowCount++;
        System.out.println("✓ Borrowed: " + title);
    }

    public void returnItem() {
        if (available) throw new IllegalStateException(title + " wasn't borrowed.");
        available = true;
        System.out.println("✓ Returned: " + title);
    }
}

// ===================================
// INHERITANCE – Children reuse + extend parent
// ===================================
class Book extends MediaItem {

    private int pages;
    private String genre;

    Book(String itemId, String title, String author, int pages, String genre) {
        super(itemId, title, author);
        this.pages = pages;
        this.genre = genre;
    }

    @Override
    public String getItemType() { return "Book"; }

    @Override
    public void displayDetails() {
        System.out.printf("[Book] %-30s | Author: %-20s | Pages: %d | Genre: %-12s | %s%n",
                getTitle(), getAuthor(), pages, genre,
                isAvailable() ? "AVAILABLE" : "BORROWED");
    }
}

class DVD extends MediaItem {

    private int durationMinutes;
    private String language;

    DVD(String itemId, String title, String director, int durationMinutes, String language) {
        super(itemId, title, director);
        this.durationMinutes = durationMinutes;
        this.language = language;
    }

    @Override
    public String getItemType() { return "DVD"; }

    @Override
    public void displayDetails() {
        System.out.printf("[DVD]  %-30s | Director: %-20s | Duration: %d min | Lang: %-6s | %s%n",
                getTitle(), getAuthor(), durationMinutes, language,
                isAvailable() ? "AVAILABLE" : "BORROWED");
    }
}

class AudioBook extends MediaItem {

    private double durationHours;
    private String narrator;

    AudioBook(String itemId, String title, String author, double durationHours, String narrator) {
        super(itemId, title, author);
        this.durationHours = durationHours;
        this.narrator = narrator;
    }

    @Override
    public String getItemType() { return "AudioBook"; }

    @Override
    public void displayDetails() {
        System.out.printf("[Audio] %-29s | Author: %-20s | Hours: %.1f     | Narrator: %-10s | %s%n",
                getTitle(), getAuthor(), durationHours, narrator,
                isAvailable() ? "AVAILABLE" : "BORROWED");
    }
}

// ===================================
// POLYMORPHISM – Works with any LibraryItem
// ===================================
class Library {

    private java.util.List<LibraryItem> catalog = new java.util.ArrayList<>();

    void addItem(LibraryItem item) {
        catalog.add(item);
    }

    // Polymorphism: iterates over different types, calls same method
    void listAllItems() {
        System.out.println("\n========== LIBRARY CATALOG ==========");
        for (LibraryItem item : catalog) {
            item.displayDetails();  // Each type displays differently (polymorphism!)
        }
        System.out.println("=====================================");
    }

    // Polymorphism: counts any item type
    long countAvailable() {
        return catalog.stream().filter(LibraryItem::isAvailable).count();
    }

    // Polymorphism: finds any type by title
    LibraryItem findByTitle(String title) {
        return catalog.stream()
                .filter(item -> item.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }
}

// ===================================
// DEMO
// ===================================
public class LibraryDemo {

    public static void main(String[] args) {

        Library library = new Library();

        // Add different types
        library.addItem(new Book("B001", "Clean Code",               "Robert Martin", 431, "Technology"));
        library.addItem(new Book("B002", "Atomic Habits",            "James Clear",   320, "Self-Help"));
        library.addItem(new DVD ("D001", "Inception",                "Christopher Nolan", 148, "English"));
        library.addItem(new DVD ("D002", "3 Idiots",                 "Rajkumar Hirani",   170, "Hindi"));
        library.addItem(new AudioBook("A001","Thinking, Fast & Slow","Daniel Kahneman", 20.5, "Sean Pratt"));

        // List all (POLYMORPHISM – same call, different outputs)
        library.listAllItems();
        System.out.println("Available items: " + library.countAvailable() + "\n");

        // Borrow items (ENCAPSULATION – state change controlled)
        MediaItem cleanCode = (MediaItem) library.findByTitle("Clean Code");
        MediaItem inception = (MediaItem) library.findByTitle("Inception");

        cleanCode.borrow();  // ✓ Borrowed: Clean Code
        inception.borrow();  // ✓ Borrowed: Inception

        System.out.println();
        library.listAllItems();

        // Try invalid operation
        try {
            cleanCode.borrow();  // Should throw exception
        } catch (IllegalStateException e) {
            System.out.println("Error caught: " + e.getMessage());
        }

        // Return
        System.out.println();
        cleanCode.returnItem();

        System.out.println("\nFinal available items: " + library.countAvailable());
    }
}
```

---

## Quick Summary Table

| Pillar | Keyword | Question Answered | Benefit |
|--------|---------|-------------------|---------|
| **Abstraction** | `abstract`, `interface` | *What* does it do? | Hides complexity, defines contracts |
| **Encapsulation** | `private`, getters/setters | *Who* can access? | Data safety, validation, control |
| **Inheritance** | `extends`, `implements` | *Is it* a type of...? | Code reuse, hierarchy |
| **Polymorphism** | `@Override`, overloading | *Which* form is used? | Flexibility, extensibility |

---

## Common Interview Questions

**Q1. Difference between Abstraction and Encapsulation?**
> Abstraction hides *complexity* (you don't know HOW). Encapsulation hides *data* (you can't directly touch it). Abstraction is design, Encapsulation is implementation.

**Q2. Can we have a constructor in an abstract class?**
> Yes! Abstract classes can have constructors, called via `super()` in child classes.

**Q3. What is the difference between method overloading and overriding?**
> Overloading = same method name, different parameters, same class. Decided at *compile time*. Overriding = child redefines parent method, same signature. Decided at *runtime*.

**Q4. Can Java have multiple inheritance?**
> Not with classes (diamond problem). But with interfaces, yes! `class C implements A, B { }`

**Q5. What is dynamic dispatch?**
> At runtime, Java looks at the actual object type (not reference type) and calls the correct overridden method. This is what makes polymorphism work.

---

## Further Reading

- **Head First Java** – Best beginner book on OOP
- **Effective Java by Joshua Bloch** – Advanced OOP patterns
- **Java OOP Tutorial**: https://docs.oracle.com/javase/tutorial/java/concepts/

