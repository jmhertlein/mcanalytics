# mcanalytics - Analytics for Minecraft

`mcanalytics` consists of two parts: `mcanalytics-plugin`, a Bukkit1.8/Spigot plugin, and `mcanalytics-console`, a desktop application to view the charts produced by the server. 

# Server Setup

`mcanalytics-plugin` requires:

* Java 8
* database access (mysql or postgres)
* Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files

## Java 8

Ensure you're running Java 8.

    java -version

should print something like:

    openjdk version "1.8.0_51"
    OpenJDK Runtime Environment (build 1.8.0_51-b16)
    OpenJDK 64-Bit Server VM (build 25.51-b03, mixed mode)

The `1.8` part is the most important.

If you're not running Java 8, grab it here: http://www.oracle.com/technetwork/java/javase/downloads/server-jre8-downloads-2133154.html


## JCE Policy Files

Download here: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

`mcanalytics` uses modern cryptography to protect passwords and network communications. This requires these policy files be installed.

## Database

Use either mysql or postgres. Make sure that, whatever you choose, `plugins/MCAnalytics/config.yml` is updated to reflect the connection details. Specifically, the `type:` can be either `mysql` or `postgres`.

# Desktop Setup

`mcanalytics-console` requires Java 8 and JavaFX- everything else is bundled. 

Java 8 can be found here: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

## Oracle Java

JavaFX is bundled with your JRE. Nothing needs to be done.

## OpenJDK

You will need to get ahold of `openjfx`. If you're using Arch Linux, just run `pacman -S java-openjfx`.

If you're using anything else, you may have to compile it yourself. I use Arch, so I didn't need to go down this rabbit hole, so I leave you with the link below and wish you good luck.

https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX

# Usage

1. Start server.
2. Run `mca adduser username password`, replacing username and password with the username and password you wish to log in with. Note, this will end up in your server log. Pick a dummy password and change it securely from the console, later.
3. Launch the console.
4. Click "new"
5. Type in a nick for the server (doesn't matter, just what gets shown in your local menu), and the hostnme and port. The default port is `35555`.
6. Click login. If you want to skip typing your password every time, check the "remember" checkbox. Don't worry, your password doesn't get stored.
7. Pick a chart from the drop-down, specify the dates to pull data from, and click "Search"

# License

GPLv3

# Contributing

Pull requests, bug reports, etc are welcome. Use the GitHub issues page.
