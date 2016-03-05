# EmailScraper
This project is for experimenting with web scraping tools to scrape emails off
various web sites.

The current version supports using:

* [jsoup](http://jsoup.org)
* [jBrowserDriver](https://github.com/MachinePublishers/jBrowserDriver/)
* [Ui4j](https://github.com/ui4j/ui4j)

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

Other Parameters:

* -noajax : You can pass in "-noajax" as a parameter if you want to see how well the site
gets parsed with using Selenium; if the site doesn't use AJAX, this will be a *lot* faster.

* -maxthreads: For extra speed if you have the bandwidth, you can use the
"-maxthreads" parameter followed by the number of threads.  If you use too many
threads, you'll overload your bandwidth or heap; I'd recommend no more than 10.
NOTE: this only works if you have -noajax set.

* -ui4j: You can alternatively use Ui4j as the AJAX page downloader via the "-ui4j" parameter, 
but you have to uncomment the compile Monocle statement in the build.gradle; 
this library conflicts with jBrowserDriver (if you include Monocle, 
jBrowserDriver won't run properly).  If anyone
knows how to fix this, please let me know or put in a Pull Request.

### Notes
Tried using HtmlUnit for AJAX sites and it was terrible and threw a lot of
weird errors.  Selenium worked a lot better though much slower than using
jsoup for non-AJAX sites.

Ui4j isn't appreciably different from jBrowserDriver; it includes progress info,
but has a worse API.  jBrowserDriver seems to be the best compromise.