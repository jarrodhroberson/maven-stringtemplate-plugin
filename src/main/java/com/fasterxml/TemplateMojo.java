package com.fasterxml;

/* (c) Tatu Saloranta, 2011-
 * 
 * Simple Maven plug-in for executing set of Stringtemplate
 * templates
 */

import java.io.*;
import java.util.*;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.stringtemplate.v4.ST;

/**
 * Beef of the plug-in; place where magic happens
 *
 * @author Tatu Saloranta (tatu.saloranta@iki.fi)
 * @goal process
 * @phase compile
 * @threadSafe
 */
public class TemplateMojo
    extends AbstractMojo
{
    /**
     * Input root directory
     * 
     * @parameter expression="${stringtemplate.inputDir}"
     * @required
     */
    private File inputDir;

    /**
     * File suffix of input files to process, such as ".st"
     * 
     * @parameter expression="${stringtemplate.inputSuffix}"  default-value=".st"
     */
    private String inputSuffix;

    /**
     * Location of the output directory to output results of template expression to
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDir;

    /**
     * File suffix for output files, to replace input suffix.
     * Defaults to empty String, meaning that input suffix is simply removed.
     * 
     * @parameter expression="${stringtemplate.outputSuffix}"  default-value=""
     */
    private String outputSuffix;

    /**
     * Encoding that input files use (and which will also be used for
     * encoding output); default value is "UTF-8"
     * 
     * @parameter expression="${stringtemplate.encoding}" default-value="UTF-8"
     */
    private String encoding;
    
    /**
     * Starting delimiter character for Stringtemplate expressions;
     * defaults to '$'.
     * NOTE: Stringtemplate limits delimiters to a single character, so you can NOT
     * use multi-character delimiters.
     * 
     * @parameter expression="${stringtemplate.startDelimiter}"  default-value='$'
     */
    private char startDelimiter;

    /**
     * Ending (closing) delimiter character for Stringtemplate expressions;
     * defaults to '$'.
     * NOTE: Stringtemplate limits delimiters to a single character, so you can NOT
     * use multi-character delimiters.
     * 
     * @parameter expression="${stringtemplate.endDelimiter}"  default-value="$"
     */
    private char endDelimiter;

    /**
     * We can also define attributes for ST to use. These are defined as
     * a Map in pom.xml
     * 
     * @parameter 
     */
    private Map<String,String> attributes;
    
    /*
    ///////////////////////////////////////////////////////////////////////
    // Actual processing method(s)
    ///////////////////////////////////////////////////////////////////////
     */
    
    /**
     * Main operation
     */
    public void execute()
        throws MojoExecutionException
    {
        // first, find input directory and files it contains
        if (!inputDir.exists()) {
            throw new MojoExecutionException("Input directory '"+inputDir.getAbsolutePath()+"' does not exist");
        }
        Map<File,File> files = new LinkedHashMap<File,File>();
        // looks like maven may change empty String to null?
        String outputSuffix = this.outputSuffix;
        findFiles(files, inputDir, trim(inputSuffix), outputDir, trim(outputSuffix));
        
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        getLog().info(String.format("Found %s template files to process: %s", files.size(), files.toString()));
        for (Map.Entry<File,File> fileEntry : files.entrySet()) {
            File inputFile = fileEntry.getKey();
            File outputFile = fileEntry.getValue();
            try {
                // let's do sanity check... ok to use same dir, but not same dir and same suffix (i.e. can't overwrite input)
                if (inputFile.getCanonicalPath().equals(outputFile.getCanonicalPath())) {
                    new MojoExecutionException("Problem: trying to replace input file '"+inputFile.getCanonicalPath()+"' with output; not allowed");                
                }
            } catch (IOException e) {
                throw new MojoExecutionException("I/O problem: "+e.getMessage(), e);
            }
            String input = readFile(inputFile, encoding);
            ST stringTemplate = new ST(input, startDelimiter, endDelimiter);
            if (attributes != null) {
                for (Map.Entry<String,String> attrEntry : attributes.entrySet()) {
                    stringTemplate.add(attrEntry.getKey(), attrEntry.getValue());
                }
            }
            getLog().info("Read template '"+inputFile.getAbsolutePath()+"'; will process");
            String output = stringTemplate.render();
            writeFile(outputFile, encoding, output);
            getLog().info("Wrote output file '"+outputFile.getAbsolutePath()+"'");
        }
    }

    /*
    ///////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////
     */

    private static String trim(String str)
    {
        if (str == null) {
            return "";
        }
        return str.trim();
    }
    
    private static void findFiles(Map<File,File> result, File inputDir, String inputSuffix,
            File outputDir, String outputSuffix)
    {
        for (File f : inputDir.listFiles()) {
            String name = f.getName();
            if (f.isDirectory()) {
                findFiles(result, f, inputSuffix, new File(outputDir, name), outputSuffix);
            } else if (name.endsWith(inputSuffix)) {
                name = name.substring(0, name.length() - inputSuffix.length()) + outputSuffix;
                result.put(f, new File(outputDir, name));
            }
        }
    }
    
    private final static String readFile(File inputFile, String encoding) throws MojoExecutionException
    {
        StringBuilder sb = new StringBuilder((int) inputFile.length());
        char[] buf = new char[1000];
        int count;

        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(inputFile), encoding);
            while ((count = in.read(buf)) >= 0) {
                sb.append(buf, 0, count);
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read input file '"+inputFile.getAbsolutePath()+"': "+e.getMessage(), e);
        }    
    }
    
    private final static void writeFile(File ouputtFile, String encoding, String contents)
        throws MojoExecutionException
    {
        try {
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(ouputtFile), encoding);
            w.write(contents);
            w.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write output file '"+ouputtFile.getAbsolutePath()+"': "+e.getMessage(), e);
        }    
    }

}
