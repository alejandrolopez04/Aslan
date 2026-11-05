package co.edu.udea.aslan.shared;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Estrategias contra antipatrones de la sección 5.3 del documento de diseño.
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
    void acoplamientoRigidoOtrosModulosNoDependenDelDominioNiDeLaInfraestructuraDe(String modulo) {
        String paqueteModulo = RAIZ + "." + modulo;
        noClasses()
                .that().resideOutsideOfPackage(paqueteModulo + "..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(paqueteModulo + ".dominio..", paqueteModulo + ".infraestructura..")
                .allowEmptyShould(true)
                .check(clases);
    }

    @Test
    void dependenciasCircularesNoHayCiclosEntreModulos() {
        slices().matching(RAIZ + ".(*)..")
                .should().beFreeOfCycles()
                .check(clases);
    }

    @Test
    void fabricacionPuraElDominioNoDependeDeFrameworksNiDeInfraestructura() {
        noClasses()
                .that().resideInAPackage(RAIZ + "..dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "io.jsonwebtoken..",
                        RAIZ + "..infraestructura..")
                .check(clases);
    }
}
