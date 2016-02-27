# EmailScraper
This project is for experimenting with web scraping tools to scrape emails off
various web sites.

The current version supports using:
    - [jsoup](http://jsoup.org)
    - [htmlunit](http://htmlunit.sourceforge.net) with jsoup.

### Building
This project can be built as a single fatJar using this command:

`````
    gradle fatJar
`````

### Running
Once you build it, you can run it by typing in 
(assuming you have the Java JDK installed and it's in your path):

`````
    java -jar build/libs/emailscraper-1.0.jar <domain-name>
`````

### Limitations
It currently does not support scraping sites that use AJAX (aka Single Page Apps like
those built with Angular or React).
