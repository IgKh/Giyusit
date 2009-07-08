This directory contains the library JARs used by Giyusit. The build system
automatically adds every JAR file stored here to the classpath, and merges
it to the distribution JAR as part of the release proccess.

For various reasons, the libraries themselves are not put under version
contorl. Instead, when one sets up a fresh Giyusit development enviorment,
he must place the required JARs in this directory:

    - Qt Jambi (native & binding JARS, at least version 4.4.3)
    - SQLite JDBC driver
    - JExcelAPI

