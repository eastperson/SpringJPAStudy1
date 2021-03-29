package com.ep.studyplatform.modules.study;

import com.ep.studyplatform.modules.account.QAccount;
import com.ep.studyplatform.modules.tag.QTag;
import com.ep.studyplatform.modules.tag.Tag;
import com.ep.studyplatform.modules.zone.QZone;
import com.ep.studyplatform.modules.zone.Zone;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    // 상위 생성자에 기본 생성자가 없고, 상위 클래스에 필수 생성자를 충족 시켜야 하기 때문이다.
    public StudyRepositoryExtensionImpl() {
        // 우리는 Impl에 사용할 클래스의 타입을 알고 있다. 그래서 부모 클래스의 매개변수에 넣어줄 수 있다.
        super(Study.class);
    }

    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                // 태그 중에 아무거나
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                // 지역 중에 아무거나
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin()
                .distinct();
        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> fetchResults = pageableQuery.fetchResults();

        // fetch를 사용하면 Study의 목록을 가져올 수 있다.
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.closed.isFalse())
                .and(study.tags.any().in(tags))
                .and(study.zones.any().in(zones)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .orderBy(study.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}

//    @Override
//    public List<Study> findByPublishedDateInMain() {
//        QStudy study = QStudy.study;
//        JPQLQuery<Study> query = from(study).where(study.published.isTrue())
//                .limit(9)
//                .offset(0)
//                .orderBy(new OrderSpecifier<>(Order.DESC,study.publishedDateTime))
//                .leftJoin(study.tags, QTag.tag).fetchJoin()
//                .leftJoin(study.zones, QZone.zone).fetchJoin()
//                .leftJoin(study.members, QAccount.account).fetchJoin()
//                .distinct();
//
//        return query.fetch();
//    }

