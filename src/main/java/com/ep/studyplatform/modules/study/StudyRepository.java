package com.ep.studyplatform.modules.study;

import com.ep.studyplatform.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study,Long>,StudyRepositoryExtension {
    boolean existsByPath(String path);

    // LOAD는 엔티티 그래프에 명시한 연관관계는 Eager모드로 가져오고, 나머지 attribute는 기본 fetch 타입(one to one, many to one 은 eager, many로 끝나는 타입은 lazy로 가져온다.)
    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);// JPA는 withTags라는 무의미한 단어이다.그래서 다른 엔티티 그래프를 사용한다.

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

    @EntityGraph(value = "Study.withTagsAndZones", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsAndZonesById(Long id);

    // 한 곳에서만 사용하는 EntityGraph는 아래와 같은 방법으로 해도 된다.
    // 도메인에 있는 엔티티그래프를 따로 정의해주지 않아도 된다.
    @EntityGraph(attributePaths = {"members","managers"})
    Study findStudyWithMangersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"zones", "tags"})
    List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published,boolean closed);

    List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean b);

    List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean b);
}
