package de.dfki.mary.voicebuilding.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.workers.*

import javax.inject.Inject

class ProcessWav extends DefaultTask {

    @Internal
    final WorkerExecutor workerExecutor

    @InputFiles
    FileCollection srcFiles

    @OutputDirectory
    File destDir

    @Inject
    ProcessWav(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
    }

    @TaskAction
    void process() {
        def soxPath = System.env['PATH'].split(':').collect { dir ->
            new File(dir, 'sox')
        }.find { it.exists() }
        assert soxPath
        srcFiles.each { wavFile ->
            def destFile = project.file("$destDir/$wavFile.name")
            workerExecutor.submit(RunnableExec.class) { WorkerConfiguration config ->
                def cmd = [soxPath, wavFile, destFile, 'rate', project.voice.samplingRate]
                def args = [commandLine: cmd]
                config.params args
                config.isolationMode = IsolationMode.PROCESS
            }
        }
    }
}