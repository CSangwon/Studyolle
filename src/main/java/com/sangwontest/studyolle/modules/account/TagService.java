package com.sangwontest.studyolle.modules.account;

import com.sangwontest.studyolle.modules.account.repository.TagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagsRepository tagsRepository;

    public Tags findOrCreateNew(String tagTitle) {

        Tags tags = tagsRepository.findByTitle(tagTitle);
        if (tags == null) {
            log.info("tag is null");
            tags = tagsRepository.save(Tags.builder().title(tagTitle).build());
        }

        return tags;

    }
}
