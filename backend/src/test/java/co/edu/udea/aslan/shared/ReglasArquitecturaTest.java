package co.edu.udea.aslan.shared;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regla 1 de diseño: un módulo nunca importa clases de dominio/ o infraestructura/ de otro.
 */
class ReglasArquitecturaTest {

    private static final String RAIZ = "co.edu.udea.aslan";

    private static JavaClasses clases;

    @BeforeAll
    static void importarClases() {
        clases = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RAIZ);
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuarios", "materias", "sesiones", "planificador", "ia"})
    void otrosModulosNoDependenDelDominioNiDeLaInfraestructuraDe(String modulo) {
        String paqueteModulo = RAIZ + "." + modulo;
        noClasses()
                .that().resideOutsideOfPackage(paqueteModulo + "..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(paqueteModulo + ".dominio..", paqueteModulo + ".infraestructura..")
                .allowEmptyShould(true)
                .check(clases);
    }
}
