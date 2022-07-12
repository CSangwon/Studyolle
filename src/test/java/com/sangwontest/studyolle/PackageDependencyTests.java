package com.sangwontest.studyolle;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.boot.test.context.SpringBootTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = StudyolleApplication.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAnyPackage("..modules.study..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAnyPackage("..modules.study..")
            .should().accessClassesThat()
            .resideInAnyPackage(STUDY, ACCOUNT, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAnyPackage("..modules.study..")
            .should().accessClassesThat()
            .resideInAnyPackage(TAG, ZONE, ACCOUNT);

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.sangwontest.studyolle.modules.(*)..")
            .should().beFreeOfCycles();
}
