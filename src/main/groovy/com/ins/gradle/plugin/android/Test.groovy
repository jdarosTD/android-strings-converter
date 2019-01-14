//package com.ins.gradle.plugin.android
//
//import org.gradle.api.DefaultTask
//import org.gradle.api.NamedDomainObjectContainer
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import org.gradle.api.tasks.Exec
//import org.gradle.api.tasks.TaskAction
//
///**
// * Created by Jonathan DA ROS on 07/01/2019.
// */
//
//class Converter implements Plugin<Project> {
//
//    public static final String EXTENSION_NAME = 'stringsconverter'
//    public static final String LANG_EXTENSION_NAME = 'languages'
//    private static final String RETRIEVE_TASK_PATTERN = 'retrieve%s'
//
//    void apply(Project project) {
//        setupExtension(project)
////        createTasks(project)
//
//        createTask(project)
//
//    }
//
//    void createTask(Project project) {
//        ConverterExtension extension = project.extensions.getByName(EXTENSION_NAME)
//
//
//        project.project.task("retrieveLanguages", type: DoItTask){
//            mode = extension.mode
//            resourcesDir = extension.resourcesDir
//            name       =  project.name
//            languages  =   extension.lang
//            clientId =  extension.clientId
//            clientSecret = extension.clientSecret
//            spreadSheetKey = extension.spreadsheetKey
//            credentiallocation = extension.credentiallocation
//        }
//    }
//
//    private void setupExtension(final Project project) {
//
//        def extension = project.extensions.create(EXTENSION_NAME, ConverterExtension)
//
//
//        final NamedDomainObjectContainer<Language> languages =
//                project.container(Language)
//
//        languages.all {
//            toto = delegate.name
//        }
//
//        // Use deployments as name in the build script to define
//        // servers and nodes.
//        project.extensions.add(LANG_EXTENSION_NAME, languages)
//
//
//    }
//
////
////    private void createTasks(final Project project ){
////        def languages = project.extensions.getByName(LANG_EXTENSION_NAME)
////        ConverterExtension extension = project.extensions.getByName(EXTENSION_NAME)
////
////
////        languages.all {
////            def langInfo = delegate
////            println("Lang " + langInfo.name)
////            def taskName = String.format(RETRIEVE_TASK_PATTERN, langInfo.name.capitalize())
////            def langStr =  langInfo.name
////            project.project.task(taskName, type: RetrievingTask){
////                lang = langStr
////                mode = extension.mode
////
////            }
////        }
////    }
//}
//
//
//class RetrievingTask extends DefaultTask {
//
//    public String lang
//    public  String mode
//
//    /**
//     * Simple implementation to show we can
//     * access the Server and Node instances created
//     * from the DSL.
//     */
//    @TaskAction
//    void retrieve() {
//        println "Retrieving language  ${lang}, mode ${mode}"
//    }
//
//}
//
//class DoItTask  extends Exec {
//
//    public String  mode
//    public String resourcesDir
//    public String name
//    public String clientId
//    public String clientSecret
//    public String spreadSheetKey
//    public String languages
//    public String credentiallocation
//
//    public File scriptFile
//    DoItTask(){
//
//        resourcesDir = project.projectDir.absolutePath + "\\" + resourcesDir
//        if(credentiallocation == null){
//            credentiallocation = project.rootProject.getBuildDir()
//        }
//
//        println("Languages : " + languages)
//        scriptFile = new File(project.getBuildDir().toString() + "\\tmp\\stringsConverter.py")
//
//
//        setWorkingDir(project.projectDir)
//        setCommandLine('echo toto')
////        setCommandLine('py', scriptFile.getPath(), '-l', languages, '-gcid',clientId,
////                '-gcsecret', clientSecret, '-cl', credentiallocation,
////                '-sk',spreadSheetKey, '-m', mode, "-o", resourcesDir, name
////        )
//    }
//
//
//    @TaskAction
//    protected void exec() {
//
//
//        def tmp = new File(project.getBuildDir().toString() + "\\tmp")
//        if (!tmp.exists()) tmp.mkdirs()
//
//        ClassLoader classLoader = getClass().getClassLoader()
//        def fis = classLoader.getResourceAsStream("stringsConverter.py")
//
//
//        FileOutputStream fos = new FileOutputStream(scriptFile)
//        int c
//
//        while ((c = fis.read()) != -1) {
//            fos.write(c)
//        }
//
//        fis.close()
//        fos.close()
//
//        super.exec()
//
//
//    }
//
//}
//
//class Language {
//
//    public String name
//    public String toto
//    Language(String name){
//        this.name = name
//    }
//}
//
//class ConverterExtension {
//
//
//    def static final DEFAULT_RESOURCES_PATH = 'src\\main\\res'
//    public  String mode = 'retrieve'
//    public  String resourcesDir = DEFAULT_RESOURCES_PATH
//    public  String clientId = ''
//    public   String clientSecret = ''
//    public  String spreadsheetKey = ''
//    public  String credentiallocation = null
//    public String lang
//}
//
