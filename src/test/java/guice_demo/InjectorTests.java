package guice_demo;

import com.google.inject.*;
import com.google.inject.name.Names;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class InjectorTests {

    /***************************************************************
     * No module or explicit binding is defined so JIT binding is used https://github.com/google/guice/wiki/JustInTimeBindings
     ***************************************************************/

    static class SimpleEditor {
        SimpleEditor() {
            System.out.println("Constructor SimpleEditor");
        }
    }

    @Test
    public void noModuleOrExplicitBinding() {
        Injector injector = Guice.createInjector();

        SimpleEditor editor = injector.getInstance(SimpleEditor.class);
        SimpleEditor editor2 = injector.getInstance(SimpleEditor.class);
        assertThat(editor, not(nullValue()));
        assertThat(editor, not(equalTo(editor2))); // by default new instance is created
    }

    @Test
    public void moduleAndScopeSingleton() {
        Module module = binder -> {
            binder.bind(SimpleEditor.class).in(Singleton.class); // we can also annotate SimpleEditor with @Singleton
        };
        Injector injector = Guice.createInjector(module);

        SimpleEditor editor = injector.getInstance(SimpleEditor.class);
        SimpleEditor editor2 = injector.getInstance(SimpleEditor.class);
        assertThat(editor, not(nullValue()));
        assertThat(editor, equalTo(editor2));

        assertThat(editor, not(equalTo(new SimpleEditor()))); // constructors should be package private to prevent this

    }

    /***************************************************************
     *                     Injecting dependencies
     ***************************************************************/

    interface IDependencyService {
    }

    static class DependencyService implements IDependencyService {
    }

    static class Editor {
        final IDependencyService dependencyService;

        @Inject
        Editor(IDependencyService dependencyService) {
            this.dependencyService = dependencyService;
        }
    }

    @Test
    public void usingModule() {
        Module module = binder -> {
            binder.bind(IDependencyService.class).to(DependencyService.class);
        };
        Injector injector = Guice.createInjector(module);

        Editor editor = injector.getInstance(Editor.class);
        assertThat(editor.dependencyService, is(notNullValue()));
    }

    /***************************************************************
     *                     Injecting annotated
     ***************************************************************/

    static class NewDependencyService implements IDependencyService {
    }

    static class Editor2 {
        final IDependencyService dependencyService;

        final IDependencyService newDependencyService;

        String role;

        @Inject
        Editor2(IDependencyService dependencyService, @OurAnnotation IDependencyService newDependencyService) {
            this.dependencyService = dependencyService;
            this.newDependencyService = newDependencyService;
        }

        @Inject
        void setRole(@Named("role") String role) {
            this.role = role;
        }
    }

    @Test
    public void annotatedDependency_toInstanceBinding_injectingUsingMethod() {
        Module module = binder -> {
            binder.bind(IDependencyService.class).to(DependencyService.class);
            binder.bind(IDependencyService.class).annotatedWith(OurAnnotation.class).to(NewDependencyService.class);
            binder.bind(String.class).annotatedWith(Names.named("role")).toInstance("Project Developer");
        };
        Injector injector = Guice.createInjector(module);

        Editor2 editor = injector.getInstance(Editor2.class);
        assertThat(editor.dependencyService, instanceOf(DependencyService.class));
        assertThat(editor.newDependencyService, instanceOf(NewDependencyService.class));
        assertThat(editor.role, equalTo("Project Developer"));
    }

    /*****************************************************************
     *                      Providers
     *****************************************************************/

    enum UserRole {
        PRODUCT
    }

    static class Resource {
    }

    static class Editor3 {
        final UserRole userRole;
        final Provider<Resource> resourceProvider; // see comment below

        @Inject
        Editor3(UserRole userRole, Provider<Resource> resourceProvider) {
            this.userRole = userRole;
            this.resourceProvider = resourceProvider;
        }
    }

    Provider<UserRole> userRoleProvider = () -> {
        System.out.println("Providing user role");
        return UserRole.PRODUCT;
    };

    /**
     * Providers are injected when:
     * <ul>
     * <li>multiple instances are needed</li>
     * <li>lazy loading - expensive to create, esp. when not always needed</li>
     * <li>mixing scope - singleton needed something from session scope</li>
     * </ul>
     */
    @Test
    public void providers() {

        Module module = new AbstractModule() {
            @Provides
            Resource resultTypeIsImportant() {
                System.out.println("Providing resource");
                return new Resource();
            }

            @Override
            protected void configure() {
                bind(UserRole.class).toProvider(userRoleProvider);
            }
        };
        Injector injector = Guice.createInjector(module);

        Editor3 editor = injector.getInstance(Editor3.class);
        assertThat(editor.userRole, equalTo(UserRole.PRODUCT));
        assertThat(editor.resourceProvider.get(), instanceOf(Resource.class));
        assertThat(editor.resourceProvider.get(), instanceOf(Resource.class));
    }
}
