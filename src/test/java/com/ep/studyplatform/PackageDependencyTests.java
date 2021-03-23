package com.ep.studyplatform;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;


// App.class가 있는 패키지를 분석
@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAnyPackage("com.studyplatform.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.studyplatform.modules..");

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage("..modules.study..")
                .should().onlyBeAccessed().byClassesThat()
                .resideInAnyPackage(STUDY,EVENT);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, ACCOUNT, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);

    // 각각의 모듈간에 circular dependency가 없는지
    @ArchTest
    ArchRule cycleCheck = slices().matching("com.studyplatform.modules.(*)..")
            .should().beFreeOfCycles();

}
