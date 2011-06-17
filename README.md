Wouldn't it be nice to combine simplicity and power of stringtemplate
(http://www.stringtemplate.org/) with convenience and power of Maven
build tool? I think so -- yet could not find such combination.
But this small project aims to fix this deficiency.

# Usage

Required information for running is to know:

* groupId: com.fasterxml
* artifactId: maven-stringtemplate-plugin

and goal, which is usually 'process'.

Given this, you would add something like this in your Maven pom.xml:

    <plugin>
      <groupId>com.fasterxml</groupId>
      <artifactId>maven-stringtemplate-plugin</artifactId>
      <version>{LATEST YOU CAN FIND}</version>
      <configuration>
          <inputDir>src/templates</inputDir>
      </configuration>
    </plugin>

# Current status

''IMPORTANT''! Plug-in is work in progress, and DOES NOT YET WORK. :)

# Configuration

Following properties can be configured (note that these are properties
to add in pom.xml, within <configuration> -- equivalent system properties
use "stringtemplate." prefix)

* inputDir (no default): Defines input directory (relative to project base directory) -- all files (with suitable suffix) will be processed, recursively
* outputDir (defaults to '${project.build.directory}'): Directory under which processed template output is stored; retains input structure for sub-directories
* inputSuffix (defaults to ".st"): Suffix of input files to process; only files that end with the suffix are processed
* outputSuffix (defaults to ""): Suffix to use for output files, instead of input suffix (i.e. inputSuffix is replaced with outputSuffix)

