# Klojang JDBC

<i>Klojang JDBC</i> is a thin, non-intrusive abstraction layer around standard JDBC that
builds on <i>[Klojang Templates](https://github.com/klojang4j/klojang-templates)</i> to
provide functionality not commonly found in JDBC extension libraries.

## Getting Started

To use _Klojang JDBC_, add the following dependency to your Maven POM file:

```xml
<dependency>
    <groupId>org.klojang</groupId>
    <artifactId>klojang-jdbc</artifactId>
    <version>1.1.5</version>
</dependency>
```

or Gradle build script:

```
implementation group: 'org.klojang', name: 'klojang-jdbc', version: '1.1.5'
```

## Example

Here is an example that takes you full-circle from CREATE TABLE via INSERT to SELECT:

```java
public record Person(Integer personId, String firstName, String lastName, LocalDate birthDate) {
  
  public Person(String firstName, String lastName, LocalDate birthDate) {
    this(null, firstName, lastName, birthDate);
  }
     
}
```

```java
try(Connection con = ...) {

  String sql = """
          CREATE TABLE PERSON(
            PERSON_ID INT AUTO_INCREMENT, 
            FIRST_NAME VARCHAR(255),
            LAST_NAME VARCHAR(255),
            BIRTH_DATE DATE)
          """;

  SQL.staticSQL(sql).session(con).prepareUpdate().execute();
   
  List<Person> persons = List.of(
       new Person("John", "Smith", LocalDate.of(1960, 4, 15)),
       new Person("Mary", "Smith", LocalDate.of(1980, 10, 5)),
       new Person("Joan", "de Santos", LocalDate.of(1977, 5, 23)),
       new Person("Jill", "Kriel", LocalDate.of(1977, 2, 10)),
       new Person("Stephen", "Bester", LocalDate.of(2001, 2, 8)),
       new Person("Carlos", "Smith", LocalDate.of(2004, 2, 8)),
       new Person("Mary", "Bear", LocalDate.of(1956, 11, 7)),
       new Person("Dieter", "Washington", LocalDate.of(1989, 2, 4)),
       new Person("Peter", "Peterson", LocalDate.of(1963, 5, 3)),
       new Person("Joe", "Peterson", LocalDate.of(1998, 9, 23)));
   
  SQLBatchInsert sbi = SQL.insertBatch()
       .of(Person.class)
       .into("PERSON")
       .excluding("personId")
       .prepare(con);
  sbi.insertBatch(persons);

  // Here we parametrize the value for LAST_NAME using a named 
  // parameter while we parametrize the ORDER BY clause using 
  // Klojang Templates variables
  sql = """
        SELECT * FROM PERSON
         WHERE LAST_NAME = :lastName
         ORDER BY ~%sortColumn%
        """;
   
  SQLSession session = SQL.template(sql).session(con);
  session.setIdentifier("sortColumn", "FIRST_NAME");
  try (SQLQuery query = session.prepareQuery()) {
    List<Person> persons = query
          .bind("lastName", "Smith")
          .getExtractor(Person.class)
          .extractAll();
    for (Person person : persons) {
      System.out.println(person);
    }
  }

}
```

## Documentation

The **Javadocs** for <i>Klojang JDBC</i> can be
found **[here](https://klojang4j.github.io/klojang-jdbc/api)**.

The latest <b>vulnerabilities report</b> can be found
**[here](https://klojang4j.github.io/klojang-jdbc/vulnerabilities/dependency-check-report.html)**.

The latest **test coverage results**
are **[here](https://klojang4j.github.io/klojang-jdbc/coverage)**.

## About

<img src="docs/logo-groen.png" style="float:left;width:5%;padding:0 12px 12px 0"/>

<i>Klojang JDBC</i> is developed by [Naturalis](https://www.naturalis.nl/en), a
biodiversity research institute and natural history museum. It maintains one
of the largest collections of zoological and botanical specimens in the world.
