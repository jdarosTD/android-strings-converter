# android-strings-converter



This gradle plugin provides you a way to manage your Android App/Module strings resources from/to a google sheet file. 

Nota Bene. At this time, the plugin is not available from any public repository. You will have to to install it in your own 

## Setup
* ### Step 1 : Adding plugin to your project

  From your project build file add the the dependency in the buildScript
  
  ```groovy
    buildscript {
      ext.pluginVersion = '1.2'

      repositories {
          mavenLocal()
      }

      dependencies {
          classpath "com.ins.gradle.plugin.android:strings-converter:$pluginVersion"
      }
  }
  ```
 
  If your Android App is managed through different modules, you can apply the plugin to each one if they include some strings resources 
 
   ```groovy
   apply plugin: 'strings-converter'
   ```
 * ### Step 2 : Configuration
 
    ####  to apply on each module :
    
    ```groovy
    stringsconverter {
    
        mode = 'retrieve'
        clientId = rootProject.ext.clientId
        clientSecret = rootProject.ext.clientSecret
        spreadsheetKey =  rootProject.ext.spreadsheetKey
        lang = rootProject.ext.languages
    }
    ```
    
    **There are 2 differents modes :**
    
      * Retrieve : to retrieve the remote google sheet file to your local project
      * Apply    : to apply your local resources file to your remote google sheet
      
    **Google authentication :**
    
    The plugin uses oAuth2 to connect to your google spreadsheet.
      Through the google console api, you will have :
      * To active the google sheet api
      * Create an OAuth 2.0 client id
      * Add the client id and client secret in your build file (or your gradle global properties) as defined above
    
    Then, from your google drive, create a single google sheet file, and add its key in your build.gradle as defined above 
       
    **!! NB  :** Actually, you will have create one tab per module. The name of the tab has to be the name of the module. Auto creation of the tab will come soon
    
    
    **Languages :**
    
    You will have to define the languages you manage in your app. There must be at least one language defined.
    There is also a "Main" language defined as in Android as a default language
    
    *Example:*
    
    ```groovy
   stringsconverter {
        ...
       lang = "fr, es"
   }
    ```
       
     In this example, we are considering English as the default language
     
     **!! NB  :** Actually there is no "header" generated in the Google sheet, so you have to keep the order of the languages in your file and in your configuration. Evolution will come soon.

     * ### Step 3 : Launch the script with a simple build
     
     The plugin is defined as it is a dependency of the "preBuild" task in the Android Gradle Plugin. On the first launch, the plugin will launch a browser for you to let you connect on your google account.
     
     If you don't want to apply this plugin on every build you make, you can consider adding a property like for instance : 
     
     ```groovy
     if(!project.hasProperty("noStringPlugin")) {
       apply plugin: 'strings-converter'

        stringsconverter {
            ...
        }
      }
      ```
      
      and add your property on your build command line
      
      ```
      gradle app:assembleDebug -PnoStrConverter
    
