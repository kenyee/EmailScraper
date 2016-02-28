# EmailScraper
This project is for experimenting with web scraping tools to scrape emails off
various web sites.

The current version supports using:
    - [jsoup](http://jsoup.org)
    - [jBrowserDriver](https://github.com/MachinePublishers/jBrowserDriver/) with jsoup.

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

### Notes
Tried using HtmlUnit for AJAX sites and it was terrible and threw a lot of
weird errors.  Selenium worked a lot better though much slower than using
jsoup for non-AJAX sites.
