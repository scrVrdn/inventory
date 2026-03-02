# Inventory: Collection Manager

<div align="center">
<img src="https://img.shields.io/badge/Java-007396?style=flat&logo=java&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white"/>
<img src="https://img.shields.io/badge/JavaFX-FC4C02?style=flat&logo=javafx&logoColor=white"/>
<img src="https://img.shields.io/badge/SQLite-003B57?style=flat&logo=sqlite&logoColor=white"/>
<img src="https://img.shields.io/badge/FXML-FC4C02?style=flat&logoColor=white"/>
<img src="https://img.shields.io/badge/CSS-1572B6?style=flat&logo=css3&logoColor=white"/>
</div>

## Quick Start

```bash
git clone https://github.com/scrVrdn/inventory.git
cd inventory
./mvnw spring-boot:run
```
(On first startup, the app creates its database in ```~/.inventory/``` (your user home directory).)

## What it does

A Spring Boot desktop app for managing book collections (for now, but easily expandable to other item types), intended for private collections where the number of items doesn't exceed ~10k 

* full CRUD functionality

* details pane for editing entries: a full entry holds information on book title, author(s), editor(s), publisher, year of publication, ISBN-10/ISBN-13 and shelf mark

* read-optimized view of whole collection via JdbcTemplate + complex joins

* custom pagination controls with random access page indices for targeted perusal of the collection

* server-side sorting and filtering

* full backup functionality



## Architecture

![Architecture](diagrams/architecture.drawio.svg)


#### Key Decisions

* for proper encapsulation, the (fxml) controller class has only direct access to the facade service EntryService which orchestrates the domain services und the backup services

* For a quick and efficient loading of complete entries, EntryService bypasses the domain services and calls the (read only) EntryViewRepository directly; this way it obtains the requested data via a single query (+ a quick lookup of the total number of entries in a dedicated 1-row table) (without filtering) or two queries (with filtering)

* No JPA/Hibernate, instead JdbcTemplate for database access: no ORM overhead, SQLite optimized

* Lazy approach to updating for user convenience: updating persons or publishers results in creating a new table entry (or if an identical entry already exists, this will be used instead); then at the next start up of the app all unused persons or publishers (i. e. those with no association with a book_id) will be deleted from the database via the cleanup services. No need for multiple forms to manage persons and publishers.


## Tech Stack

### Backend

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=spring-boot&logoColor=white&style=flat)
![Spring](https://img.shields.io/badge/Spring-6DB33F?logo=spring&logoColor=white&style=flat)
![JdbcTemplate](https://img.shields.io/badge/JdbcTemplate-orange?logo=datajoint&logoColor=white&style=flat)
![SQLite](https://img.shields.io/badge/SQLite-003B57?logo=sqlite&logoColor=white&style=flat)

### Frontend
<div>
  <img src="https://img.shields.io/badge/JavaFX-FC4C02?logo=javafx&logoColor=white&style=flat" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/FXML-FC4C02?logo=fxml&logoColor=white&style=flat" alt="FXML"/>
  <img src="https://img.shields.io/badge/CSS-1572B6?logo=css3&logoColor=white&style=flat" alt="CSS"/>
</div>



### Core

<img src="https://img.shields.io/badge/Java-007396?logo=java&logoColor=white&style=flat" alt="Java"/>
