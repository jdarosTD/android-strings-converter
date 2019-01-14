package com.ins.gradle.plugin.android

import org.gradle.api.DefaultTask

/**
 * Created by Jonathan DA ROS on 11/01/2019.
 */
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction

class DeploymentPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = 'deployments'

    private static final String DEPLOY_TASK_PATTERN = 'deployOn%sTo%s'

    private static final String REPORTING_TASK_NAME = 'reportDeployments'

    private static final String TASK_GROUP_NAME = 'Deployment'

    void apply(final Project project) {

        def extension = project.extensions.findByName("stringsconverter")

        setupExtension(project)
        createDeploymentTasks(project)
        createReportTask(project)
    }

    /**
     * Create extension on the project for handling the deployments
     * definition DSL with servers and nodes. This allows the following DSL
     * in our build script:
     * <pre>
     * deployments {
     *     server1 {
     *         url = 'http://server'
     *         nodes {
     *             node1 {
     *                 port = 9000
     *             }
     *         }
     *     }
     * }
     * </pre>
     */
    private void setupExtension(final Project project) {

        // Create NamedDomainObjectContainer for Server objects.
        // We must use the container() method of the Project class
        // to create an instance. New Server instances are
        // automatically created, because we have String argument
        // constructor that will get the name we use in the DSL.
        final NamedDomainObjectContainer<Server> servers =
                project.container(Server)

        servers.all {
            // Here we have access to the project object, so we
            // can use the container() method to create a
            // NamedDomainObjectContainer for Node objects.
            nodes = project.container(Node)
        }

        // Use deployments as name in the build script to define
        // servers and nodes.
        project.extensions.add(EXTENSION_NAME, servers)
    }

    /**
     * Create a new deployment task for each node.
     */
    private void createDeploymentTasks(final Project project) {
        def servers = project.extensions.getByName(EXTENSION_NAME)


        servers.all {
            // Actual Server instance is the delegate
            // of this closure. We assign it to a variable
            // so we can use it again inside the
            // closure for nodes.all() method.
            def serverInfo = delegate
            println("Servers : " + serverInfo.name)
            nodes.all {
                // Assign this closure's delegate to
                // variable so we can use it in the task
                // configuration closure.
                def nodeInfo = delegate

                // Make node and server names pretty
                // for use in task name.
                def taskName =
                        String.format(
                                DEPLOY_TASK_PATTERN,
                                name.capitalize(),
                                serverInfo.name.capitalize())

                // Create new task for this node.
                project.task(taskName, type: DeploymentTask) {
                    description = "Deploy to '${nodeInfo.name}' on '${serverInfo.name}'"
                    group = TASK_GROUP_NAME

                    server = serverInfo
                    node = nodeInfo
                }
            }
        }
    }

    /**
     * Add reporting task to project.
     */
    private void createReportTask(final Project project) {
        project.task(REPORTING_TASK_NAME, type: DeploymentReportTask) {
            description = 'Show configuration of servers and nodes'
            group = TASK_GROUP_NAME
        }
    }
}

class Node {

    String name

    Integer port

    /**
     * We need this constructor so Gradle can create an instance
     * from the DSL.
     */
    Node(String name) {
        this.name = name
    }
}

class DeploymentTask extends DefaultTask {

    Server server

    Node node

    /**
     * Simple implementation to show we can
     * access the Server and Node instances created
     * from the DSL.
     */
    @TaskAction
    void deploy() {
        println "Deploying to ${server.url}:${node.port}"
    }

}


class DeploymentReportTask extends DefaultTask {

    /**
     * Simple task to show we can access the
     * Server and Node instances also via the
     * project extension.
     */
    @TaskAction
    void report() {
        def servers = project.extensions.getByName(DeploymentPlugin.EXTENSION_NAME)

        servers.all {
            println "Server '${name}' with url '${url}':"

            nodes.all {
                println "\tNode '${name}' using port ${port}"
            }
        }
    }

}

class Server {

    /**
     * An instance is created in the plugin class, because
     * there we have access to the container() method
     * of the Project object.
     */
    NamedDomainObjectContainer<Node> nodes

    String url

    String name

    /**
     * We need this constructor so Gradle can create an instance
     * from the DSL.
     */
    Server(String name) {
        this.name = name
    }

    /**
     * Inside the DSL this method is invoked. We use
     * the configure method of the NamedDomainObjectContainer to
     * automatically create Node instances.
     * Notice this is a method not a property assignment.
     * <pre>
     * server1 {
     *     url = 'http://server1'
     *     nodes { // This is the nodes() method we define here.
     *         port = 9000
     *     }
     * }
     * </pre>
     */
    def nodes(final Closure configureClosure) {
        nodes.configure(configureClosure)
    }


}

class PluginExtension {
    NamedDomainObjectContainer<Server> servers
}