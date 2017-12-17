package a01_injector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Test;

import javax.inject.Singleton;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

interface IEditor {
}

class Editor implements IEditor {
    Editor() {
        System.out.println("Creating editor");
    }

}

class Editor2 implements IEditor {
    Editor2() {
        System.out.println("Creating editor2");
    }
}


public class InjectorTests {

    @Test
    public void shouldReturnInstance_whenNoModuleAndRetrievedByClass() {
        Injector injector = Guice.createInjector();

        // no module or explicit binding is defined so JIT binding is used https://github.com/google/guice/wiki/JustInTimeBindings
        Editor editor = injector.getInstance(Editor.class);
        assertThat(editor, not(nullValue()));
    }

    @Test
    public void shouldReturnInstanceOfRightClass_whenModuleIsSetAndRetrievedByInterface() {
        Module module = binder -> binder.bind(IEditor.class).to(Editor2.class);
        Injector injector = Guice.createInjector(module);

        IEditor editor = injector.getInstance(IEditor.class);
        assertThat(editor, instanceOf(Editor2.class));
    }

    @Test
    public void shouldReturnNewInstance_whenScopeNotDefined() {
        Injector injector = Guice.createInjector();
        Editor editor1 = injector.getInstance(Editor.class);
        Editor editor2 = injector.getInstance(Editor.class);
        assertThat(editor1, not(equalTo(editor2)));
    }

    @Test
    public void shouldReturnSameInstance_whenScopeSingleton() {
        Injector injector = Guice.createInjector(binder ->
                binder.bind(Editor.class).in(Singleton.class)
        );
        Editor editor1 = injector.getInstance(Editor.class);
        Editor editor2 = injector.getInstance(Editor.class);
        assertThat(editor1, equalTo(editor2));

        // but we still can create new instance by new or other injector
        Injector injector2 = Guice.createInjector();
        assertThat(editor1, not(equalTo(injector2.getInstance(Editor.class))));

        Editor editor = new Editor(); // constructor should not be public https://github.com/google/guice/wiki/KeepConstructorsHidden
        assertThat(editor1, not(equalTo(editor)));
    }

}
