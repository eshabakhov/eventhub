package org.kmb.eventhub.tag.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.common.exception.AlreadyExistsException;
import org.kmb.eventhub.common.exception.MissingFieldException;
import org.kmb.eventhub.event.exception.EventNotFoundException;
import org.kmb.eventhub.tables.daos.UserDao;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tag.dto.EventTagsDTO;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.tag.exception.TagNotFoundException;
import org.kmb.eventhub.tag.mapper.TagMapper;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.TagDao;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tag.repository.TagRepository;
import org.kmb.eventhub.tag.dto.TagDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    private final TagDao tagDao;

    private final UserDao userDao;

    private final EventDao eventDao;

    private final TagMapper tagMapper;

    public ResponseList<Tag> getList(Integer page, Integer pageSize) {
        ResponseList<Tag> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<Tag> list =  tagRepository.fetch(condition);

        responseList.setList(list);
        responseList.setTotal(tagRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public Tag get(Long id) {
        return tagDao.fetchOptionalById(id).orElseThrow(() -> new TagNotFoundException(id));
    }

    @Transactional
    public void addTagsToUser(Long tagId, Long userId) {
        userDao.findOptionalById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        tagDao.findOptionalById(tagId).orElseThrow(() -> new TagNotFoundException(tagId));

        assignTagsToUser(userId, tagId);
    }

    @Transactional
    public Tag addTagsToEvent(Long eventId, TagDTO tagDTO) {

        if (Objects.isNull(tagDTO.getName()) || tagDTO.getName().isEmpty())
            throw new MissingFieldException("name");

        Tag tag = tagMapper.toEntity(tagDTO);

        var existingTag = tagDao.fetchByName(tagDTO.getName());

        if (existingTag.isEmpty()) {
            tagDao.insert(tag);
        } else {
            tag.setId(existingTag.getFirst().getId());
        }

        eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        assignTagToEvent(eventId, tag);
        return tag;
    }

    @Transactional
    public void deleteTagFromEvent(Long tagId, Long eventId) {
        if (tagDao.fetchOptionalById(tagId).isEmpty()) {
            throw new TagNotFoundException(tagId);
        }
        eventDao.fetchOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        tagRepository.deleteTagFromEvent(tagId, eventId);
        if (!tagRepository.tagIsUsed(tagId)) {
            tagDao.deleteById(tagId);
        }
    }

    @Transactional
    public void deleteTagFromUser(Long tagId, Long userId) {
        if (tagDao.fetchOptionalById(tagId).isEmpty()) {
            throw new TagNotFoundException(tagId);
        }
        eventDao.fetchOptionalById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        tagRepository.deleteTagFromUser(tagId, userId);
        if (!tagRepository.tagIsUsed(tagId)) {
            tagDao.deleteById(tagId);
        }
    }

    public List<String> createNewTags(List<String> tags) {
        List<String> existingTagNames = tagRepository.fetch(tags).stream().map(Tag::getName).toList();

        List<String> newTagNames = tags.stream()
                .filter(tag -> !existingTagNames.contains(tag))
                .distinct()
                .toList();

        tagRepository.createTags(newTagNames);

        return newTagNames;
    }

    public Set<Long> getUsedTagIdsForEvent(Long eventId) {
        return tagRepository.getUsedTagIdsForEvent(eventId);
    }

    public void assignTagsToEvent(Long eventId, List<Tag> tags) {
         tagRepository.assignNewEventTags(eventId, tags);
    }
    public void assignTagToEvent(Long eventId, Tag tag) {
        tagRepository.assignNewEventTag(eventId, tag);
    }


    public Set<Long> getUsedTagIdsForUser(Long userId) {
        return tagRepository.getUsedTagIdsForUser(userId);
    }

    public void assignTagsToUser(Long userId, Long tagId) {
        tagRepository.assignTagToUser(tagId, userId);
    }
}
