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
        project.extensions.create("stringsconverter", ConverterExtension)
        // Add a task that uses the configuration

        project.build {

            dependsOn {
                project.task('stringsconverter', type: Exec) {

                    String mode = project.stringsconverter.mode
                    String resourcesDir = project.stringsconverter.resourcesDir

                    def name = project.name
                    resourcesDir = project.projectDir.absolutePath + "\\" + resourcesDir

                    def clientId = project.stringsconverter.clientId
                    def clientSecret = project.stringsconverter.clientSecret
                    def spreadSheetKey  = project.stringsconverter.spreadsheetKey
                    ClassLoader classLoader = getClass().getClassLoader()
                    def fis = classLoader.getResourceAsStream("stringsConverter.py")

                    def languages          = project.stringsconverter.languages.join(' ')
                    println languages

                    def credentiallocation = project.stringsconverter.credentiallocation
                    if(credentiallocation == null){
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
                    commandLine 'py', tmpFile.getPath(), '-gcid',clientId, '-gcsecret', clientSecret, '-cl', credentiallocation, '-sk',spreadSheetKey, '-m', mode, '-l', languages, "-o", resourcesDir, name
                }
            }
        }
    }
}


class ConverterExtension {


    def static final DEFAULT_RESOURCES_PATH = 'src\\main\\res'
    def String mode = 'retrieve'
    def String resourcesDir = DEFAULT_RESOURCES_PATH
    def String clientId = ''
    def String clientSecret = ''
    def String spreadsheetKey = ''
    def String credentiallocation = null
    String[]   languages
 }

