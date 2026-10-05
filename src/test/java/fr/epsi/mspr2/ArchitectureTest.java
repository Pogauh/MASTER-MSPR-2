package fr.epsi.mspr2;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Garde-fous de l'architecture hexagonale :
 * <pre>
 *   api  ──▶  domain  ◀──  infrastructure
 * </pre>
 * Le domaine (modèles, ports, services) ne dépend de rien d'extérieur ; l'API REST et l'infrastructure
 * (JPA, MQTT, e-mail…) dépendent du domaine, jamais l'inverse ni l'un de l'autre.
 */
@AnalyzeClasses(packages = "fr.epsi.mspr2", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String DOMAIN = "fr.epsi.mspr2.domain..";
    private static final String API = "fr.epsi.mspr2.api..";
    private static final String INFRASTRUCTURE = "fr.epsi.mspr2.infrastructure..";

    @ArchTest
    static final ArchRule les_couches_respectent_le_sens_des_dependances = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domaine").definedBy(DOMAIN)
            .layer("API").definedBy(API)
            .layer("Infrastructure").definedBy(INFRASTRUCTURE)
            .whereLayer("API").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("Domaine").mayOnlyBeAccessedByLayers("API", "Infrastructure");

    @ArchTest
    static final ArchRule le_domaine_ne_depend_ni_de_l_api_ni_de_l_infrastructure = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(API, INFRASTRUCTURE);

    @ArchTest
    static final ArchRule le_domaine_est_independant_des_frameworks = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "javax..", "org.hibernate..",
                    "com.fasterxml.jackson..", "org.eclipse.paho..", "org.mapstruct..")
            .because("le domaine ne doit connaître ni le web, ni la persistance, ni la sérialisation, ni le transport MQTT");

    @ArchTest
    static final ArchRule l_api_ne_depend_pas_de_l_infrastructure = noClasses().that().resideInAPackage(API)
            .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE);

    @ArchTest
    static final ArchRule l_infrastructure_ne_depend_pas_de_l_api = noClasses().that().resideInAPackage(INFRASTRUCTURE)
            .should().dependOnClassesThat().resideInAPackage(API);

    @ArchTest
    static final ArchRule les_couches_ne_forment_pas_de_cycle = slices()
            .matching("fr.epsi.mspr2.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule les_controleurs_ne_connaissent_que_les_services_du_domaine = noClasses()
            .that().resideInAPackage("fr.epsi.mspr2.api.controller..")
            .should().dependOnClassesThat().resideInAnyPackage("fr.epsi.mspr2.domain.ports..", INFRASTRUCTURE)
            .because("un contrôleur passe par un service, jamais directement par un port ou un adaptateur");

    @ArchTest
    static final ArchRule les_services_du_domaine_ne_dependent_que_des_ports_et_des_modeles = classes()
            .that().resideInAPackage("fr.epsi.mspr2.domain.services..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "fr.epsi.mspr2.domain..", "java..", "lombok..");

    @ArchTest
    static final ArchRule les_ports_sont_des_interfaces = classes()
            .that().resideInAPackage("fr.epsi.mspr2.domain.ports..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule les_adaptateurs_de_persistance_realisent_un_port_du_domaine = classes()
            .that().resideInAPackage("fr.epsi.mspr2.infrastructure.persistence.adapter..")
            .should().implement(resideInAnyPackage("fr.epsi.mspr2.domain.ports.."));

    @ArchTest
    static final ArchRule les_entites_jpa_restent_dans_la_couche_de_persistance = classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage("fr.epsi.mspr2.infrastructure.persistence.entity..");

    @ArchTest
    static final ArchRule les_repositories_jpa_ne_sont_utilises_que_par_les_adaptateurs = classes()
            .that().resideInAPackage("fr.epsi.mspr2.infrastructure.persistence.repository..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage(
                    "fr.epsi.mspr2.infrastructure.persistence.adapter..",
                    "fr.epsi.mspr2.infrastructure.persistence.repository..");

    @ArchTest
    static final ArchRule les_controleurs_sont_nommes_Controller_et_vivent_dans_api_controller = classes()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .andShould().resideInAPackage("fr.epsi.mspr2.api.controller..");
}
