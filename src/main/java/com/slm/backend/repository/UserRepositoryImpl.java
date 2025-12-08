package com.slm.backend.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.slm.backend.entity.QUser;
import com.slm.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> searchUsers(String searchTerm, Boolean isArchived, Pageable pageable) {
        QUser user = QUser.user;

        BooleanBuilder builder = new BooleanBuilder();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            builder.and(
                user.name.containsIgnoreCase(searchTerm)
                    .or(user.email.containsIgnoreCase(searchTerm))
            );
        }

        if (isArchived != null) {
            builder.and(user.isArchived.eq(isArchived));
        }

        JPAQuery<User> query = queryFactory
            .selectFrom(user)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                String property = order.getProperty();
                if (property.equals("name")) {
                    query.orderBy(order.isAscending() ? user.name.asc() : user.name.desc());
                } else if (property.equals("email")) {
                    query.orderBy(order.isAscending() ? user.email.asc() : user.email.desc());
                } else if (property.equals("createdAt")) {
                    query.orderBy(order.isAscending() ? user.createdAt.asc() : user.createdAt.desc());
                }
            });
        }

        List<User> users = query.fetch();

        long total = queryFactory
            .select(user.count())
            .from(user)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(users, pageable, total);
    }
}
