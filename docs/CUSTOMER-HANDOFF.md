# Customer Handoff Guide

This guide describes the simplest first-release handoff method for the Jig & Toolings Management System.

## Recommended Simple Handoff

Use one customer PC or one small internal server.

The customer machine needs:

- Java 17
- MySQL 8.x
- The application JAR file
- The MySQL database dump
- The uploaded files folder

## Delivery Folder

Prepare one folder like this:

```text
JJ jig toolings customer handoff package/
  app/
    jig-toolings-management.jar
    application.properties
    start-system.bat
    stop-system.bat
  database/
    schema.sql
    default-users.sql
    sample-data.sql
    install-database.bat
    import-sample-data.bat
  uploads/
    jigs/
  scripts/
    backup-now.bat
    open-firewall-8080.bat
  documents/
    windows-installation-guide.md
    admin-operation-guide.md
    user-quick-guide.md
    handoff-checklist.md
  README-FIRST.md
```

## Basic Installation Steps

1. Install Java 17.
2. Install MySQL 8.x.
3. Copy the handoff folder to the customer server PC, for example `C:\JJ-Jig-Toolings\`.
4. Edit `app\application.properties` if the MySQL password is not blank.
5. Run `database\install-database.bat`.
6. Run `app\start-system.bat`.
7. Open the browser on the server PC.

```text
http://localhost:8080
```

8. For other users on the company LAN, open:

```text
http://SERVER-IP:8080
```

## Daily Backup

Back up these two items every day:

- MySQL database dump
- `uploads/jigs/` folder

The current Windows handoff package includes `scripts\backup-now.bat` for a simple manual backup.

## Notes

- This Java 17 + MySQL + JAR method is the simplest first-release handoff.
- For factory-wide use, deploy the app on an internal server and give users a LAN URL.
- Docker can be considered later, but it is not necessary for the simplest customer handoff.
- If other LAN computers cannot connect, run `scripts\open-firewall-8080.bat` as Administrator.
