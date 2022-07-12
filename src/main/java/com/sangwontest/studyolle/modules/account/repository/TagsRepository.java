package com.sangwontest.studyolle.modules.account.repository;

import com.sangwontest.studyolle.modules.account.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagsRepository extends JpaRepository<Tags, Long> {
    Tags findByTitle(String title);
}
