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
    
    private void findFiles(Map<File,File> result, File inputDir, String inputSuffix,
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
    
    public void execute()
        throws MojoExecutionException
    {
        // first, find input directory and files it contains
        if (!inputDir.exists()) {
            throw new MojoExecutionException( "Input directory '"+inputDir.getAbsolutePath()+"' does not exist");
        }
        String suffix = inputSuffix.trim();
        Map<File,File> files = new LinkedHashMap<File,File>();
        findFiles(files, inputDir, suffix, outputDir, outputSuffix.trim());

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        getLog().info(String.format("Found %s template files: %s", files.size(), files.toString()));
    }
}
