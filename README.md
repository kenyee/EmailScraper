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
    java -jar build/libs/EmailScraper-1.0.jar <domain-name>
`````

You can pass in "-noajax" as a parameter if you want to see how well the site
gets parsed with using Selenium; if the site doesn't use AJAX, this will be a *lot* faster.

### Notes
Tried using HtmlUnit for AJAX sites and it was terrible and threw a lot of
weird errors.  Selenium worked a lot better though much slower than using
jsoup for non-AJAX sites.
