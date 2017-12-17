package a02_dependencies;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

interface IDependencyService {
}

class DependencyService implements IDependencyService {
}

class CachedDependencyService implements IDependencyService {
}

public class InjectingDependenciesTest {

    static class EditorField {
        @Inject
        private IDependencyService dependencyService;

        public IDependencyService getDependencyService() {
            return dependencyService;
        }
    }

    @Test
    public void shouldInjectService_whenFieldIsAnnotated() {
        // Given
        Injector injector = Guice.createInjector(binder -> {
            binder.bind(IDependencyService.class).to(DependencyService.class);
        });

        // When
        EditorField editor = injector.getInstance(EditorField.class);

        // Then
        assertThat(editor.getDependencyService(), notNullValue());
    }

    static class EditorSet {
        private IDependencyService dependencyService;

        @Inject
        void setDependencyService(IDependencyService dependencyService) {
            this.dependencyService = dependencyService;
        }

        public IDependencyService getDependencyService() {
            return dependencyService;
        }
    }

    @Test
    public void shouldInjectService_whenSetIsAnnotated() {
        // Given
        Injector injector = Guice.createInjector(binder -> {
            binder.bind(IDependencyService.class).to(DependencyService.class);
        });

        // When
        EditorSet editor = injector.getInstance(EditorSet.class);

        // Then
        assertThat(editor.getDependencyService(), notNullValue());
    }

    static class EditorConstructor {
        private IDependencyService dependencyService;

        @Inject
        public EditorConstructor(IDependencyService dependencyService) {
            this.dependencyService = dependencyService;
        }

        public IDependencyService getDependencyService() {
            return dependencyService;
        }
    }

    @Test
    public void shouldInjectService_whenConstructorIsAnnotated() {
        // Given
        Injector injector = Guice.createInjector(binder -> {
            binder.bind(IDependencyService.class).to(DependencyService.class);
        });

        // When
        EditorConstructor editor = injector.getInstance(EditorConstructor.class);

        // Then
        assertThat(editor.getDependencyService(), notNullValue());
    }

    static class EditorBothServices {
        private IDependencyService dependencyService;
        private IDependencyService cachedDependencyService;

        @Inject
        public EditorBothServices(IDependencyService dependencyService,
                                  @Named("Cached") IDependencyService cachedDependencyService) {
            this.dependencyService = dependencyService;
            this.cachedDependencyService = cachedDependencyService;
        }

        public IDependencyService getDependencyService() {
            return dependencyService;
        }

        public IDependencyService getCachedDependencyService() {
            return cachedDependencyService;
        }
    }

    @Test
    public void shouldInjectNamed() {
        // Given
        Injector injector = Guice.createInjector(binder -> {
            binder.bind(IDependencyService.class).to(DependencyService.class);
            binder.bind(IDependencyService.class).annotatedWith(Names.named("Cached")).to(CachedDependencyService.class);
        });

        // When
        EditorBothServices editor = injector.getInstance(EditorBothServices.class);

        // Then
        assertThat(editor.getDependencyService(), instanceOf(DependencyService.class));
        assertThat(editor.getCachedDependencyService(), instanceOf(CachedDependencyService.class));
    }

    static class EditorBothServicesAnnotation {
        private IDependencyService dependencyService;
        private IDependencyService cachedDependencyService;

        @Inject
        public EditorBothServicesAnnotation(IDependencyService dependencyService,
                                  @Cached IDependencyService cachedDependencyService) {
            this.dependencyService = dependencyService;
            this.cachedDependencyService = cachedDependencyService;
        }

        public IDependencyService getDependencyService() {
            return dependencyService;
        }

        public IDependencyService getCachedDependencyService() {
            return cachedDependencyService;
        }
    }

    @Test
    public void shouldInjectAnnotated() {
        // Given
        Injector injector = Guice.createInjector(binder -> {
            binder.bind(IDependencyService.class).to(DependencyService.class);
            binder.bind(IDependencyService.class).annotatedWith(Cached.class).to(CachedDependencyService.class);
        });

        // When
        EditorBothServicesAnnotation editor = injector.getInstance(EditorBothServicesAnnotation.class);

        // Then
        assertThat(editor.getDependencyService(), instanceOf(DependencyService.class));
        assertThat(editor.getCachedDependencyService(), instanceOf(CachedDependencyService.class));
    }

}
