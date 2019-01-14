package com.ins.gradle.plugin.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

/**
 * Created by Jonathan DA ROS on 07/01/2019.
 */

class Converter implements Plugin<Project> {
    void apply(Project project) {
        // Add the 'stringsconverter' extension object
        def extension = project.extensions.create("stringsconverter", ConverterExtension)
        // Add a task that uses the configuration

        project.task('converterTask', type: Exec) {
            doFirst {
                String mode = extension.mode
                String resourcesDir = extension.resourcesDir

                def name = project.name
                resourcesDir = project.projectDir.absolutePath + "\\" + resourcesDir

                def clientId = extension.clientId
                def clientSecret = extension.clientSecret
                def spreadSheetKey = extension.spreadsheetKey
                ClassLoader classLoader = getClass().getClassLoader()
                def fis = classLoader.getResourceAsStream("stringsConverter.py")

                def languages = extension.lang
                println languages

                def credentiallocation = extension.credentiallocation
                if (credentiallocation == null) {
                    credentiallocation = project.rootProject.getBuildDir()
                }
                def tmp = new File(project.getBuildDir().toString() + "\\tmp")
                if (!tmp.exists()) tmp.mkdirs()

                def tmpFile = new File(project.getBuildDir().toString() + "\\tmp\\stringsConverter.py")
                FileOutputStream fos = new FileOutputStream(tmpFile)
                int c

                while ((c = fis.read()) != -1) {
                    fos.write(c)
                }

                fis.close()
                fos.close()

                println mode
                println resourcesDir


                workingDir project.projectDir
                commandLine 'py', tmpFile.getPath(), '-gcid', clientId, '-gcsecret', clientSecret, '-cl', credentiallocation, '-sk', spreadSheetKey, '-m', mode, '-l', languages, "-o", resourcesDir, name
            }

        }

//        project.preBuild {
//            dependsOn converterTask
//        }
    }
}

class ConverterExtension {


    def static final DEFAULT_RESOURCES_PATH = 'src\\main\\res'
    String mode = 'retrieve'
    String resourcesDir = DEFAULT_RESOURCES_PATH
    String clientId = ''
    String clientSecret = ''
    String spreadsheetKey = ''
    String credentiallocation = null
    String   lang


}

