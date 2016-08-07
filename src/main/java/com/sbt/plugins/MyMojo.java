package com.sbt.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Goal which touches a timestamp file.
 *
 * @deprecated Don't use!
 */
@Mojo(name = "version_file", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MyMojo
        extends AbstractMojo {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.build.sourceEncoding}", property = "sourceEncoding")
    private String charset = "UTF-8";

    public void execute()
            throws MojoExecutionException {
        File outputDir = outputDirectory;

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File logsFile = new File(outputDir, "version.txt");

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(logsFile), Charset.forName(charset));
            writeLogsInWriter(writer);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + logsFile, e);
        } catch (GitAPIException e) {
            throw new MojoExecutionException("Git error  " + logsFile, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void writeLogsInWriter(Writer writer) throws IOException, GitAPIException {
        Repository existingRepo = new FileRepositoryBuilder().setGitDir(new File("./.git")).build();
        Git git = new Git(existingRepo);
        LogCommand logCommand = git.log();
        Iterable<RevCommit> logs = logCommand.call();
        for (RevCommit current : logs) {
            writer.write(current.getName() + ": " + current.getFullMessage());
            writer.write(LINE_SEPARATOR);
        }
    }
}
