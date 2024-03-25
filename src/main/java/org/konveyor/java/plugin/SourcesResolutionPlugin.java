package org.konveyor.java.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;

import javax.inject.Inject;

public abstract class SourcesResolutionPlugin implements Plugin<Project> {

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        Configuration runtimeClasspath = project.getConfigurations().getByName("runtimeClasspath");
        project.getTasks().register("resolveSources", ResolveDocsTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Resolve source artifacts for all runtime dependencies");

            // Set task input and output conventions
            ArtifactView docsView = buildDocumentationView(runtimeClasspath, DocsType.SOURCES);
            task.getDocs().from(docsView.getFiles());
            task.getDestinationDir().convention(project.getLayout().getBuildDirectory().dir("sources"));

            // Always rerun this task
            task.getOutputs().upToDateWhen(e -> false);
        });

    }

    /**
     * Sets up an ArtifactView based on this project's runtime classpath which will fetch documentation.
     *
     * @param graph    the resolution graph to retrieve artifacts from
     * @param docsType the type of documentation artifact the returned view will fetch
     * @return ArtifactView which will fetch documentation
     */
    private ArtifactView buildDocumentationView(Configuration graph, String docsType) {
        return graph.getIncoming().artifactView(view -> {
            view.setLenient(true);

            // Uncomment me to view the new behavior
            view.withVariantReselection();

            AttributeContainer attributes = view.getAttributes();
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, getObjectFactory().named(Category.class, Category.DOCUMENTATION));
            attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, getObjectFactory().named(Bundling.class, Bundling.EXTERNAL));
            attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjectFactory().named(DocsType.class, docsType));
            attributes.attribute(Usage.USAGE_ATTRIBUTE, getObjectFactory().named(Usage.class, Usage.JAVA_RUNTIME));
        });
    }
}
